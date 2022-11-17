package edu.vanderbilt.imagecrawler.web;

import static io.reactivex.rxjava3.schedulers.Schedulers.io;
import static reactor.core.scheduler.Schedulers.parallel;

import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.reactor.ReactorCallAdapterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import edu.vanderbilt.imagecrawler.crawlers.ImageCrawler;
import edu.vanderbilt.imagecrawler.utils.IOUtils;
import edu.vanderbilt.imagecrawler.utils.Image;
import edu.vanderbilt.imagecrawler.utils.TransformException;
import edu.vanderbilt.imagecrawler.utils.TransformedImage;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import reactor.core.publisher.Mono;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Remote data source adapter that builds an HTTP client for {@link
 * TransformApi} API calls.
 */
public class RemoteDataSource {
    /**
     * Http request timeouts.
     */
    public static final Duration DEFAULT_TIMEOUT =
        Duration.ofMinutes(30);

    /**
     * API instances.
     */
    private final TransformApi api;

    /**
     * Retrofit instance.
     */
    private final Retrofit retrofit;

    /**
     * Project Reactor adapter needed to obtain info from the server.
     */
    private static final ReactorCallAdapterFactory fluxAdapter =
        ReactorCallAdapterFactory.createWithScheduler(parallel());

    /**
     * RxJava adapter needed to obtain info from the server.
     */
    private static final RxJava3CallAdapterFactory rxAdapter =
        RxJava3CallAdapterFactory.createWithScheduler(io());

    /**
     * The constructor initializes the fields.
     */
    public RemoteDataSource(String baseUrl) {
        retrofit = getRetrofit(baseUrl);
        api = retrofit.create(TransformApi.class);
    }

    /**
     * @return Api instance.
     */
    public TransformApi getApi() {
        return api;
    }

    public List<TransformedImage> applyTransforms() {
        return null;
    }

    /**
     * Builds a {@link MultipartBody.Part} containing the passed image bytes.
     *
     * @param imageCrawler The image crawler instance.
     * @param image        The base image to be transformed.
     * @return A {@link MultipartBody.Part} instance for an applyTransforms POST.
     */
    public MultipartBody.Part buildMultipartBodyPart(
        ImageCrawler imageCrawler,
        Image image) {

        // Get an input stream for the source image.
        byte[] bytes;
        try (InputStream inputStream = imageCrawler
            .mapUriToInputStream(image.getSourceUrl().toString())) {

            // Get the image bytes from the input stream.
            bytes = IOUtils.toBytes(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Get the image file name (key).
        String fileName = image.getFileName();

        // Build a multipart request body containing the image bytes.
        RequestBody requestBody = RequestBody
            .create(bytes, MediaType.parse("multipart/form-data"));

        // Create the request part for the image bytes request body.
        return MultipartBody.Part
            .createFormData("image", fileName, requestBody);
    }

    /**
     * @return An OkHttpClient that supports token authentication.
     */
    private static OkHttpClient buildHttpClient() {
        // Create a custom logging interceptor.
        HttpLoggingInterceptor httpLoggingInterceptor =
            new HttpLoggingInterceptor();
        httpLoggingInterceptor
            .setLevel(HttpLoggingInterceptor.Level.HEADERS);

        // Build the HTTP client using with the custom logging
        // interceptor.
        return new OkHttpClient
            .Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(DEFAULT_TIMEOUT)
            .readTimeout(DEFAULT_TIMEOUT)
            .writeTimeout(DEFAULT_TIMEOUT)
            .build();
    }

    /**
     * Creates a new Retrofit instance using the passed baseUrl.
     *
     * @param baseUrl Target server URL.
     * @return A Retrofit instance.
     */
    private static Retrofit getRetrofit(String baseUrl) {
        return new Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(buildHttpClient())
            .addCallAdapterFactory(rxAdapter)
            .addCallAdapterFactory(fluxAdapter)
            .addConverterFactory(GsonConverterFactory
                .create(new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ")
                    .setLenient()
                    .create()))
            .build();
    }

    /**
     * Calls remote API to apply the requested transforms on the
     * specified image.
     *
     * @param imageCrawler   The image crawler instance
     * @param image          The {@link Image} to transform remotely
     * @param transformNames List of transformed images
     * @param parallel       True if transforms should be applied in
     *                       parallel, else false
     * @return A {@link List} of {@link TransformedImage} objects
     */
    public List<TransformedImage> applyTransforms(
        ImageCrawler imageCrawler,
        Image image,
        List<String> transformNames,
        Boolean parallel
    ) {
        Response<List<TransformedImage>> response;

        try {
            response =
                api.applyTransforms(
                    transformNames,
                    buildMultipartBodyPart(imageCrawler, image),
                    parallel
                ).execute();

            // Check if request was successful.
            if (response.isSuccessful()) {
                // Return the transformed images.
                return response.body();
            } else {
                // Build a custom TransformException that wraps the
                // error body (a string) and then throw.
                throw new TransformException(
                    Objects.requireNonNull(response.errorBody()).string());
            }
        } catch (IOException e) {
            // An unexpected problem has occurred taking
            // to server (ConnectionException, etc.).
            throw new RuntimeException(e);
        }
    }

    /**
     * API interface used by Retrofit to create an instance that can
     * then be used for all server API calls.
     */
    public interface TransformApi {
        String APPLY_TRANSFORMS = "/apply-transforms";

        /**
         * Asynchronously applies all passed transforms to the base
         * image.
         * <p>
         * The @Multipart annotation is used to indicate that this API
         * call will contain a multipart body.
         * <p>
         * The @POST annotation is required for any API call that
         * sends a data in the request body using the @Multipart
         * format.
         *
         * @param transforms The transforms to perform on the passed image
         * @param image      The image to transform
         * @param parallel   True if transforms should be applied in
         *                   parallel, else false
         * @return A {@link Mono} that emits a {@link List} of {@link TransformedImage} objects
         */
        @Multipart
        @POST(APPLY_TRANSFORMS)
        Mono<List<TransformedImage>>
        applyReactorTransforms(@Query("transforms") List<String> transforms,
                               @Part MultipartBody.Part image,
                               @Query("parallel") Boolean parallel);

        /**
         * Asynchronously applies all passed transforms to the base
         * image.
         * <p>
         * The @Multipart annotation is used to indicate that this API
         * call will contain a multipart body.
         * <p>
         * The @POST annotation is required for any API call that
         * sends a data in the request body using the @Multipart
         * format.
         *
         * @param transforms The transforms to perform on the passed image
         * @param image      The image to transform
         * @param parallel   True if transforms should be applied in
         *                   parallel, else false
         * @return A {@link Single} that emits a {@link List} of {@link TransformedImage} objects
         */
        @Multipart
        @POST(APPLY_TRANSFORMS)
        Single<List<TransformedImage>>
        applyRxJavaTransforms(@Query("transforms") List<String> transforms,
                              @Part MultipartBody.Part image,
                              @Query("parallel") Boolean parallel);

        /**
         * Asynchronously applies all passed transforms to the base
         * image.
         * <p>
         * The @Multipart annotation is used to indicate that this API
         * call will contain a multipart body.
         * <p>
         * The @POST annotation is required for any API call that
         * sends a data in the request body using the @Multipart
         * format.
         *
         * @param transforms The transforms to perform on the passed image
         * @param image      The image to transform
         * @param parallel   True if transforms should be applied in
         *                   parallel, else false
         * @return A {@link List} of {@link TransformedImage} objects.
         */
        @Multipart
        @POST(APPLY_TRANSFORMS)
        Call<List<TransformedImage>>
        applyTransforms(@Query("transforms") List<String> transforms,
                        @Part MultipartBody.Part image,
                        @Query("parallel") Boolean parallel);
    }
}
