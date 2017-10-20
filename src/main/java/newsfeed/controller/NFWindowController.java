package newsfeed.controller;

import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.Timer;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import newsfeed.view.*;
import newsfeed.model.*;

public class NFWindowController
{
    // Reference to the window
    private NFWindow window = null;
    
    private LinkedBlockingQueue<Path>   queue = new LinkedBlockingQueue<>();
    private ExecutorService executorService;
    
    private boolean running = true;
    private Timer timeTimer;
    private Logger logger;
        
    public NFWindowController()
    {
        initLogger();
        executorService = Executors.newFixedThreadPool(10);
    }

    public void setWindow(NFWindow window)
    {
        this.window = window;
    }
    
    /* Using another thread to set the time every minute so
    * that execution of the rest of the software is unaffectved by the clock. */
    public void startTimerThread()
    {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mma");
        Calendar initial = Calendar.getInstance();
        window.setTime(dateFormat.format(initial.getTime()));
        
        // Start update timer
        timeTimer = new Timer(1000, (ActionEvent e) ->
        {
            Calendar now = Calendar.getInstance();
            window.setTime(dateFormat.format(now.getTime()));
        });
        
        timeTimer.start();
    }   
    
    public NFWindow getWindow()
    {
        return this.window;
    }
    
    // Initialise the logger. All logging tasks are run in separate threads using the logException and logInfo handlers
    public void initLogger()
    {
        try
        {
            Handler handler = new FileHandler("error.log");
            logger = Logger.getLogger("newsfeed");
            logger.setLevel(Level.ALL);
            Logger.getLogger("").addHandler(handler);
        }
        catch(IOException e)
        {
            JOptionPane.showMessageDialog(null, "Error: Unable to start logger.");
            stop(); // Unable to log events, stop the program.
        }
    }
    
    public void logException(String logString)
    {
        Runnable logTask = () -> { logger.severe(logString); };
        new Thread(logTask).start();
    }
    
    public void logInfo(String logString)
    {
        Runnable logTask = () -> { logger.info(logString); };
        new Thread(logTask).start();
    }

    public void stop()
    {
        running = false;
        timeTimer.stop();
        executorService.shutdown();
        window.setVisible(false);
    }
}