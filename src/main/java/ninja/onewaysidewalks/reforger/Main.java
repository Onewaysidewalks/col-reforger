package ninja.onewaysidewalks.reforger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.sourceforge.tess4j.Tesseract;

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
import java.util.Map;

/**
 * Entry point for application. Requires credentials stored in environment variables prior to execution.
 */
public class Main extends Applet {
    static Robot ROBOT;
    static Tesseract tesseract = new Tesseract();

    public static void main(String args[]) throws AWTException, InterruptedException, IOException {
        ROBOT = new Robot();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = mapper.readValue(new File("reforger.yaml"), Config.class);

        if (args.length > 0 && args[0].equalsIgnoreCase("OCR-TEST")) {
            System.out.println("running as test");
            config.setTest(true);
        }

        System.out.println("Running with config: " + config.toString());

        if (args.length <= 0) {
            System.out.println("No Valid Argument for App");
            return;
        }

        String windowName = getWindowName(config.getWindowNames());

        boolean done = false;
        for (int i = 0; i < config.getExecution().getMaxAttempts() && !done; i++) {
            List<String> output = ocr(config, windowName);

            if (meetsCriteria(config, output)) {
                System.out.println("found matching reforge");

                done = true;
            } else {
                executeReforge(config, windowName);
            }
        }

        System.out.println("Execution end");
//        System.in.read();
    }

    /**
     * Evaluate each line for matching criteria from config
     */
    private static boolean meetsCriteria(Config config, List<String> lines) {
        System.out.println("Looking for " + config.getExecution().getStatToDesiredCount());

        for (Map<Config.StatType, Integer> desiredStatMap : config.getExecution().getStatToDesiredCount()) {
            boolean found = false;
            for (Map.Entry<Config.StatType, Integer> desiredStat
                    : desiredStatMap.entrySet()) {
                int score = 0;

                for (String line : lines) {
                    line = line.toLowerCase();
                    boolean matched = true;
                    for (Config.StatConfig statConfig : config.getStats().get(desiredStat.getKey())) {
                        if (statConfig.getType().equals(Config.StatConfigComparitor.AND)) {
                            matched = matched && statConfig.getValues().stream().anyMatch(line::contains);
                        } else if (statConfig.getType().equals(Config.StatConfigComparitor.OR)) {
                            matched = matched || statConfig.getValues().stream().anyMatch(line::contains);
                        } else if (statConfig.getType().equals(Config.StatConfigComparitor.AND_NOT)) {
                            matched = matched && statConfig.getValues().stream().noneMatch(line::contains);
                        } else {
                            throw new RuntimeException("Unhandled stat type comparator " + statConfig.getType());
                        }
                    }

                    if (matched) {
                        System.out.println(String.format(" %s [1],", line));
                        score++;
                    } else {
                        System.out.println(String.format(" %s [0],", line));
                    }
                }

                if (score >= desiredStat.getValue()) {
                    System.out.println("Matched " + desiredStat.getKey()
                            + " with a score of " + desiredStat.getValue());
                    found = true;
                } else {
                    //not enough stat present
                    //reforge/abort
                    System.out.println("Insufficient  match, reforging. (Failed on "
                            + desiredStat.getKey() + " with score " + score + ")");
                    found = false;
                    break;
                }
            }

            if (found) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method will execute a click to process reforge
     */
    private static void executeReforge(Config config, String windowName) throws InterruptedException {
        System.out.println("executing reforge");
        try {
            if (config.isTest()) {
                System.out.println("skipping reforge due to test");
                return;
            }

            //get the bounds/position of the game window
            Rectangle rectangle = WindowsOSUtility.getRect(windowName);

            //use relative distances to find the reforge button
            //for smaller window
            ROBOT.mouseMove((int) ((rectangle.x + rectangle.width) * .92),
                    (int) ((rectangle.y + rectangle.height) * .9));

            Thread.sleep(1000);

            ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            //use relative distances to find the reforge button
            //for full screen
            ROBOT.mouseMove((int) ((rectangle.x + rectangle.width) * .90),
                    (int) ((rectangle.y + rectangle.height) * .9));
            ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Thread.sleep(4000);
    }

    private static List<String> ocr(Config config, String windowName) {
        List<String> lines = new ArrayList<>();


        BufferedImage rawImage = getWindowImage(windowName);
        BufferedImage bufferedImage = getCoLReforgeRight(rawImage);

        try {
            File inputFile = new File(config.getPreprocessFile());

            if (inputFile.exists()) {
                System.out.println("preprocessed image already exists, deleting");
                inputFile.delete(); //delete before recreation
            }

            //Create the preprocessed image
            System.out.println("creating preproccessed image");
            preprocess(bufferedImage);

            ImageIO.write(bufferedImage, "png", inputFile);

            String result = tesseract.doOCR(inputFile);
            Arrays.asList(result.split(System.lineSeparator()))
                    .forEach(x -> lines.addAll(Arrays.asList(x.split("\n"))));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return lines;
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
            System.out.println(WindowsOSUtility.getRect(getWindowName(
                    Arrays.asList("Crusaders of Light", "[#] Crusaders of Light [#]"))).toString());
        } catch (WindowsOSUtility.WindowNotFoundException | WindowsOSUtility.GetWindowRectException e) {
            e.printStackTrace();
        }
        g.drawImage(getWindowImage(getWindowName(
                Arrays.asList("Crusaders of Light", "[#] Crusaders of Light [#]"))),
                0, 0, null);
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

    private static String getWindowName(List<String> possibleNames) {
        for (String name : possibleNames) {
            try {
                WindowsOSUtility.getRect(name);
                System.out.println("Found window with name " + name);
                return name;
            } catch (Exception e) {
                System.out.println(name + " window not found, trying next");
            }
        }

        throw new RuntimeException("Window cannot be found, tried: " + possibleNames);
    }
}
