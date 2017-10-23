package newsfeed.controller;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
    
    private Timer timeTimer;
    private ArrayList<Plugin> pluginList;
    private NFPluginScheduler pluginScheduler;
    
    private final List<Headline> currentHeadlines;
    private final LinkedBlockingQueue<List<Headline>> headlineUpdateQueue;
    private final ExecutorService headlineUpdateExecutor;
    
    private final LinkedBlockingQueue<String> downloadAddQueue;
    private final ExecutorService downloadUpdateExecutor;
    
    public NFWindowController()
    {
        currentHeadlines = Collections.synchronizedList(new ArrayList<>());
        headlineUpdateExecutor = Executors.newSingleThreadExecutor();
        headlineUpdateQueue = new LinkedBlockingQueue<>();
        
        downloadUpdateExecutor = Executors.newSingleThreadExecutor();
        downloadAddQueue = new LinkedBlockingQueue<>();
    }
    
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
    
    public void initPlugins(String[] plugins)
    {
        Runnable initPlugins = () ->
        {
            if(plugins.length == 0)
            {
               // window.showError("No plugins specified. Specify one or more newsfeed plugins in initial command line arguments.");
               // stop();
               // System.exit(0);
                
                // Test code:
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
            catch (IOException e)
            {
                System.err.println("Error: Unable to log info message. Message: " + logString);
            }
        };
        new Thread(logTask).start();
    }
    
    /** Method to update the headlines for a specific source.
    * Method is called by the newsfeed plugin to update the current headlines of the specific site.
    * Queues the update to the updateQueue for updating when the updateExector is available. 
    * Prevents articles of the same name being listed also as some sites may contain duplicate headings. i.e previews.
    **/
    public void updateHeadlines(List<Headline> updatedHeadlines)
    {
        headlineUpdateQueue.add(updatedHeadlines);
        headlineUpdateExecutor.execute(() ->
        {
            try
            {
                synchronized(currentHeadlines)
                {
                    List<Headline> hUpdate = headlineUpdateQueue.take();
                    // Two checks to update the main list of headlines.
                    // 1 - Has the headline already been downloaded? If not, add to the list of headlines.
                    // 2 - Has a headline been deleted from the list of headlines? If so, delete the headline.
                    boolean alreadyDownloaded = false;
                    boolean wasRemoved = true;
                    
                    ArrayList<Headline> headlinesToRemove = new ArrayList<>();
                    ArrayList<Headline> headlinesToAdd = new ArrayList<>();

                    for(Headline newHeadline : updatedHeadlines)
                    {
                        alreadyDownloaded = false;
                        for(Headline currentHeadline : currentHeadlines)
                        {
                            wasRemoved = true;
                            if((newHeadline.getSourceURL().equals(currentHeadline.getSourceURL())) && (newHeadline.getHeadline().equals(currentHeadline.getHeadline())))
                            {   // Check if the headline was already downloaded.
                                alreadyDownloaded = true;
                            }
                            for(Headline newHeadlineB : updatedHeadlines) // Check if the headline was removed.
                            {
                                if(newHeadlineB.getSourceURL().equals(currentHeadline.getSourceURL()) && newHeadlineB.getHeadline().equals(currentHeadline.getHeadline()))
                                {
                                    wasRemoved = false;
                                }
                            }
                            if(wasRemoved)
                            {
                                window.deleteHeadline(currentHeadline.toString());
                                headlinesToRemove.add(currentHeadline);
                            }
                        }
                        if(!alreadyDownloaded)
                        {
                            window.addHeadline(newHeadline.toString());
                            headlinesToAdd.add(newHeadline);
                        }
                    }
                    currentHeadlines.removeAll(headlinesToRemove);
                    currentHeadlines.addAll(headlinesToAdd);
                }
            }
            catch(InterruptedException e)
            {
                logException("Error: GUI headline update action interrupted.", e);
                window.showError("Error: GUI headline update action interrupted.");
            }
        });
    }
    
    public void addDownload(String source) 
    {
        downloadAddQueue.add(source);
        
        downloadUpdateExecutor.execute(() ->
        {
            try
            {
                if(!window.isLoading())
                {
                    window.startLoading();
                }
                String toAdd = downloadAddQueue.take();
                window.addDownload(toAdd);
            } catch (InterruptedException e)
            {
                logException("Error: Add download to GUI action interrupted.", e);
                window.showError("Error: Add download to GUI action interrupted.");
            }
        });
    }
    
    public void deleteDownload(String source)
    {
        downloadUpdateExecutor.execute(() ->
        {
            if(downloadAddQueue.size() == 0 && window.isLoading())
            {
                window.stopLoading();
            }
            window.deleteDownload(source);
        });
    }
    
    public void stop() throws InterruptedException
    {
        headlineUpdateExecutor.shutdown();
        headlineUpdateExecutor.awaitTermination(5, TimeUnit.SECONDS);
        pluginScheduler.stopAll();
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