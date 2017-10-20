package newsfeed.model;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import newsfeed.controller.NFWindowController;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Implementable Plugin class required to be extended for plug-in support.
 */

public abstract class Plugin
{
    //Window controller reference
    private NFWindowController windowController;
    private String pageText;
    
    // Values initialised in the plugin constructor:
    private String source;
    private int refreshInterval;
    
    // Template method required to be implemented in the plugin
    public abstract ArrayList<String> getHeadlines(String pageText);   // Method to parse the page HTML
    
    public String getSource()
    {
        return this.source;
    }
    
    public int getRefreshInterval()
    {
        return this.refreshInterval;
    }
    
    public void loadSource()
    {
        try
        {    
            URL url = new URL(getSource()); // Download source
            try(ReadableByteChannel chan = Channels.newChannel(url.openStream()))
            {
                // "try-with-resources" statement; will automatically call
                // chan.close() when finished.
                ByteBuffer buf = ByteBuffer.allocate(65536);
                byte[] array = buf.array();
                int bytesRead = chan.read(buf); // Read a chunk of data.
                
                while(bytesRead != -1)
                {
                    // Get data from array[0 .. bytesRead - 1] 
                    buf.clear();
                    bytesRead = chan.read(buf);
                }
               pageText = new String(array, StandardCharsets.UTF_8);
            }
            catch(ClosedByInterruptException e) {   // Thread.interrupt()
                windowController.logInfo("Download of page cancelled.");
            }
            catch(IOException e) {  // An error
                windowController.logException("Error: Error downloading URL.");
                windowController.getWindow().showError("Error: Error downloading URL.");
            }
        }
        catch(MalformedURLException e)  // Plugin has incorrect URL
        {
            windowController.logException("Error: Specified plugin URL of incorrect format.");
            windowController.getWindow().showError("Error: Specified plugin URL of incorrect format.");
        }
    }
    
    /** Method to dynamically load a class file.
     * Program will continue to run for plugins that cannot load properly however an error message will display.
    */
    public Plugin loadClassFile(String pluginName)
    {
        Plugin newPlugin = null;
        try
        {
            Class newClass = Class.forName(pluginName);
            newPlugin = (Plugin) newClass.newInstance();
        } catch (ClassNotFoundException e)
        {
            windowController.logException("Error: Plugin class not found.");
            windowController.getWindow().showError("Error: Plugin class not found.");
        } catch(InstantiationException | IllegalAccessException | ClassCastException e)
        {
            windowController.logException("Error: Cannot instantiate plugin class.");
            windowController.getWindow().showError("EError: Cannot instantiate plugin class.");
        }
        
        return newPlugin;
    }
    
    public void setWindowController(NFWindowController controller)
    {
        this.windowController = controller;
    }
}