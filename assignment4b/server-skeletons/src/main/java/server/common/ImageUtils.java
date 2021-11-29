package server.common;

import server.common.model.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * This Java utility class defines static methods that convert various
 * data types to and from {@link BufferedImage}.
 */
public final class ImageUtils {
    /**
     * A Java utility class needs a private constructor.
     */
    private ImageUtils() {}

    /**
     * Converts a BufferedImage to a formatted byte array.
     *
     * @param bi     The {@link BufferedImage} input
     * @param format Format (typically "jpg" or "png")
     * @return image A byte array containing the {@link BufferedImage}
     * contents in the appropriate {@code format}
     */
    public static byte[] toByteArray(BufferedImage bi, String format) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, format, baos);

            return baos.toByteArray();
        } catch (IOException e) {
            // Rethrow as unchecked exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts image byte[] to a {@link BufferedImage}.
     *
     * @param bytes The bytes to convert
     * @return A {@link BufferedImage}
     */
    public static BufferedImage toBufferedImage(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            // Rethrow as unchecked exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the {@link TransformedImage} to a {@link File}
     *
     * @param transformedImage The {@link TransformedImage to save}
     * @return A {@link File} containing the {@link TransformedImage}
     */
    public static File toFile(TransformedImage transformedImage) {
        try {
            byte[] bytes = transformedImage.getBytes();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            BufferedImage bi = ImageIO.read(inputStream);
            String fileName =
                    transformedImage.getTransformName() + "-" + transformedImage.getImageName();
            File file = new File(fileName);
            ImageIO.write(bi, "png", file);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read a {@link BufferedImage} from the given {@code resourceFileName}
     *
     * @param resourceFileName The given resource to read from
     * @return A {@link BufferedImage} containing the contents of the
     * {@code resourceFileName}
     */
    public static BufferedImage toBufferedImage(String resourceFileName) {
        ClassLoader classLoader = ImageUtils.class.getClassLoader();
        try {
            BufferedImage image = null;
            image = ImageIO.read(classLoader.getResource("resources/" + resourceFileName));
            return image;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
