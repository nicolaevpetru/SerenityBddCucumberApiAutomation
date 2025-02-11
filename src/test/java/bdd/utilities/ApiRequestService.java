package bdd.utilities;

import io.restassured.response.Response;
import java.util.List;

/**
 * Defines the contract for making HTTP requests using specific HTTP methods like GET, POST, PUT, and DELETE.
 * Intended to abstract the details of making HTTP requests and handling responses.
 */
public interface ApiRequestService {

    /**
     * Performs a GET request to the specified endpoint.
     *
     * @param endpoint The relative URL endpoint for the GET request.
     * @param pathParams A list of path parameters to include in the URL.
     * @param token The authentication token to be included in the request header.
     * @return The response from the API as a {@link Response} object.
     */
    Response GET(String endpoint, List<String> pathParams, String token);

    /**
     * Performs a POST request to the specified endpoint.
     *
     * @param endpoint The relative URL endpoint for the POST request.
     * @param pathParams A list of path parameters to include in the URL.
     * @param token The authentication token to be included in the request header.
     * @return The response from the API as a {@link Response} object.
     */
    Response POST(String endpoint, List<String> pathParams, String token);

    /**
     * Performs a PUT request to the specified endpoint.
     *
     * @param endpoint The relative URL endpoint for the PUT request.
     * @param pathParams A list of path parameters to include in the URL.
     * @param token The authentication token to be included in the request header.
     * @return The response from the API as a {@link Response} object.
     */
    Response PUT(String endpoint, List<String> pathParams, String token);

    /**
     * Performs a DELETE request to the specified endpoint.
     *
     * @param endpoint The relative URL endpoint for the DELETE request.
     * @param pathParams A list of path parameters to include in the URL.
     * @param token The authentication token to be included in the request header.
     * @return The response from the API as a {@link Response} object.
     */
    Response DELETE(String endpoint, List<String> pathParams, String token);
}

