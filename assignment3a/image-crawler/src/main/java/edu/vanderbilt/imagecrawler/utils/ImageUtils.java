package edu.vanderbilt.imagecrawler.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * This Java utility class contains static methods for manipulating
 * {@link Image} objects.
 */
public final class ImageUtils {
    /**
     * A Java utility class should define a private constructor.
     */
    private ImageUtils() {}

    /**
     * Converts a {@link BufferedImage} to a formatted byte array.
     *
     * @param bi     Buffered image input
     * @param format Format (typically "jpg" or "png").
     * @return image byte array
     */
    public static byte[] toByteArray(BufferedImage bi, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Converts image byte[] to a {@link BufferedImage}.
     *
     * @param bytes bytes to convert into a {@link BufferedImage}
     * @return A new {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }

    /**
     * Convert a {@link TransformedImage} into a {@link File}
     * @param transformedImage The {@link TransformedImage} to convert
     * @return A {@link File} containing the {@link TransformedImage}
     */
    public static File toFile(TransformedImage transformedImage) {
        try {
            byte[] bytes = transformedImage.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            BufferedImage bi = ImageIO.read(inputStream);
            String fileName = transformedImage.getTransformName() + "-" + transformedImage.getSourceName();
            File file = new File(fileName);

            ImageIO.write(bi, "png", file);

            return file;
        } catch (IOException e) {
            throw ExceptionUtils.unchecked(e);
        }
    }
}
