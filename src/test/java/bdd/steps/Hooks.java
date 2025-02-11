package bdd.steps;

import bdd.utilities.ApiRequestManagerService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hooks {

    private final Logger log = LoggerFactory.getLogger(Hooks.class);


    @Before
    public void setUp(Scenario scenario) {
        log.info("### Starting scenario: {}", scenario.getName());
        ApiRequestManagerService apiRequestManagerService = new ApiRequestManagerService();

        // Setting up RestAssured base URI
        try {
            RestAssured.baseURI = apiRequestManagerService.getBaseUri();
            log.info("RestAssured Base URI set to: {}", RestAssured.baseURI);
        } catch (Exception e) {
            log.error("Error setting RestAssured Base URI", e);
        }

        // Establishing database connection
        // DatabaseUtility.getConnection();
    }

    @After
    public void tearDown(Scenario scenario) {
        log.info("### Finishing scenario: {}. Status - {}", scenario.getName(), scenario.getStatus());

//        // Reset RestAssured configuration
        RestAssured.reset();

    }
}


