package web;

import org.junit.Test;

public class RemoteDataSourceTest {
    public static final String BASE_URL = "http://localhost:8081";

    @Test
    public void testTransformedImage() {
        RemoteDataSource remoteDataSource = new RemoteDataSource(BASE_URL);
        remoteDataSource.applyTransforms();
    }
}