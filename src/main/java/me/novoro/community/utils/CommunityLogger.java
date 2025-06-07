package me.novoro.community.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seam's Logger. It's not recommended to use this externally.
 */
public class CommunityLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("Seam");

    /**
     * Sends an info log to console.
     * @param s The string to log.
     */
    public static void info(String s) {
        CommunityLogger.LOGGER.info("{}{}", "[Seam]: ", s);
    }

    /**
     * Sends a warn log to console.
     * @param s The string to log.
     */
    public static void warn(String s) {
        CommunityLogger.LOGGER.warn("{}{}", "[Seam]: ", s);
    }

    /**
     * Sends an error log to console.
     * @param s The string to log.
     */
    public static void error(String s) {
        CommunityLogger.LOGGER.error("{}{}", "[Seam]: ", s);
    }

    /**
     * Prints a stacktrace using Seam's Logger.
     * @param throwable The exception to print.
     */
    public static void printStackTrace(Throwable throwable) {
        CommunityLogger.error(throwable.toString());
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement traceElement : trace) CommunityLogger.error("\tat " + traceElement);
    }
}
