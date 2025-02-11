package bdd.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;

/**
 * Provides utility methods for common testing operations such as waiting,
 * generating future dates, and creating random API keys. This class is
 * designed to support various testing scenarios where simulated time delays,
 * date calculations, and unique identifiers are needed.
 */
public class TestingUtilities {

    private TestingUtilities() {
    }

    private final static Logger log = LoggerFactory.getLogger(TestingUtilities.class);


    /**
     * Pauses the current thread for a specified number of seconds.
     *
     * @param sec The number of seconds to wait.
     */
    public static void wait(int sec) {
        try {
            Thread.sleep(sec * 1000L);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Generates a future date as a string, based on a specified number of days from today.
     *
     * @param days The number of days to add to the current date.
     * @return A string representation of the future date in the format YYYY-MM-DD.
     */
    public static String generateFutureDate(int days) {
        LocalDate date = LocalDate.now().plusDays(days);
        return date.toString();
    }


    /**
     * Generates a random API key using a secure random number generator and encodes it as a Base64 string.
     *
     * @return A random Base64-encoded string that can be used as an API key.
     */
    public static String generateRandomApiKey() {
        int apiKeyLength = 32;
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[apiKeyLength];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
