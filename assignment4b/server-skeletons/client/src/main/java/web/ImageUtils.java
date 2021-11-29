package web;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {
    /**
     * Converts a BufferedImage to a formatted byte array.
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
            // Rethrow as unchecked exception.
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts image byte[] to a BufferedImage.
     *
     * @param bytes bytes to convert.
     * @return BufferedImage
     */
    public static BufferedImage toBufferedImage(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            // Rethrow as unchecked exception.
            throw new RuntimeException(e);
        }
    }
}