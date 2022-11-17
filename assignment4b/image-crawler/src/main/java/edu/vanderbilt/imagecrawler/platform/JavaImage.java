package edu.vanderbilt.imagecrawler.platform;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import edu.vanderbilt.imagecrawler.transforms.Transform;
import edu.vanderbilt.imagecrawler.common.Filters;
import kotlin.Unit;

import static edu.vanderbilt.imagecrawler.platform.Cache.Operation.TRANSFORM;

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Java platform.
 */
public class JavaImage
       implements PlatformImage {
    /**
     * Cache item used to report progress.
     */
    private Cache.Item mCacheItem;

    /**
     * The Bitmap our Image stores.
     */
    private BufferedImage mImage;

    /**
     * Size of image.
     */
    private int mSize = 0;

    /**
     * Package only constructor only accessed by Platform.
     */
    JavaImage(InputStream inputStream, Cache.Item item) {
        setImage(inputStream, item);
    }

    /**
     * Private constructor only accessed internally by this class.
     */
    private JavaImage(BufferedImage image) {
        mImage = image;
    }

    /**
     * Decodes a input stream into an @a Image that can be used in the rest
     * of the application.
     */
    @Override
    public void setImage(InputStream inputStream, Cache.Item item) {
        try {
            mSize = inputStream.available();
            mImage = ImageIO.read(inputStream);
            mCacheItem = item;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the {@link BufferedImage} to the {@link OutputStream}.
     *
     * @param outputStream The {@link OutputStream} to write
     *                     the {@link BufferedImage} to
     */
    @Override
    public void writeImage(OutputStream outputStream) throws IOException {
        if (mImage == null) {
            System.out.println("null image");
        } else {
            ImageIO.write(mImage,
                    "png",
                    outputStream);
        }
    }

    /**
     * Routes transform request to transform method.
     */
    @Override
    public PlatformImage applyTransform(Transform.Type type,
                                        Cache.Item item) {
        // Forward to the platform-specific implementation of this transform.
        BufferedImage originalImage = mImage;
        BufferedImage filteredImage =
                new BufferedImage
                        (originalImage.getColorModel(),
                                originalImage.copyData(null),
                                originalImage.getColorModel().isAlphaPremultiplied(),
                                null);

        int[] pixels = filteredImage.getRGB(
                0, 0, filteredImage.getWidth(), filteredImage.getHeight(),
                null, 0, filteredImage.getWidth());

        int[] lastProgress = new int[1];

        switch (type) {
            case GRAY_SCALE_TRANSFORM:
                Filters.grayScale(pixels, filteredImage.getColorModel().hasAlpha(), progress -> {
                    lastProgress[0] = updateProgress(mCacheItem, progress, lastProgress[0]);
                    return Unit.INSTANCE;
                });
            case TINT_TRANSFORM:
            case SEPIA_TRANSFORM:
                Filters.sepia(pixels, filteredImage.getColorModel().hasAlpha(), progress -> {
                    lastProgress[0] = updateProgress(mCacheItem, progress, lastProgress[0]);
                    return Unit.INSTANCE;
                });
                break;
            default:
                return this;
        }

        filteredImage.setRGB(
                0, 0, filteredImage.getWidth(), filteredImage.getHeight(),
                pixels, 0, filteredImage.getWidth());

        mCacheItem.progress(Cache.Operation.CLOSE, 1f);

        return new JavaImage(filteredImage);
    }

    /**
     * @return Size of image.
     */
    @Override
    public int size() {
        return mSize;
    }

    /**
     * @return The associated cache item.
     */
    @Override
    public Cache.Item getCacheItem() {
        return mCacheItem;
    }

    /**
     * Sets the current action progress to the passed value.
     *
     * @param item The item whose progress value is to be set.
     * @param progress A progress Float value from 0 to 1.
     * @param lastProgress The last progress value used to
     *                     insure progress is increasing.
     * @return The new current progress value as a percent.
     */
    private int updateProgress(Cache.Item item, float progress, int lastProgress) {
        int percent = (int) (progress * 100);
        if (percent > lastProgress) {
            item.progress(TRANSFORM, progress);
        }
        return percent;
    }
}
