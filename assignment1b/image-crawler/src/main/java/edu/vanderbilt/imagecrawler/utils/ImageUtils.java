package edu.vanderbilt.imagecrawler.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import edu.vanderbilt.imagecrawler.web.TransformedImage;

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
            throw ExceptionUtils.unchecked(e);
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
            throw ExceptionUtils.unchecked(e);
        }
    }

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