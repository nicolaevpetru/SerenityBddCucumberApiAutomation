package bdd.steps;

import bdd.enums.HttpMethod;
import bdd.utilities.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import org.htmlunit.corejs.javascript.tools.shell.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static bdd.utilities.ConfigManager.getProperty;
import static bdd.utilities.TestingUtilities.generateRandomApiKey;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class defines steps for testing APIs using Cucumber. It includes methods for:
 * - Setting up query parameters from tables in test files.
 * - Creating JSON payloads for sending data to APIs, with the ability to fill in specific details dynamically.
 * - Making HTTP requests (like GET, POST, PUT, DELETE) to APIs, and using different types of authentication.
 * - Saving data from API responses for use in later test steps.
 * - Checking if the API's response code matches what's expected, to make sure the API is working correctly.
 * - Ensuring the API's response message is exactly what we expect, which helps us test for specific
 * errors or confirmations.
 * <p>
 * The goal of this class is to make it easy to perform and check the results of common actions
 * with APIs in a way that's straightforward and easy to manage in tests.
 */
public class CommonSteps {

    private static final Logger log = LoggerFactory.getLogger(CommonSteps.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Steps
    private ApiRequestManagerService apiRequestManager;
    private ThreadLocal<Response> response = ThreadLocal.withInitial(() -> null);

    private ThreadLocal<Map<String, String>> inputQueryParams = ThreadLocal.withInitial(HashMap::new);
    private ThreadLocal<String> updatedPayload = ThreadLocal.withInitial(() -> "");
    private ObjectStore objectStore = ObjectStore.getInstance();

    /**
     * Configures the query parameters for the upcoming HTTP request based on the provided DataTable.
     *
     * @param dataTable A Cucumber DataTable containing the query parameters as key-value pairs.
     */
    @When("the client configures the query parameters as follows:")
    public void theClientConfiguresTheQueryParametersAsFollows(DataTable dataTable) {
        Map<String, String> params = dataTable.asMap(String.class, String.class);
        Map<String, String> modifiedParams = new HashMap<>();

        Optional.ofNullable(params).orElse(Collections.emptyMap()).forEach((key, value) -> {
            String modifiedValue = modifyQueryParamValue(value);
            modifiedParams.put(key, modifiedValue);
        });

        inputQueryParams.get().putAll(modifiedParams);
    }

    private String modifyQueryParamValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.startsWith("{") && value.endsWith("}")) {
            String propertyKey = value.substring(1, value.length() - 1);
            return Optional.ofNullable(getProperty(propertyKey)).orElse("");
        } else if (value.startsWith("%") && value.endsWith("%")) {
            String key = value.substring(1, value.length() - 1);
            Object objectValue = ObjectStore.getInstance().getObject(key);
            return objectValue != null ? objectValue.toString() : "";
        }
        return value;
    }

    /**
     * Constructs a JSON payload for the HTTP request based on the provided payload string, replacing placeholders with
     * actual values.
     *
     * @param payload The JSON string representing the payload with placeholders.
     */
    @And("the client constructs a payload with the following specifications:")
    public void theClientConstructsAPayloadWithTheFollowingSpecifications(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            log.error("The payload is null or empty");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode;

        try {
            rootNode = objectMapper.readTree(payload);
            log.info(payload);
        } catch (IOException e) {
            log.error("Failed to parse the JSON payload", e);
            return;
        }

        try {
            replaceTemplateValues(rootNode, objectMapper);
        } catch (Exception e) {
            log.error("Failed to replace template values in the payload", e);
            return;
        }

        try {
            updatedPayload.set(objectMapper.writeValueAsString(rootNode));
        } catch (IOException e) {
            log.error("Failed to convert the updated JsonNode to a JSON string", e);
        }
    }

    /**
     * Initiates an HTTP request of the specified method to the given endpoint with authentication, using previously
     * configured query parameters and payload.
     *
     * @param httpMethod The HTTP method (GET, POST, PUT, DELETE).
     * @param endpoint   The endpoint URL to which the request will be sent.
     * @param username   The username to retrieve the associated API key for authentication.
     */
    @When("the client initiates a {string} request to {string} with {string} authentication")
    public void theClientInitiatesARequestToWithAuthentication(String httpMethod, String endpoint, String username) {
        HttpMethod method = HttpMethod.valueOf(httpMethod.toUpperCase());
        List<String> pathParams = extractPathParamsFromEndpoint(endpoint);
        String payload = getUpdatedPayload();
        Map<String, String> queryParams = inputQueryParams.get();

        String apiKey = retrieveApiKey(username);

        try {
            response.set(apiRequestManager.performHttpRequest(
                    method, endpoint, pathParams, queryParams, payload, apiKey
            ));
        } catch (Exception e) {
            log.error("Failed to send HTTP request", e);
        } finally {
            inputQueryParams.get().clear();
        }
    }

    @When("the client initiates a {string} request to {string} with API key {string}")
    public void theClientInitiatesARequestToWithAPIKey(String httpMethod, String endpoint, String apiKeyProperty) {
        HttpMethod method = HttpMethod.valueOf(httpMethod.toUpperCase());
        List<String> pathParams = extractPathParamsFromEndpoint(endpoint);
        String payload = getUpdatedPayload();
        Map<String, String> queryParams = inputQueryParams.get();

        // Retrieve API key from properties, if it exists; otherwise, generate a random one
        String apiKey = ConfigManager.getProperty(apiKeyProperty);
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = UUID.randomUUID().toString(); // Generate a random string for the API key if not found
            log.info("Generated a random API key for negative scenarios: {}", apiKey);
        } else {
            log.info("Using an API key from properties.");
        }

        try {
            response.set(apiRequestManager.performHttpRequest(method, endpoint, pathParams, queryParams, payload, apiKey));
        } catch (Exception e) {
            log.error("Failed to send HTTP request", e);
        } finally {
            inputQueryParams.get().clear();
        }
    }

    private String retrieveApiKey(String username) {
//        try {
//            ApiKeyInfo apiKeyInfo = ApiKeyStore.getApiKeyInfo(username.toUpperCase());
//            if (apiKeyInfo != null && apiKeyInfo.getApiKey() != null && !apiKeyInfo.getApiKey().trim().isEmpty()) {
//                return apiKeyInfo.getApiKey();
//            } else {
//                log.info("Generated a random apiKey for negative scenarios or new user.");
//                return generateRandomApiKey();
//            }
//        } catch (Exception e) {
//            log.error("Failed to retrieve apiKey", e);
//            return generateRandomApiKey();
//        }
        return null;
    }

    /**
     * Verifies that the response payload includes all fields specified in the DataTable.
     *
     * @param dataTable A Cucumber DataTable containing the expected fields.
     */
    @Then("the response payload should include the following fields:")
    public void theResponsePayloadShouldIncludeTheFollowingFields(DataTable dataTable) {
        // Convert dataTable to a List of Lists to check for actual content
        List<List<String>> raw = dataTable != null ? dataTable.asLists(String.class) : Collections.emptyList();

        // Check if the dataTable is effectively empty (no content or only empty strings)
        boolean isEffectivelyEmpty = raw.isEmpty() || raw.stream()
                .allMatch(list -> list == null
                        || list.isEmpty()
                        || list.stream().allMatch(s -> s == null || s.isEmpty()));

        if (isEffectivelyEmpty) {
            log.info("Skipping field validation as no fields are provided in the examples.");
            return;
        }

        try {
            boolean isArrayResponse = response.get().asString().trim().startsWith("[");

            List<Map<String, Object>> jsonResponseList = new ArrayList<>();
            if (isArrayResponse) {
                jsonResponseList = response.get().jsonPath().getList("$");
            } else {
                Map<String, Object> singleJsonResponse = response.get().jsonPath().getMap("$");
                jsonResponseList.add(singleJsonResponse);
            }

            List<String> fields = dataTable.asList();
            log.info("Fields: {}", fields);

            List<String> expectedFields = new ArrayList<>();
            if (!fields.isEmpty() && fields.get(0) != null) {
                if (fields.size() == 1 && fields.get(0).contains(",")) {
                    expectedFields.addAll(Arrays.asList(fields.get(0).split(",")));
                } else {
                    expectedFields.addAll(fields);
                }
                log.info("Expected fields: {}", expectedFields);

                for (Map<String, Object> jsonResponse : jsonResponseList) {
                    for (String field : expectedFields) {
                        assertTrue(
                                containsField(jsonResponse, field),
                                "The field " + field + " is not present in the response"
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while verifying the presence of fields in the response", e);
            throw e;
        }
    }


    private List<Map<String, Object>> prepareResponseList() {
        if (response.get().asString().trim().startsWith("[")) {
            List<Map> rawList = response.get().jsonPath().getList("$");
            return safeConvertListOfMaps(rawList);
        } else {
            Map<String, Object> singleMap = response.get().jsonPath().getMap("$");
            return Collections.singletonList(singleMap);
        }
    }

    private List<Map<String, Object>> safeConvertListOfMaps(List<Map> rawList) {
        List<Map<String, Object>> convertedList = new ArrayList<>();
        for (Map rawMap : rawList) {
            Map<String, Object> convertedMap = new HashMap<>();
            for (Object key : rawMap.keySet()) {
                if (key instanceof String) {
                    convertedMap.put((String) key, rawMap.get(key));
                } else {
                    throw new ClassCastException("Map key is not a String");
                }
            }
            convertedList.add(convertedMap);
        }
        return convertedList;
    }

    @Then("the user verifies that the response field {string} equals {string}")
    public void verifyFieldValueMatches(String fieldPath, String expectedValue) {
        if (response.get() == null) {
            fail("Response is null. Cannot verify field value.");
        }

        log.info("Verifying the response field '{}' against the expected value '{}'", fieldPath, expectedValue);

        try {
            String actualValue = response.get().jsonPath().getString(fieldPath);

            if (actualValue == null) {
                fail("The response field '" + fieldPath + "' was not found or is null in the JSON response.");
            }

            assertEquals("The field '" + fieldPath + "' value does not match the expected value.",
                    expectedValue, actualValue);

            log.info("Field '{}' matches the expected value: '{}'", fieldPath, expectedValue);
            SerenityReportLogger.logMessage(
                    "Verification Details - PASS",
                    "Verified that '" + fieldPath + "' = '" + expectedValue + "'"
            );

        } catch (AssertionError ae) {
            SerenityReportLogger.logMessage(
                    "Verification Details - FAILED",
                    "The step failed because: " + ae.getMessage()
            );
            throw ae;

        } catch (Exception e) {
            log.error("Error retrieving the field '{}' from the response.", fieldPath, e);
            SerenityReportLogger.logMessage(
                    "Verification Details - FAILED",
                    "The step failed due to an exception: " + e.getMessage()
            );
            throw e;
        }
    }

    /**
     * Resolves the expected value based on placeholder rules:
     * - If expectedValue starts with '{' and ends with '}', read from properties.
     * - If expectedValue starts with '%' and ends with '%', read from ObjectStore.
     * - Otherwise, use the expectedValue as is.
     */
    private String resolveExpectedValue(String expectedValue) {
        // Null or empty check
        if (expectedValue == null || expectedValue.trim().isEmpty()) {
            return expectedValue;
        }

        // {propertyKey} pattern
        if (expectedValue.startsWith("{") && expectedValue.endsWith("}")) {
            String propertyKey = expectedValue.substring(1, expectedValue.length() - 1);
            String propertyValue = ConfigManager.getProperty(propertyKey);
            if (propertyValue == null) {
                log.warn("No property found for key: {}", propertyKey);
                return "";
            }
            return propertyValue;
        }

        // %objectKey% pattern
        if (expectedValue.startsWith("%") && expectedValue.endsWith("%")) {
            String objectKey = expectedValue.substring(1, expectedValue.length() - 1);
            Object propertyValue = objectStore.getObject(objectKey);
            if (propertyValue instanceof String) {
                return (String) propertyValue;
            } else {
                log.warn("No object found for key: {}", objectKey);
                return "";
            }
        }
        // Regular value, no placeholders
        return expectedValue;
    }

    /**
     * Stores the value of a specified field from the response into the {@link ObjectStore} with a given key.
     * If the field is not present in the response, logs a warning. If the response is null, fails the test.
     *
     * @param fieldPath The JSON path to the field in the response whose value needs to be stored.
     * @param key       The key under which the value will be stored in the {@link ObjectStore}.
     */
    @Then("the value of the response field {string} is stored with the key {string}")
    public void theValueOfResponseFieldIsStoredWithTheKey(String fieldPath, String key) {
        Optional.ofNullable(response.get()).ifPresentOrElse(r -> {
            Object value = r.jsonPath().get(fieldPath);
            if (value != null) {
                objectStore.putObject(key, value.toString());
            } else {
                log.warn("Cannot read field by path: {}", fieldPath);
            }
        }, () -> fail("Response is null, cannot read fields"));
    }

    /**
     * Verifies that the HTTP response returned a specific status code. If the response is null, the test fails
     * immediately.
     *
     * @param expectedStatusCode The expected HTTP status code as a string.
     */
    @Then("the response should return a status code of {string}")
    public void theResponseShouldReturnAStatusCodeOf(String expectedStatusCode) {
        assertResponseNotNull();
        int expectedCode = Integer.parseInt(expectedStatusCode);
        int actualCode = response.get().getStatusCode();
        assertEquals(expectedCode, actualCode, "Status code does not match the expected value.");
    }

    /**
     * Compares the actual response message against an expected message. If the expected message is empty, skips the
     * verification. This method is useful for validating error messages or specific response notifications.
     *
     * @param expectedMessage The expected message string to compare against the actual response message.
     */
    @Then("the response message should match the expected message {string}")
    public void theResponseMessageShouldMatchTheExpectedMessage(String expectedMessage) {
        if (expectedMessage.trim().isEmpty()) {
            log.info("Skipping message verification as no expected message is provided.");
            return;
        }

        String actualMessage = response.get().jsonPath().getString("message");
        assertEquals(expectedMessage, actualMessage, "The actual response message does not match the expected message");
    }

    private void assertResponseNotNull() {
        if (response.get() == null) {
            fail("Response is null, cannot validate status code.");
        }
    }

    private boolean containsField(Map<String, Object> map, String field) {
        return map.containsKey(field)
                || map.values().stream().anyMatch(value ->
                (value instanceof Map && containsField((Map<String, Object>) value, field))
                        || (value instanceof List && ((List<?>) value).stream().anyMatch(
                        item -> item instanceof Map && containsField((Map<String, Object>) item, field)
                ))
        );
    }

    private List<String> extractPathParamsFromEndpoint(String endpoint) {
        return Arrays.stream(Optional.ofNullable(endpoint).orElse("").split("/"))
                .filter(part ->
                        (part.startsWith("{") && part.endsWith("}"))
                                || (part.startsWith("%") && part.endsWith("%"))
                )
                .map(part -> {
                    String paramKey = part.substring(1, part.length() - 1);
                    if (paramKey.isEmpty()) {
                        throw new IllegalArgumentException("Invalid input format for path parameter: " + part);
                    }
                    if (part.startsWith("{")) {
                        return getProperty(paramKey);
                    } else if (part.startsWith("%")) {
                        Object objectValue = ObjectStore.getInstance().getObject(paramKey);
                        return objectValue != null ? objectValue.toString() : "";
                    }
                    throw new IllegalArgumentException("Invalid input format for path parameter: " + part);
                }).collect(Collectors.toList());
    }

    private void replaceTemplateValues(JsonNode node, ObjectMapper objectMapper) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldNode = objectNode.get(fieldName);
                if (fieldNode.isTextual()) {
                    String fieldValue = fieldNode.textValue();
                    // {SOME_PROPERTY} placeholder
                    if (fieldValue.startsWith("{") && fieldValue.endsWith("}")) {
                        String propertyKey = fieldValue.substring(1, fieldValue.length() - 1);
                        String propertyValue = getProperty(propertyKey);
                        if (propertyValue != null) {
                            objectNode.put(fieldName, propertyValue);
                        } else {
                            log.warn("No property found for key: {}", propertyKey);
                        }
                    }
                    // %objectKey% placeholder
                    if (fieldValue.startsWith("%") && fieldValue.endsWith("%")) {
                        String propertyKey = fieldValue.substring(1, fieldValue.length() - 1);
                        Object propertyValue = objectStore.getObject(propertyKey);
                        if (propertyValue instanceof String) {
                            objectNode.put(fieldName, (String) propertyValue);
                        } else {
                            log.warn("No object found for key: {}", propertyKey);
                            propertyValue = getProperty(propertyKey);
                            if (propertyValue != null) {
                                objectNode.put(fieldName, (String) propertyValue);
                            }
                        }
                    }
                } else {
                    replaceTemplateValues(fieldNode, objectMapper);
                }
            });
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                replaceTemplateValues(arrayElement, objectMapper);
            }
        }
    }


    private String getUpdatedPayload() {
        return updatedPayload.get();
    }
}