package newsfeed.controller;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Event logging class. Contains only static methods for logging different event types.
 */

public class NFEventLogger
{
    public static synchronized void logException(String logString, Exception e)
    {
        Runnable logTask = () ->
        {
            try
            {
                Handler handler = new FileHandler("error.log", true);
                Logger logger = Logger.getLogger("newsfeed.exception");
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.log(Level.SEVERE, logString, e);
                handler.close();
            } catch (IOException ex)
            {
                System.err.println("Error: Unable to log exception message. Message: " + logString);
            }
        };
        new Thread(logTask).start();
    }

    public static synchronized void logInfo(String logString)
    {
        Runnable logTask = () ->
        {
            try
            {
                Handler handler = new FileHandler("info.log", true);
                Logger logger = Logger.getLogger("newsfeed.info");
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.log(Level.INFO, logString);
                handler.close();
            } catch (IOException e)
            {
                System.err.println("Error: Unable to log info message. Message: " + logString);
            }
        };
        new Thread(logTask).start();
    }
}
