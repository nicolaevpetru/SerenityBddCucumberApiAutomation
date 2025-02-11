package bdd.utilities;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages the loading and accessing of configuration properties for the application.
 * It supports encrypted property values for secure storage of sensitive information.
 * The class initializes by loading a base properties file and then an environment-specific properties file, based on the current environment setting.
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static final Properties properties = new EncryptableProperties(createTextEncryptor());

    static {
        loadProperties("automation-config.properties");
        loadEnvironmentSpecificProperties();
    }


    /**
     * Creates a text encryptor for decrypting property values. The encryptor's configuration,
     * including its password and algorithm, is derived from system properties.
     */
    private static StandardPBEStringEncryptor createTextEncryptor() {
        StandardPBEStringEncryptor textEncryptor = new StandardPBEStringEncryptor();
        textEncryptor.setAlgorithm("PBEWithMD5AndDES");
        textEncryptor.setPassword(System.getProperty("jasypt.encryptor.password"));
        return textEncryptor;
    }


    /**
     * Loads properties from the specified file path. If the file is not found
     * in the classpath, an {@link IOException} is thrown.
     *
     * @param path The relative path to the properties file.
     */
    private static void loadProperties(String path) {
        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException("Property file '" + path + "' not found in the classpath");
            }
            properties.load(input);
            log.info("{} properties file successfully loaded.", path);
        } catch (IOException e) {
            log.error("Failed to load properties from file: {}", path, e);
        }
    }


    /**
     * Loads properties specific to the currently set environment. The environment
     * is determined by a system property 'env' or a default value.
     */
    private static void loadEnvironmentSpecificProperties() {
        String environment = getEnvironment();
        String envPropertiesFile = "properties/" + environment.toLowerCase() + ".properties";
        loadProperties(envPropertiesFile);
    }


    /**
     * Retrieves the current environment setting, defaulting to 'local' if not
     * explicitly set via the 'env' system property.
     *
     * @return The current environment name, uppercase.
     */
    public static String getEnvironment() {
        String environment = System.getProperty("env");
        if (environment == null || environment.trim().isEmpty()) {
            log.warn("Environment variable 'env' not found, using default environment from properties.");
            environment = properties.getProperty("environment", "local").toUpperCase().trim();
        }
        return environment;
    }


    /**
     * Retrieves the value of a specified property key from the loaded properties.
     *
     * @param key The property key to lookup.
     * @return The property value if found, otherwise {@code null}.
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }


    /**
     * Sets or updates the value of a specified property key.
     *
     * @param key The property key to set or update.
     * @param value The new value for the property.
     * @return The previous value of the property, or {@code null} if it was not previously set.
     */
    public static Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }
}
