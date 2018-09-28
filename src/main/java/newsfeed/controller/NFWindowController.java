/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Controller class for the window of the application.
 * Abstracts the handling of managing various problems such as updating the lists,
 * initialising plugins and controlling events to be actioned on the window itself.
 */
package newsfeed.controller;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

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
    
    public NFWindowController()
    {
        currentHeadlines = Collections.synchronizedList(new ArrayList<>());
    }
    
    public void setWindow(NFWindow window)
    {
        this.window = window;
    }
    
    public void setPluginScheduler(NFPluginScheduler pluginScheduler)
    {
        this.pluginScheduler = pluginScheduler;
    }
    
    /** Method StartTimerThread - Initialises the timer text field on the window.
    ** Uses another thread to set the time every second, so
    ** that execution of the rest of the software is unaffected by the clock. */
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
    
    /** Static method getTime.
     * returns the current time as a formatted string. */
    public static String getTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mma");
        Calendar initial = Calendar.getInstance();
        return dateFormat.format(initial.getTime());
    }
    
    public NFWindow getWindow()
    {
        return this.window;
    }
    
    
    /** InitPlugins method - Creates a new thread to initialise all plugins.
    * New thread used to remove unrelated task from the GUI thread.
    */
    public void initPlugins(String[] plugins)
    {
        Runnable initPlugins = () ->
        {
            if(plugins.length == 0) // User didn't specify any command line arguments.
            {
                window.showError("No plugins specified. Specify one or more newsfeed plugins in initial command line arguments.");
                stopNewsfeed();
                System.exit(0);
            }
            else    // Normal execution
            {
                pluginList = new ArrayList<>();
                for(String plugin : plugins)
                {
                    if(plugin.endsWith(".jar"))
                    {
                        Plugin newPlugin = Plugin.loadClassFromJarFile(plugin);
                        if(newPlugin != null)
                        {
                            pluginList.add(newPlugin);
                            pluginScheduler.addPlugin(newPlugin);
                            NFEventLogger.logInfo("Add new plugin: " + newPlugin.getSource());
                        }
                        else
                        {
                            window.showError("Error: Cannot instantiate plugin class.");
                        }
                    }
                    else
                    {
                        window.showError("Error: Non-jar file specified.");
                        NFEventLogger.logInfo("Error: Non-jar file specified.");
                    }
                }
            }
        };
        initPlugins.run();
    }
    
    /** UpdateHeadlines method - Updates the headlines for a specific source.
    * Method is called by the respective plugin to update the current headlines of the specific site.
    * Prevents articles of the same name being listed also, as some sites may contain duplicate headings. i.e previews, ads, menus.
    **/
    public synchronized void updateHeadlines(List<Headline> updatedHeadlines)
    {
        Runnable headlineUpdate = () ->
        {
            synchronized(currentHeadlines)
            {
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
                        if(currentHeadline.getSourceURL().equals(updatedHeadlines.get(0).getSourceURL()) && wasRemoved)
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
        };
        
        SwingUtilities.invokeLater(headlineUpdate);
    }
    
    /** Add/Delete download methods.
     * Controller methods to modify the downloads list on the GUI. The tasks are 
     * added to the invokeLater events queue to make sure the list is updated without corruption.*/
    public void addDownload(String source) 
    {        
        Runnable addDownload = () ->
        {
            window.startLoading();
            window.addDownload(source);
        };
        
        SwingUtilities.invokeLater(addDownload);
    }
    public void deleteDownload(String source)
    {
        Runnable delDownload = () ->
        {
            window.deleteDownload(source);
            if(window.downloadsCount() == 0)
            {
                window.stopLoading();
            }
        };
        
        SwingUtilities.invokeLater(delDownload);
    }
    
    /** Main stop method for the entire application.
     * Calls the various required methods to stop execution of each
     * running part of the software. */
    public void stopNewsfeed()
    {
        try
        {
            pluginScheduler.stopAll();
        } catch (InterruptedException e)
        {
            NFEventLogger.logException("Error: ShutdownInterrupted.", e);
        }
        finally
        {
            NFEventLogger.logInfo("Newsfeed application terminated.");
            timeTimer.stop();
            window.setVisible(false);
            System.exit(0);
        }
    }

    public void update()
    {
        pluginScheduler.updateAllNow();
        NFEventLogger.logInfo("Updated all active plugins.");
    }
    
    public void cancelAllRunning()
    {
        pluginScheduler.cancelAllRunning();
    }
}