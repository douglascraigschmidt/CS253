package server.main;

import org.apache.commons.lang.RandomStringUtils;
import server.common.ImageUtils;
import server.common.model.TransformedImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * This class creates a list random round flight flights for any number
 * of airlines, airports, travel dates, and number of daily flights.
 */
public class ImageFactory {

    public static TransformedImage randomTransformedImage() {
        return new TransformedImage(
                RandomStringUtils.randomAlphabetic(10),
                RandomStringUtils.randomAlphabetic(10),
                randomBufferedImage()
        );
    }

    public static List<TransformedImage> randomTransformedImages(int size) {
        List<TransformedImage> transformedImages = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            transformedImages.add(randomTransformedImage());
        }
        return transformedImages;
    }


    public static byte[] randomImageBytes() {
        return ImageUtils.toByteArray(randomBufferedImage(), "png");
    }

    public static BufferedImage randomBufferedImage() {
        int width = 250;
        int height = 250;

        BufferedImage bufferedImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        // Fill all the image with white.
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        // Create a circle with black.
        g2d.setColor(Color.black);
        g2d.fillOval(0, 0, width, height);

        // Create a random yellow string.
        g2d.setColor(Color.yellow);
        g2d.drawString(RandomStringUtils.randomAlphabetic(10), 50, 120);

        g2d.dispose();

        return bufferedImage;
    }
}