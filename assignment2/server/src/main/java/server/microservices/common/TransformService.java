package server.microservices.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import server.common.ImageUtils;
import server.common.model.TransformedImage;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static server.common.Constants.Service.*;

import javax.validation.constraints.NotNull;

/**
 * This class defines the {@link TransformService#applyTransform}
 * method called by the {@link TransformController}.  The contained
 * method is used to perform an image transform operation.
 * <p>
 * This class is annotated as a Spring {@code @Service}, which enables
 * the auto-detection and wiring of dependent implementation classes
 * via classpath scanning.
 */
@Service
public class TransformService {
    /**
     * This auto-wired field connects the {@link TransformService} to
     * a {@link Transforms} class containing algorithms that perform
     * various image transforms.
     */
    @Autowired
    Transforms transforms;

    /**
     * Map transform names to a {@link Function} that performs the transform.
     */
    protected final Map<String, Function<BufferedImage, Void>> mTransformMap =
        new HashMap<>();

    /**
     * Constructor initializes the field.
     */
    TransformService() {
        // Initialize the contents of mTransformMap with the appropriate values.
        // TODO -- you fill in here.
    }

    /**
     * Applies the named transform to the passed byte array image and
     * returns the result as a {@link TransformedImage}.
     *
     * @param imageName     Image file name
     * @param transformName Transforms name to apply
     * @param imageBytes    Image bytes to transform
     * @return A {@link TransformedImage}
     */
    public TransformedImage applyTransform(String imageName,
                                           String transformName,
                                           byte[] imageBytes) {
        // Convert the imageBytes into a BufferedImage that contains
        // the image pixels and metadata about the image.
        BufferedImage bufferedImage = ImageUtils
            .toBufferedImage(imageBytes);

        // Use mTransformMap to
        // 1. Find the Function associated with the given
        //    transform and apply the transform to the buffered image.
        // 2. Create and return a new DTO transformed image.

        // TODO -- you fill in here, replacing return null with
        // the appropriate code.
        return null;
    }

    /**
     * Perform the grayscale transform.
     *
     * @param bufferedImage Contains metadata about the image
     */
    protected Void grayscaleTransform(BufferedImage bufferedImage) {
        // Reset the bufferedImage pixels to the transformed pixel array.
        setPixels(bufferedImage,
                  transforms.grayScale(getPixels(bufferedImage),
                                       bufferedImage.getColorModel().hasAlpha()));
        return null;
    }

    /**
     * Perform the sepia transform.
     *
     * @param bufferedImage Contains metadata about the image
     */
    protected Void sepiaTransform(BufferedImage bufferedImage) {
        // Reset the bufferedImage pixels to the transformed pixel array.
        setPixels(bufferedImage,
                  transforms.sepia(getPixels(bufferedImage),
                                   bufferedImage.getColorModel().hasAlpha()));
        return null;
    }

    /**
     * Perform the tint transform.
     *
     * @param bufferedImage Contains metadata about the image
     */
    protected Void tintTransform(BufferedImage bufferedImage) {
        // Reset the bufferedImage pixels to the transformed pixel array.
        setPixels(bufferedImage,
                  transforms.tint(getPixels(bufferedImage),
                                  bufferedImage.getColorModel().hasAlpha(),
                                  0.0f, 0.0f, 0.9f));
        return null;
    }

    /**
     * Helper method used to set the RGB values in the BufferedImage
     *
     * @param bufferedImage buffered image to be updated
     * @param pixels        Pixels to set
     */
    protected void setPixels(BufferedImage bufferedImage, int[] pixels) {
        bufferedImage
            .setRGB(0, 0,
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    pixels, 0,
                    bufferedImage.getWidth());
    }

    /**
     * Helper method used to get the BufferedImage pixels.
     *
     * @param bufferedImage BufferedImage to get pixels from
     * @return The image RGB pixels
     */
    @NotNull
    protected int[] getPixels(BufferedImage bufferedImage) {
        return bufferedImage
            .getRGB(0, 0,
                    bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    null, 0,
                    bufferedImage.getWidth());
    }
}
