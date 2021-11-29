package server.microservices.common;

import org.springframework.stereotype.Component;

import static java.lang.Math.min;

/**
 * Algorithms that perform various image transforms.
 */
@Component
public class Transforms {
    /**
     * Performs a grayscale transformation on passed pixel array.
     */
    public int[] grayScale(int[] pixels, boolean hasAlpha) {
        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            // Skip transparent pixels
            if (hasAlpha && alpha(pixel) == 0) {
                continue;
            }

            // Convert the pixel to grayscale.
            int grayScale = (int) (red(pixel) * .299
                                   + green(pixel) * .587
                                   + blue(pixel) * .114);
            pixels[i] = rgb(grayScale, grayScale, grayScale);
        }

        return pixels;
    }

    /**
     * Performs a sepia transformation on passed pixel array.
     */
    public int[] sepia(int[] pixels, boolean hasAlpha) {

        int red;
        int green;
        int blue;
        int alpha;
        int pixel;
        int depth = 20;

        for (int i = 0; i < pixels.length; i++) {
            pixel = pixels[i];

            // Skip transparent pixels
            if (hasAlpha && alpha(pixel) == 0) {
                continue;
            }

            red = red(pixel);
            green = green(pixel);
            blue = blue(pixel);
            alpha = alpha(pixel);

            blue = (red + green + blue) / 3;
            green = blue;
            red = green;

            red += depth * 2;
            green += depth;

            red = min(red, 255);
            green = min(green, 255);

            pixels[i] = rgba(red, blue, green, alpha);
        }

        return pixels;
    }

    /**
     * Performs a tint transformation on passed pixel array.
     */
    public int[] tint(int[] pixels,
                     boolean hasAlpha,
                     float redTint,
                     float greenTint,
                     float blueTint
                     ) {
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            // Skip transparent pixels
            if (hasAlpha && alpha(pixel) == 0) {
                continue;
            }

            int alpha = alpha(pixel);
            int red = (int) (red(pixel) + (255 - red(pixel)) * redTint);
            int green = (int) (green(pixel) + (255 - green(pixel)) * greenTint);
            int blue = (int) (blue(pixel) + (255 - blue(pixel)) * blueTint);

            pixels[i] = rgba(red, green, blue, alpha);
        }

        return pixels;
    }

    /**
     * Returns pixel alpha value
     */
    private int alpha(int color) {
        return color >> 24 & 0xFF;
    }

    /**
     * Returns pixel red value
     */
    private int red(int color) {
        return (color >> 16 & 0xFF);
    }

    /**
     * Returns pixel green value
     */
    private int green(int color) {
        return (color >> 8 & 0xFF);
    }

    /**
     * Returns pixel blue value
     */
    private int blue(int color) {
        return (color & 0xFF);
    }

    /**
     * Builds a pixel value from rgb int values
     */
    private int rgb(int red, int green, int blue) {
        return rgba(red, green, blue, 0xFF);
    }

    /**
     * Builds a pixel value from rgb and alpha int values
     */
    private int rgba(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
