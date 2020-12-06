# Col Reforger

Hate reforging in Crusaders of light? This is the tool for you!

### Requirements
The only requirement to run this tool is java 8 SDK. Most computers already have this installed.
If you do not, you can install and set it up here: [Direct Download link from Oracle](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)


### How to use
Simply ...

- clone/download this repository
- change the `reforger.yaml` to which stats you want to reforge for if needed. (The default is 2 skill roll, with 100 max attempts to get)
- make sure your CoL window is open to a screen that looks like this: 

![image](example.png) 

- open the command prompt or powershell window to this directory, and run `gradlew.bat reforge` 

Note: make sure the command prompt window does not overlap with the CoL window.

Once the program detects your desired, it will stop!

## WARNING: Only Warrior is supported at the moment

# Contributing

If you want to help contribute to which classes are supported, please open an issue with a bunch of screen shots of each possible skill for the class you want to add.

The screen shot should look exactly like this:
![example](example.png)