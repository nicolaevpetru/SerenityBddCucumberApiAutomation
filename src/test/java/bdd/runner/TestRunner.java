package bdd.runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, json:target/cucumber.json, io.cucumber.core.plugin.SerenityReporter")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "bdd.steps")
// Temporarily remove the tag filter if you’re not using it in your feature files:
// @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Testing")
public class TestRunner {
}