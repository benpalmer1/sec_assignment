package newsfeed.model;

import java.io.File;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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
    protected String source;
    protected int refreshInterval;
    
    private boolean downloading = false;
    ArrayList<String> currentHeadlines;

    // Template method required to be implemented in the plugin
    protected abstract ArrayList<String> parseHeadlines(String pageText);   // Method to parse the page HTML
    
    public String getSource()
    {
        return this.source;
    }
    
    public int getRefreshInterval()
    {
        return this.refreshInterval;
    }
    
    public ArrayList<String> getHeadlines()
    {
        return this.currentHeadlines;
    }
    public ArrayList<String> refreshHeadlines()
    {
        if(!downloading)   // Else page is currently being downloaded, do nothing
        {
            downloading = true;
            try
            {   
                windowController.getWindow().startLoading();
                windowController.getWindow().addDownload(getSource());

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
                   currentHeadlines = parseHeadlines(pageText);
                   NFWindowController.logInfo("Downloaded headlines from source: " + source);
                }
                catch(ClosedByInterruptException e) {   // Triggered by Thread.interrupt()
                    NFWindowController.logInfo("Download of page cancelled.");
                }
                catch(IOException e) {  // An error
                    NFWindowController.logException("Error: Error downloading URL.", e);
                    windowController.getWindow().showError("Error: Error downloading URL.");
                }
            }
            catch(MalformedURLException e)  // Plugin has incorrect URL
            {
                NFWindowController.logException("Error: Specified plugin URL of incorrect format.", e);
                windowController.getWindow().showError("Error: Specified plugin URL of incorrect format.");
            }
            finally
            {
                if(windowController != null)
                {
                    windowController.getWindow().stopLoading();
                    windowController.getWindow().deleteDownload(getSource());
                    downloading = false;
                }
            }
        }
        return currentHeadlines;
    }
    
    /** Method to dynamically load a class file.
     * Program will continue to run for plugins that cannot load properly however an error message will display.
     * Returns null when the plugin is of an incorrect format.
    */
    public static Plugin loadClassFromJarFile(String pluginName)
    {
        Plugin newPlugin = null;
        String className = "";
        try
        {
            if(pluginName.endsWith(".jar"))
            {
                JarFile jarFile = new JarFile(pluginName);
                if(jarFile != null)
                {
                    Enumeration jarComponents = jarFile.entries();

                    while (jarComponents.hasMoreElements())
                    {
                        JarEntry entry = (JarEntry) jarComponents.nextElement();
                        if(entry.getName().endsWith(".class"))
                        {
                            className = entry.getName();
                        }
                    }
                    // Set file directory URL containing the .jar
                    File file = new File(System.getProperty("user.dir")+"/"+pluginName);
                    URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
                    // Initialise the class.
                    Class newClass = Class.forName(className.replace(".class", ""), true, loader);
                    newPlugin = (Plugin) newClass.newInstance();
                }
            }
            else if(pluginName.endsWith(".class"))
            {
                Class newClass = Class.forName(className.replace(".class", ""));
                newPlugin = (Plugin) newClass.newInstance();
            }   
        }
        catch (ClassNotFoundException | IOException e)
        {
            NFWindowController.logException("Error: Plugin class not found. Class name: " + className, e);
        }
        catch(InstantiationException | IllegalAccessException | ClassCastException e)
        {
            NFWindowController.logException("Error: Cannot instantiate plugin class. Class name: " + className, e);
        }
        
        if(newPlugin != null)
        {
            if(newPlugin.getSource() == null)
            {
                newPlugin = null;
            }
            else if(newPlugin.getRefreshInterval() <= 0)
            {
                newPlugin = null;
            }
        }
        return newPlugin;
    }
    
    public void setWindowController(NFWindowController controller)
    {
        this.windowController = controller;
    }
}