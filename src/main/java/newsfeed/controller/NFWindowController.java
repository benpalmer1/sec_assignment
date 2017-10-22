package newsfeed.controller;

import java.awt.event.ActionEvent;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.Timer;
import java.util.logging.Logger;

import newsfeed.view.*;
import newsfeed.model.*;

public class NFWindowController
{
    // Reference to the window
    private NFWindow window = null;
    
    private boolean running = true;
    private Timer timeTimer;
    private ArrayList<Plugin> pluginList;
    private NFPluginScheduler pluginScheduler;
    
    public void setWindow(NFWindow window)
    {
        this.window = window;
    }
    
    public void setPluginScheduler(NFPluginScheduler pluginScheduler)
    {
        this.pluginScheduler = pluginScheduler;
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
    // mutexes
    // add a task to the 
    
    public void initPlugins(String[] plugins)
    {
        Runnable initPlugins = () ->
        {
            if(plugins.length == 0)
            {
               // window.showError("No plugins specified. Specify one or more newsfeed plugins in initial command line arguments.");
               // stop();
               // System.exit(0);
                pluginList = new ArrayList<>();
                Plugin newPlugin = Plugin.loadClassFromJarFile("test_subproject.jar");
                newPlugin.setWindowController(this);
                if(newPlugin != null)
                {
                    pluginList.add(newPlugin);
                    pluginScheduler.addPlugin(newPlugin);
                    logInfo("Add new plugin: " + newPlugin.getSource());
                }
                else
                {
                    window.showError("Error: Cannot instantiate plugin class.");
                }
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
                        pluginScheduler.addPlugin(newPlugin);
                        logInfo("Add new plugin: " + newPlugin.getSource());
                    }
                    else
                    {
                        window.showError("Error: Cannot instantiate plugin class.");
                    }
                }
            }
        };
        initPlugins.run();
    }
    
    public synchronized static void logException(String logString, Exception e)
    {
        Runnable logTask = () -> { 
            try
            {
                Handler handler = new FileHandler("error.log", true);
                Logger logger = Logger.getLogger("newsfeed.exception");
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.log(Level.SEVERE, logString, e);
                handler.close();
            }
            catch (IOException ex)
            {
                System.err.println("Error: Unable to log exception message. Message: " + logString);
            }
        };
        new Thread(logTask).start();
    }
    
    public synchronized static void logInfo(String logString)
    {
        Runnable logTask = () -> { 
            try
            {
                Handler handler = new FileHandler("info.log", true);
                Logger logger = Logger.getLogger("newsfeed.info");
                logger.setLevel(Level.ALL);
                logger.addHandler(handler);
                logger.log(Level.INFO, logString);
                handler.close();
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
        pluginScheduler.stopAll();
        running = false;
        timeTimer.stop();
        window.setVisible(false);
    }

    public void update()
    {
        pluginScheduler.updateAllNow();
        logInfo("Updated all active plugins.");
    }
    
    public void cancelAllRunning()
    {
        pluginScheduler.cancelAllRunning();
    }
}