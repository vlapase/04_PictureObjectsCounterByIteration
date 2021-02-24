// file: objects counter from b/w image source.

package com.shpp.p2p.cs.vpasechnyk.assignment13;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class PictureObjectsCounterByIteration {

    // sensitivity level
    public static int pictureSensitivityLevel = 100;

    public static int[][] mainArray;

    // 0 - no pixel
    // 1 - pixel is present
    // so, 2 will be first object number
    public static int marker = 1;

    // drop objects smaller then:
    public static int maxPixelsPerCountableObject = 200;

    // Erosion depth in pixels from 0 (no erosion) to 7 (max to avoid lags):
    public static int maxErosion = 3;

    // current object pixel counter
    public static int inObjectPixelsCounter;


    public static void main(String[] args) {

        String inputFile;

        try {
            inputFile = args[0];
        } catch (Exception e) {
            System.out.println("Sorry, no Program arguments added, trying to process \"test.jpg\"");
            inputFile = "test.jpg";
        }

        // creating the main calculation array
        mainArray = pictureToIntArray(inputFile);

        // objectErosion to cut sticky objects
        objectErosion();

        // MAIN ENGINE
        iterationObjectFinder();

        // analyzeIt
        System.out.println("Objects found: " + analyzeIt());

//      print image to console to debug
      for (int y = 0; y < mainArray.length; y++) System.out.println(Arrays.toString(mainArray[y]));
//      analyzeIt repeat
//      System.out.println("Objects found: " + analyzeIt());
    }

    /**
     * objectErosion to cut sticky objects
     */
    private static void objectErosion() {
        // anyway maxErosion will be no more then 7
        if (maxErosion > 7) maxErosion = 7;
        for (int i = 1; i <= maxErosion; i++) {
            for (int y = i; y < mainArray.length - 1; y += 3) {
                for (int x = i; x < mainArray[0].length - 1; x += 3) {

                    if (mainArray[y][x] == 1 && (
                        mainArray[y][x - 1] == 0 ||
                        mainArray[y][x + 1] == 0 ||
                        mainArray[y - 1][x] == 0 ||
                        mainArray[y - 1][x - 1] == 0 ||
                        mainArray[y - 1][x + 1] == 0 ||
                        mainArray[y + 1][x] == 0 ||
                        mainArray[y + 1][x - 1] == 0 ||
                        mainArray[y + 1][x + 1] == 0
                    )) {
                        // cut 1 pixel
                        mainArray[y][x] = 0;
                    }
                }
            }
            System.out.println("picture erosion step " + i + " done");
        }
    }

    /**
     * find objects by iteration
     */
    private static void iterationObjectFinder() {
        for (int y = 0; y < mainArray.length; y++) {

            for (int x = 0; x < mainArray[0].length; x++) {

                // picture present
                if (mainArray[y][x] == 1) {

                    // first row first col

                    //noinspection ConstantConditions
                    if ((y == 0 && x == 0) || (y == 0 && x > 0 && mainArray[y][x - 1] == 0)) {
                        marker++;
                        mainArray[y][x] = marker;
                    }

                    // no left and up empty or up and left empty
                    if ((x == 0 && y > 0 && mainArray[y - 1][x] == 0) || (x > 0 && y > 0 && mainArray[y][x - 1] == 0 && mainArray[y - 1][x] == 0)) {
                        marker++;
                        mainArray[y][x] = marker;
                    }

                    // no left or left empty
                    if ((x > 0 && mainArray[y][x - 1] == 0) || x == 0) {

                        // up present
                        if (y > 0 && mainArray[y - 1][x] > 1) {
                            mainArray[y][x] = mainArray[y - 1][x];
                        }
                    }

                    // left present
                    if (x > 0 && mainArray[y][x - 1] > 1) {
                        mainArray[y][x] = mainArray[y][x - 1];

                        // up present and differs
                        if (y > 0 && mainArray[y - 1][x] > 1 && mainArray[y - 1][x] != mainArray[y][x]) {
                            renameIt(mainArray[y - 1][x], mainArray[y][x]);
                        }
                    }
                }
            }
        }
    }

    /**
     * object counter
     *
     * @return calculated objects total
     */
    private static int analyzeIt() {
        int objectsTotal = 0;
        int currentObject;
        for (int i = 2; i <= marker; i++) {

            currentObject = 0;
            //noinspection ForLoopReplaceableByForEach
            for (int y = 0; y < mainArray.length; y++) {
                for (int x = 0; x < mainArray[0].length; x++) {
                    if (mainArray[y][x] == i) currentObject++;
                }
            }

            if (currentObject > maxPixelsPerCountableObject) {
                objectsTotal++;
                System.out.println("Object #" + objectsTotal + " has " + currentObject + " pixels.");
            }
        }
        return objectsTotal;
    }

    /**
     * rename areas
     *
     * @param oldMarker old marker
     * @param newMarker new marker
     */
    private static void renameIt(int oldMarker, int newMarker) {

        for (int y = 0; y < mainArray.length; y++) {
            for (int x = 0; x < mainArray[0].length; x++) {
                if (mainArray[y][x] == oldMarker) mainArray[y][x] = newMarker;
            }
        }
    }


    /**
     * read file, zoom it and return as array
     *
     * @param fileFromMethodStart read file
     * @return return as array
     */
    private static int[][] pictureToIntArray(String fileFromMethodStart) {
        int[][] inPictureAsArray = new int[0][];
        try {
            File inPicture = new File(fileFromMethodStart);
            BufferedImage sourcePictureInMemory = ImageIO.read(inPicture);

            System.out.println("source picture size in pixel: W " + sourcePictureInMemory.getWidth() + " H " +
                    sourcePictureInMemory.getHeight());

            inPictureAsArray = new int[sourcePictureInMemory.getHeight()][sourcePictureInMemory.getWidth()];

            // check background (working for b/w picture)
            boolean whiteBackgroundFlag = isWhiteBackgroundFlag(sourcePictureInMemory);

            // fill new array from source
            arrayFiller(inPictureAsArray, sourcePictureInMemory, whiteBackgroundFlag);

        } catch (IOException e) {
            System.out.println("source image read error");
        }
        return inPictureAsArray;
    }

    /**
     * fill new array from source
     *
     * @param inPictureAsArray      array to fill
     * @param sourcePictureInMemory source picture
     * @param whiteBackgroundFlag   background flag
     */
    private static void arrayFiller(int[][] inPictureAsArray, BufferedImage sourcePictureInMemory, boolean whiteBackgroundFlag) {

        for (int y = 0; y < sourcePictureInMemory.getHeight(); y++) {
            for (int x = 0; x < sourcePictureInMemory.getWidth(); x++) {

                // take current pixel color
                Color color = new Color(sourcePictureInMemory.getRGB(x, y));

                // it can be any color, either red, green or blue
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                if (whiteBackgroundFlag) {
                    if (red < pictureSensitivityLevel || green < pictureSensitivityLevel || blue < pictureSensitivityLevel) {
                        inPictureAsArray[y][x] = 1;
                    } else inPictureAsArray[y][x] = 0;
                } else {
                    if (red > pictureSensitivityLevel || green > pictureSensitivityLevel || blue > pictureSensitivityLevel) {
                        inPictureAsArray[y][x] = 1;
                    } else inPictureAsArray[y][x] = 0;
                }
            }
        }
    }

    /**
     * check background (working for b/w picture)
     *
     * @param sourcePictureInMemory source picture
     * @return flag: true - background seems to be white
     */
    private static boolean isWhiteBackgroundFlag(BufferedImage sourcePictureInMemory) {

        double averageSum;
        int totalX = 0;
        for (int y = 0; y < sourcePictureInMemory.getHeight(); y += sourcePictureInMemory.getHeight() - 1) {
            for (int x = 0; x < sourcePictureInMemory.getWidth(); x++) {

                // take current pixel color
                Color color = new Color(sourcePictureInMemory.getRGB(x, y));
                if (color.getRed() > pictureSensitivityLevel) totalX++;
            }
        }

        averageSum = (double) totalX / sourcePictureInMemory.getWidth() / 2;
        totalX = 0;

        for (int x = 0; x < sourcePictureInMemory.getWidth(); x += sourcePictureInMemory.getWidth() - 1) {
            for (int y = 0; y < sourcePictureInMemory.getHeight(); y++) {
                // take current pixel color
                Color color = new Color(sourcePictureInMemory.getRGB(x, y));
                if (color.getRed() > pictureSensitivityLevel) totalX++;
            }
        }

        averageSum += (double) totalX / sourcePictureInMemory.getHeight() / 2;

        // encountering two steps and 255 as max color
        averageSum = averageSum / 2 * 255;

        if (averageSum > pictureSensitivityLevel) System.out.println("background color is closer to white.");
        else  System.out.println("background color is closer to black.");

        return averageSum > pictureSensitivityLevel;
    }
}