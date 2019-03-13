package ninja.onewaysidewalks.reforger;

import javafx.application.Application;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.bytedeco.javacpp.lept.pixRead;

/**
 * Entry point for application. Requires credentials stored in environment variables prior to execution.
 */
public class Main extends Applet {

    static String GAME_WINDOW_NAME = "Crusaders of Light";
    static String PREPROCESS_FILE = "preprocess_image.png";
    static Robot ROBOT;
    static final JFXPanel fxPanel = new JFXPanel();

    public static void main(String args[]) throws AWTException, InterruptedException {
        ROBOT = new Robot();

        if (args.length <= 0) {
            System.out.println("No Valid Argument for App");
            return;
        }

        boolean done = false;
        while (!done) {
            List<String> output = ocr();

            System.out.println(output);

            if (meetsCriteria(2, output)) {
                System.out.println("found matching reforge");
                Media ping = new Media(new File("ping.mp3").toURI().toString());

                new MediaPlayer(ping).play();

                done = true;
            } else {
                executeReforge();
            }
        }

        while (true) {
            Thread.sleep(10000);
        }
    }

    /**
     * Evaluate each line for matching criteria
     * @param desiredScore this many lines must match criteria
     * @return whether or not the lineset matches
     */
    private static boolean meetsCriteria(int desiredScore, List<String> lines) {
        //todo make criteria generic
        int score = 0;
        for(String line : lines) {
            if (StatUtility.containsParry(line)) {
                score++;
            }
        }

        System.out.println("Found score of " + score);

        return score >= desiredScore;
    }

    /**
     * Method will execute a click to process reforge
     */
    private static void executeReforge() throws InterruptedException {
        System.out.println("executing reforge");
        try {
            //get the bounds/position of the game window
            Rectangle rectangle = WindowsOSUtility.getRect(GAME_WINDOW_NAME);

            //use relative distances to find the reforge button
            ROBOT.mouseMove((int) ((rectangle.x + rectangle.width) * .8),
                    (int) ((rectangle.y + rectangle.height) * .9));

            Thread.sleep(1000);

            ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(4000);
    }

    private static List<String> ocr() {

        BufferedImage rawImage = getWindowImage(GAME_WINDOW_NAME);
        BufferedImage bufferedImage = getCoLReforgeRight(rawImage);

        tesseract.TessBaseAPI tessBaseAPI = new tesseract.TessBaseAPI();

        try {
            File inputFile = new File(PREPROCESS_FILE);

            if (inputFile.exists()) {
                System.out.println("preprocessed image already exists, deleting");
                inputFile.delete(); //delete before recreation
            }

            //Create the preprocessed image
            System.out.println("creating preproccessed image");
            preprocess(bufferedImage);

            ImageIO.write(bufferedImage, "png", inputFile);


            tessBaseAPI.Init(null, "eng");
            lept.PIX image = pixRead(inputFile.getAbsolutePath());

            tessBaseAPI.SetImage(image);

            BytePointer resultPointer = tessBaseAPI.GetUTF8Text();
            String result = resultPointer.getString();

            try {
//                System.out.println(result); //debug

                ArrayList<String> retList = new ArrayList<>();
                Arrays.asList(result.split("\\r?\\n")).forEach(s -> retList.add(s.toLowerCase()));

                return retList;
            } finally {
                if (resultPointer != null) {
                    resultPointer.deallocate();
                }

                if (image.refcount() >0) image.deallocate();

                tessBaseAPI.End();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (tessBaseAPI != null) {
                tessBaseAPI.End();
            }
        }
    }

    /**
     * This returns only the right hand portion of the reforge screen, as its all we're interested in
     * It is done with relative, hardcoded %'s based on original screen slice
     * 10% off the height, 50% taken from the left, 30% off the bottom
     * @param img the snapshot of CoL window
     * @return the RHS of the reforge window
     */
    private static BufferedImage getCoLReforgeRight(BufferedImage img) {
        return img.getSubimage(img.getWidth() / 2,
                (int) (img.getHeight() * .15),
                img.getWidth() / 2,
                (int) (img.getHeight() * .7));
    }

    /**
     * Get Windows image for specified window.
     * The window MUST be in the foreground (so using fullscreen is not optimal)
     * @return the image
     */
    private static BufferedImage getWindowImage(String windowName) {
        try {
            return ROBOT.createScreenCapture(WindowsOSUtility.getRect(windowName));
        } catch (WindowsOSUtility.GetWindowRectException | WindowsOSUtility.WindowNotFoundException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("couldnt find game window with name " + windowName);
    }

    /**
     * Only used in debugging purposes to see game screen window
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        try {
            System.out.println(WindowsOSUtility.getRect(GAME_WINDOW_NAME).toString());
        } catch (WindowsOSUtility.WindowNotFoundException | WindowsOSUtility.GetWindowRectException e) {
            e.printStackTrace();
        }
        g.drawImage(getWindowImage(GAME_WINDOW_NAME), 0, 0, null);
    }

    /**
     * Method to strip bland colors and swap others to make image as processable as possible
     * Starts with greyscaling object, then reprocesses to invert solid whites (like numbers) to black
     * @param img
     */
    public static void preprocess(BufferedImage img)
    {
        //First we greyscale
        for (int x = 0; x < img.getWidth(); ++x) {
            for (int y = 0; y < img.getHeight(); ++y) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                int grayLevel = (r + g + b) / 3;
                int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
                img.setRGB(x, y, gray);

            }
        }

        //Then we invert bright whites to blacks
        for (int x = 0; x < img.getWidth(); ++x) {
            for (int y = 0; y < img.getHeight(); ++y) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                if (Math.abs(r - g) <= 1
                        && Math.abs(g - b) <= 1
                        && Math.abs(r - b) <= 1
                        && r > 150) {
                    //strip common greys that are light enough, to pure white
                    //the pixel matches the grey set, set to white
                    img.setRGB(x, y,Color.white.getRGB()); //alpha << 24+r << 16+g << 8+b
                }
            }
        }
    }
}
