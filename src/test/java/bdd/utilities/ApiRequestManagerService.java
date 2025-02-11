package bdd.utilities;

import bdd.enums.HttpMethod;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;

/**
 * Handles setting up and making API requests. It uses RestAssured to make web requests easier. This class lets you send
 * different types of web requests like GET, POST, PUT, and DELETE. You can customize these requests with specific URLs,
 * data to send, and authentication keys.
 */

public class ApiRequestManagerService implements ApiRequestService {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestManagerService.class);

    private ThreadLocal<Response> threadLocalResponse = ThreadLocal.withInitial(() -> null);

    /**
     * Retrieves the base URI for the data bridge service from configuration.
     *
     * @return The base URI as a string.
     * @throws IllegalArgumentException If the base URI is not configured or is invalid.
     */
    @Step
    public String getBaseUri() {

        String baseURI = ConfigManager.getProperty("baseURI");

        if (baseURI == null) {
            String errorMessage = "Invalid environment provided: " + ConfigManager.getEnvironment();
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.info("baseURI is : {}", baseURI);
        return baseURI;
    }

    /**
     * Performs an HTTP request with the specified method, endpoint, path parameters, query parameters, payload, and API
     * key.
     *
     * @param method      The HTTP method to use for the request.
     * @param endpoint    The endpoint URL relative to the base URI.
     * @param pathParams  A list of path parameters to include in the URI.
     * @param queryParams A map of query parameters to append to the URI.
     * @param payload     The request payload (for POST and PUT requests).
     * @param apiKey      The API key for authentication.
     * @return The response from the API as a {@link Response} object.
     */
    public Response performHttpRequest(HttpMethod method, String endpoint, List<String> pathParams,
                                       Map<String, String> queryParams, String payload, String apiKey) {
        Map<String, Object> queryParamsObject = new HashMap<>(queryParams);
        return performRequest(method.name(), endpoint, pathParams, queryParamsObject, payload, apiKey);
    }

    private Response performRequest(String method, String endpoint, List<String> pathParams,
                                    Map<String, Object> queryParams, Object payload, String apiKey) {
        String uri = buildURLWithParams(endpoint, pathParams);
        String environment = ConfigManager.getEnvironment();

        RequestSpecification requestSpec = given()
                .contentType(ContentType.JSON)
                .headers("x-api-key", apiKey);

//        if (environment.equalsIgnoreCase("dev")) {
//            // For non-prod environments, relax SSL validation
//            requestSpec.config(RestAssured.config()
//                    .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation())
//                    .logConfig(LogConfig.logConfig().blacklistHeader("x-api-key")));
//        } else {
//            // For prod environment, use standard SSL configuration without relaxed validation
        requestSpec.config(RestAssured.config()
                .logConfig(LogConfig.logConfig().blacklistHeader("x-api-key")));
//        }
//        }
        if (queryParams != null) {
            requestSpec.queryParams(queryParams);
        }
        if (payload != null) {
            requestSpec.body(payload);
        }

        switch (method.toUpperCase()) {
            case "GET":
                threadLocalResponse.set(requestSpec.when().get(uri));
                break;
            case "POST":
                threadLocalResponse.set(requestSpec.when().post(uri));
                break;
            case "PUT":
                threadLocalResponse.set(requestSpec.when().put(uri));
                break;
            case "DELETE":
                threadLocalResponse.set(requestSpec.when().delete(uri));
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
        return threadLocalResponse.get();
    }

    // Overloaded GET, POST, PUT, DELETE methods to use performRequest
    @Step
    @Override
    public Response GET(String endpoint, List<String> pathParams, String apiKey) {
        return performRequest("GET", endpoint, pathParams, null, null, apiKey);
    }

    @Step
    public Response GET(String endpoint, List<String> pathParams, Map<String, Object> queryParam, String apiKey) {
        return performRequest("GET", endpoint, pathParams, queryParam, null, apiKey);
    }

    @Step
    @Override
    public Response POST(String endpoint, List<String> pathParams, String apiKey) {
        return performRequest("POST", endpoint, pathParams, null, null, apiKey);
    }

    @Step
    public Response POST(String endpoint, List<String> pathParams, Object payload, String apiKey) {
        return performRequest("POST", endpoint, pathParams, null, payload, apiKey);
    }

    @Step
    public Response POST(String endpoint, List<String> pathParams, Map<String, Object> queryParam, Object payload,
                         String apiKey) {
        return performRequest("POST", endpoint, pathParams, queryParam, payload, apiKey);
    }

    @Step
    public Response POST(String endpoint, List<String> pathParams, Map<String, Object> payload, String apiKey) {
        return performRequest("POST", endpoint, pathParams, null, payload, apiKey);
    }

    @Step
    public Response POST(String endpoint, List<String> pathParams, Map<String, Object> queryParam,
                         Map<String, Object> payload, String apiKey) {
        return performRequest("POST", endpoint, pathParams, queryParam, payload, apiKey);
    }

    @Step
    @Override
    public Response PUT(String endpoint, List<String> pathParams, String apiKey) {
        return performRequest("PUT", endpoint, pathParams, null, null, apiKey);
    }

    @Step
    public Response PUT(String endpoint, List<String> pathParams, Object payload, String apiKey) {
        return performRequest("PUT", endpoint, pathParams, null, payload, apiKey);
    }

    @Step
    public Response PUT(String endpoint, List<String> pathParams, Map<String, Object> queryParam, Object payload,
                        String apiKey) {
        return performRequest("PUT", endpoint, pathParams, queryParam, payload, apiKey);
    }

    @Step
    public Response PUT(String endpoint, List<String> pathParams, Map<String, Object> payload, String apiKey) {
        return performRequest("PUT", endpoint, pathParams, null, payload, apiKey);
    }

    @Step
    public Response PUT(String endpoint, List<String> pathParams, Map<String, Object> queryParam,
                        Map<String, Object> payload, String apiKey) {
        return performRequest("PUT", endpoint, pathParams, queryParam, payload, apiKey);
    }

    @Step
    @Override
    public Response DELETE(String endpoint, List<String> pathParams, String apiKey) {
        return performRequest("DELETE", endpoint, pathParams, null, null, apiKey);
    }

    @Step
    public Response DELETE(String endpoint, List<String> pathParams, Object payload, String apiKey) {
        return performRequest("DELETE", endpoint, pathParams, null, payload, apiKey);
    }

    @Step
    public Response DELETE(String endpoint, List<String> pathParams, Map<String, Object> queryParam, Object payload,
                           String apiKey) {
        return performRequest("DELETE", endpoint, pathParams, queryParam, payload, apiKey);
    }

    @Step
    public Response DELETE(String endpoint, List<String> pathParams, Map<String, Object> payload, String apiKey) {
        return performRequest("DELETE", endpoint, pathParams, null, payload, apiKey);
    }

    @Step
    public Response DELETE(String endpoint, List<String> pathParams, Map<String, Object> queryParam,
                           Map<String, Object> payload, String apiKey) {
        return performRequest("DELETE", endpoint, pathParams, queryParam, payload, apiKey);
    }

    /**
     * Constructs the full URI for a request using the specified endpoint and path parameters.
     *
     * @param endpoint   The endpoint URL relative to the base URI.
     * @param pathParams A list of path parameters to include in the URI.
     * @return The fully constructed URI as a string.
     */

    private String buildURLWithParams(String endpoint, List<String> pathParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUri() + endpoint);
        if (pathParams != null && !pathParams.isEmpty()) {
            return builder.buildAndExpand(pathParams.toArray()).toUriString();
        }
        return builder.toUriString();
    }

    private String buildURLWithParams(String endpoint) {
        return UriComponentsBuilder.fromHttpUrl(getBaseUri() + endpoint).toUriString();
    }

    private List<String> buildPathParams(String... args) {
        return Arrays.asList(args);
    }

}
