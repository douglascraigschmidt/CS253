package edu.vanderbilt.imagecrawler.web;

import java.awt.image.BufferedImage;
import java.util.Base64;

import edu.vanderbilt.imagecrawler.utils.ImageUtils;

/**
 * Data Transfer Object (DTO) used to return a {@link
 * TransformedImage} back to the client app.
 */
public class TransformedImage {
    /**
     * Source image name.
     */
    private String sourceName;

    /**
     * Transformed name.
     */
    private String transformName;

    /**
     * A 64-bit encoded image.
     */
    private String encodedBytes;

    /**
     * Default constructor is required for Jackson.
     */
    public TransformedImage() {
    }

    /**
     * Constructor for an image byte array.
     *
     * @param sourceName    Source image name
     * @param transformName Transform name
     * @param bytes Image byte array
     */
    public TransformedImage(String sourceName,
                            String transformName,
                            byte[] bytes) {
        this.sourceName = sourceName;
        this.transformName = transformName;
        this.encodedBytes = Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Constructor for a {@link BufferedImage}.
     *
     * @param sourceName    Source image name
     * @param transformName Transform name
     * @param bufferedImage Java {@link BufferedImage}
     */
    public TransformedImage(String sourceName,
                            String transformName,
                            BufferedImage bufferedImage) {
        this.sourceName = sourceName;
        this.transformName = transformName;
        this.encodedBytes = Base64
            .getEncoder()
            .encodeToString(ImageUtils.toByteArray(bufferedImage, "png"));
    }

    /**
     * @return Source image name.
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Sets the source image name.
     *
     * @param sourceName Source image name.
     */
    public void setSourceName(String sourceName) {
        this.transformName = sourceName;
    }

    /**
     * @return Transform name.
     */
    public String getTransformName() {
        return transformName;
    }

    /**
     * Sets the transform name.
     *
     * @param transformName
     */
    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    /**
     * @return Image bytes as a 64-bit encoded String
     */
    public String getEncodedBytes() {
        return encodedBytes;
    }

    /**
     * Directly sets the 64-bit encoded image bytes member.
     *
     * @param encodedBytes Image bytes encoded to a 64-bit String
     */
    public void setEncodedBytes(String encodedBytes) {
        this.encodedBytes = encodedBytes;
    }

    /**
     * @return Decoded image bytes
     */
    public byte[] getBytes() {
        return Base64.getDecoder().decode(encodedBytes);
    }

    /**
     * Encodes and saved passed image bytes.
     *
     * @param bytes Image bytes
     */
    public void setBytes(byte[] bytes) {
        encodedBytes = Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Custom toString implemention.
     *
     * @return Custom string for debug output
     */
    @Override
    public String toString() {
        return "TransformedImage{" +
            "sourceName='" + sourceName + '\'' +
            "transformName='" + transformName + '\'' +
            ", encodedBytes='" + encodedBytes + '\'' +
            '}';
    }
}
