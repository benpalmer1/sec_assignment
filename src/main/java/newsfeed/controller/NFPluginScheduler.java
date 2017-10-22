package newsfeed.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import newsfeed.model.Plugin;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Newsfeed Plugin Scheduler - Manages scheduling of plugins in the newsfeed.
 */
public class NFPluginScheduler extends ScheduledThreadPoolExecutor
{
    private ScheduledThreadPoolExecutor pluginScheduler;
    private List<ScheduledFuture<Plugin>> currentlyRunningPlugins;
    private NFWindowController controller;
    
    public NFPluginScheduler(NFWindowController controller)
    {
        super(10);  // init parent as executor.
        currentlyRunningPlugins = Collections.synchronizedList(new ArrayList<ScheduledFuture<Plugin>>());          // To make sure the currently running list is not corrupted.
        pluginScheduler = this;  // Maximum of 10 threads.
        pluginScheduler.setRemoveOnCancelPolicy(true);
        this.controller = controller;
    }
    
    @SuppressWarnings("unchecked")
    public void addPlugin(Plugin newPlugin)
    {
        try
        {
            Runnable pluginRefresh = () -> // lambda expression to make a new runnable thread, to run the specific plugin's refresh code
            {
                controller.getWindow().addDownload(newPlugin.getSource());
                controller.getWindow().startLoading();

                ArrayList<String> newHeadlines = newPlugin.refreshHeadlines();
                if(newHeadlines != null)
                {
                    controller.getWindow().updateHeadlines(newPlugin.getSource(), newHeadlines);
                }
                controller.getWindow().stopLoading();
                controller.getWindow().deleteDownload(newPlugin.getSource());
            };  // End of plugin refresh thread.
            
            afterExecute(pluginRefresh, null);  // To remove from the currently executing queue after refreshing the plugin information.
            ScheduledFuture newFuture = pluginScheduler.scheduleAtFixedRate(pluginRefresh, 0, newPlugin.getRefreshInterval(), TimeUnit.MINUTES);
            currentlyRunningPlugins.add(newFuture);
        }
        catch(RejectedExecutionException e)
        {
            NFWindowController.logException("Error: Cannot start thread for plugin: " + newPlugin.getSource(), e);
        }
    }

    public void cancelAllRunning()
    {
        for(ScheduledFuture f : currentlyRunningPlugins)
        {
            f.cancel(true);
        }
        
        currentlyRunningPlugins.clear();
    }
    
    public void updateAllNow()
    {
        for(Runnable task : pluginScheduler.getQueue())
        {
            task.run();
        }
    }
    
    public void stopAll()
    {
        pluginScheduler.shutdownNow();
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
