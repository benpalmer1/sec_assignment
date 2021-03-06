/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Newsfeed Plugin Scheduler - Manages scheduling of plugins in the newsfeed.
 * Uses a scheduled thread pool of size 10 in order to run the plugins.
 * Contains methods to add a new plugin to the thread pool, cancel running plugins and update all plugins immediately.
 */

package newsfeed.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import newsfeed.model.Headline;
import newsfeed.model.Plugin;

public class NFPluginScheduler extends ScheduledThreadPoolExecutor
{
    private final ScheduledThreadPoolExecutor pluginScheduler;
    private final List<ScheduledFuture<Plugin>> currentlyRunningPlugins;
    private final Map<ScheduledFuture<Plugin>, Runnable> runnableLookup;
    private final NFWindowController controller;
    
    public NFPluginScheduler(NFWindowController controller)
    {
        super(10);  // init parent as executor.
        currentlyRunningPlugins = Collections.synchronizedList(new ArrayList<>());   // To make sure the currently running list is not corrupted when update or cancel is selected.
        runnableLookup = Collections.synchronizedMap(new HashMap<>());  // Used so that the 'update all' functionality works properly.
        pluginScheduler = this;  // Maximum of 10 threads.
        this.controller = controller;
    }
    
    
    //AddPlugin method - Adds a newly instantiated Plugin class to the pluginScheduler queue.
    public void addPlugin(Plugin newPlugin)
    {
        try
        {
            Runnable pluginRefresh = () -> // lambda expression to make a new runnable thread, to run the specific plugin's refresh code
            {
                controller.addDownload(newPlugin.getSource());
                List<Headline> newHeadlines = newPlugin.refreshHeadlines();
                if(newHeadlines != null)
                {
                    controller.updateHeadlines(newHeadlines);
                }
                controller.deleteDownload(newPlugin.getSource());
            };  // End of plugin refresh thread.
            afterExecute(pluginRefresh, null);  // To remove from the currently executing queue after refreshing the plugin information.
            
            synchronized(pluginScheduler)
            {
                ScheduledFuture newFuture = pluginScheduler.scheduleAtFixedRate(pluginRefresh, 0, newPlugin.getRefreshInterval(), TimeUnit.SECONDS);
                synchronized(currentlyRunningPlugins)
                {
                    runnableLookup.put(newFuture, pluginRefresh);
                    currentlyRunningPlugins.add(newFuture);
                }
            }
        }
        catch(RejectedExecutionException e)
        {
            NFEventLogger.logException("Error: Cannot start thread for plugin: " + newPlugin.getSource(), e);
        }
    }

    // CancelAllRunningl method - cancels all running plugins by calling ScheduledFuture.cancel on each future object.
    public void cancelAllRunning()
    {
        synchronized(currentlyRunningPlugins)
        {
            for(ScheduledFuture<Plugin> f : currentlyRunningPlugins)
            {
                f.cancel(true);
            }
            currentlyRunningPlugins.clear();
        }
    }
    
    // UpdateAllNow method - Loops through the list of plugins in the plugin scheduler
    // and schedules each plugin to run immediately if it is not already running.
    public synchronized void updateAllNow()
    {
        synchronized(pluginScheduler)   // To stop a plugin being added whilst updating
        {
            synchronized(currentlyRunningPlugins)   // To make sure the plugin is not cancelled whilst checking if running or not.
            {
                for(Runnable task : pluginScheduler.getQueue())
                {
                    ScheduledFuture<Plugin> result = (ScheduledFuture<Plugin>)task;
                    
                    if(!currentlyRunningPlugins.contains(result))   // Check if the plugin is already running
                    {
                        ScheduledFuture newFuture = pluginScheduler.schedule(runnableLookup.get(result), 0, TimeUnit.SECONDS);
                        currentlyRunningPlugins.add(newFuture);
                    }
                }
            }
        }
    }
    
    // StopAll method - Used by the window controller class to immediately hault execution of all plugins.
    public void stopAll() throws InterruptedException
    {
        pluginScheduler.shutdownNow();
        pluginScheduler.awaitTermination(5, TimeUnit.SECONDS);
    }
    
    // Method which will run after each future has completed, to remove them from the currently executing list.
    // Implemented as per specification from JavaDOCS - ThreadPoolExecutor.afterExecute()
    @Override
    protected void afterExecute(Runnable r, Throwable t)
    {
        super.afterExecute(r, t);
        if (t == null && r instanceof ScheduledFuture<?>)
        {
            @SuppressWarnings("unchecked")
            ScheduledFuture<Plugin> result = (ScheduledFuture<Plugin>)r;
            if(currentlyRunningPlugins.contains(result))
            {
                currentlyRunningPlugins.remove(result);
            }
        }
    }
}
