package server.common.model;

import server.common.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.Objects;

/**
 * The Data Transfer Object (DTO) used to return a {@link TransformedImage}
 * back to client application.
 */
public class TransformedImage {
    /**
     * Source image name.
     */
    private String imageName;

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
     * @param imageName     Source image name.
     * @param transformName Transform name.
     * @param encodedBytes  64 bit encoded image bytes.
     */
    public TransformedImage(String imageName, String transformName, String encodedBytes) {
        this.imageName = imageName;
        this.transformName = transformName;
        this.encodedBytes = encodedBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransformedImage)) return false;
        TransformedImage that = (TransformedImage) o;
        return imageName.equals(that.imageName) && transformName.equals(that.transformName) && encodedBytes.equals(that.encodedBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageName, transformName, encodedBytes);
    }

    /**
     * Constructor for an image byte array.
     *
     * @param imageName     Source image name.
     * @param transformName Transform name.
     * @param bytes         Image byte array.
     */
    public TransformedImage(String imageName, String transformName, byte[] bytes) {
        this.imageName = imageName;
        this.transformName = transformName;
        this.encodedBytes = Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Constructor for a {@link BufferedImage}.
     *
     * @param imageName    Source image name.
     * @param transformName Transform name.
     * @param bufferedImage Java {@link BufferedImage}
     */
    public TransformedImage(String imageName, String transformName, BufferedImage bufferedImage) {
        this.imageName = imageName;
        this.transformName = transformName;
        this.encodedBytes =
                Base64.getEncoder().encodeToString(
                        ImageUtils.toByteArray(bufferedImage, "png"));
    }

    /**
     * @return Source image name.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Sets the source image name.
     *
     * @param imageName Source image name.
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
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
     * @return Image bytes as a 64-bit encoded String.
     */
    public String getEncodedBytes() {
        return encodedBytes;
    }

    /**
     * Directly sets the 64-bit encoded image bytes member.
     *
     * @param encodedBytes Image bytes encoded to a 64-bit String.
     */
    public void setEncodedBytes(String encodedBytes) {
        this.encodedBytes = encodedBytes;
    }

    /**
     * @return Decoded image bytes.
     */
    public byte[] getBytes() {
        return Base64.getDecoder().decode(encodedBytes);
    }

    /**
     * Encodes and saved passed image bytes.
     *
     * @param bytes Image bytes.
     */
    public void setBytes(byte[] bytes) {
        encodedBytes = Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Custom toString implementation.
     *
     * @return Custom string for debug output.
     */
    @Override
    public String toString() {
        return "TransformedImage{" +
                "imageName='" + imageName + '\'' +
                "transformName='" + transformName + '\'' +
                ", encodedBytes='" + encodedBytes + '\'' +
                '}';
    }
}
