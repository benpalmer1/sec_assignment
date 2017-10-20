package newsfeed.controller;

import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    
    private LinkedBlockingQueue<Path> queue = new LinkedBlockingQueue<>();
    private ExecutorService executorService;
    
    private boolean running = true;
    private Timer timeTimer;
    private ArrayList<Plugin> pluginList;
        
    public NFWindowController()
    {
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
        
        // Start update timer to update every minute.
        timeTimer = new Timer(60000, (ActionEvent e) ->
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
    
    public void initPlugins(String[] plugins)
    {
        if(plugins.length == 0)
        {
            window.showError("No plugins specified. Specify one or more newsfeed plugins in initial command line arguments.");
            stop();
            System.exit(0);
        }
        else    // Normal execution
        {
            pluginList = new ArrayList<>();
            for(String plugin : plugins)
            {
                Plugin newPlugin = Plugin.loadClassFromJarFile(plugin);
                if(newPlugin != null)
                {
                    pluginList.add(newPlugin);
                    logInfo("Add new plugin: " + newPlugin.getSource());
                }
                else
                {
                    window.showError("Error: Cannot instantiate plugin class.");
                }
            }
        }
    }
    
    public static void logException(String logString, Exception e)
    {
        Runnable logTask = () -> { 
            try
            {
                Handler handler = new FileHandler("error.log");
                Logger logger = Logger.getLogger("newsfeed");
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.log(Level.SEVERE, logString, e);
            }
            catch (IOException ex)
            {
                System.err.println("Error: Unable to log exception message. Message: " + logString);
            }
        };
        new Thread(logTask).start();
    }
    
    public static void logInfo(String logString)
    {
        Runnable logTask = () -> { 
            try
            {
                Handler handler = new FileHandler("info.log");
                Logger logger = Logger.getLogger("newsfeed");
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.log(Level.INFO, logString);
            }
            catch (IOException ex)
            {
                System.err.println("Error: Unable to log info message. Message: " + logString);
            }
        };
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