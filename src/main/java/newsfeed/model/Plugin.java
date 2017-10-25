/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Implementable Plugin class, required to be extended for plug-in support. Represents a Plugin model.
 * Has functionality to build a Plugin from JAR and refresh plugin headlines.
 * To build a plugin:
 * 1 - import import newsfeed.model.Plugin;
 * 2 - extend Plugin
 * 3 - initialise 'source' and 'refreshInterval'
 * 4 - implement refreshHeadlines.
 */
package newsfeed.model;

import java.io.File;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import newsfeed.controller.NFEventLogger;
import newsfeed.controller.NFWindowController;
import org.apache.commons.text.StringEscapeUtils;

public abstract class Plugin
{
    //Window controller reference
    protected NFWindowController windowController;
    protected String pageText;
    // Values initialised in the plugin constructor:
    protected String source;
    protected int refreshInterval;
    protected final List<Headline> currentHeadlines;

    // Template method required to be implemented in the plugin
    // This method to parse the page HTML and retrieve the list of headlines.
    abstract protected ArrayList<Headline> parseHeadlines(String pageText);
    
    public Plugin()
    {
        currentHeadlines = Collections.synchronizedList(new ArrayList<>());
    }
    
    public String getSource()
    {
        return this.source;
    }
    
    public int getRefreshInterval()
    {
        return this.refreshInterval;
    }
    
    public static ArrayList<String> getHeadlinesFormatted(List<Headline> toFormatHeadlines)
    {
        ArrayList<String> formattedHeadlines = new ArrayList<>();
        for(Headline h : toFormatHeadlines)
        {
            formattedHeadlines.add(h.toString());
        }

        return formattedHeadlines;
    }
    
    public void setWindowController(NFWindowController controller)
    {
        this.windowController = controller;
    }
    
    public NFWindowController getWindowController()
    {
        return this.windowController;
    }
    
    /** RefreshHeadlines method - Loads the website HTML into a string.
     * Calls the plugin-specific parseHeadlines method, updates the list of current
     * headlines and returns the list that was obtained. */
    public List<Headline> refreshHeadlines()
    {
        try
        {   
            URL url = new URL(source); // Download source
            try(ReadableByteChannel chan = Channels.newChannel(url.openStream()))
            {
                // "try-with-resources" statement; will automatically call
                // chan.close() when finished.
                ByteBuffer buf = ByteBuffer.allocate(65536);
                pageText = "";  // Reset page text to being empty.
                
                // Given example code worked quite unreliably - modified to work here.
                while(chan.read(buf) != -1)
                {
                    // Get data from array[0 .. bytesRead - 1]
                    pageText += new String(buf.array(), StandardCharsets.UTF_8);
                    buf.clear();
                }
                
                // Apache commons utility function to unescape html to a Java string
                // To make sure it is formatted correctly for HTML parsing and remove any strange characters
                // The gradle build file includes this in the final build jar for portability.
                pageText = StringEscapeUtils.unescapeHtml4(pageText);
                
                synchronized(currentHeadlines)
                {
                    currentHeadlines.clear();
                    currentHeadlines.addAll(parseHeadlines(pageText));
                }
                
                NFEventLogger.logInfo("Downloaded "+ currentHeadlines.size() + " headlines from source: " + source);
            }
            catch(ClosedByInterruptException e) {   // Triggered by Thread.interrupt()
                NFEventLogger.logInfo("Download of page cancelled: " + getSource());
            }
            catch(IOException e) {  // An error
                NFEventLogger.logException("Error: Error downloading URL: " + getSource(), e);
                windowController.getWindow().showError("Error: Error downloading URL: "+e.getMessage());
            }
        }
        catch(MalformedURLException e)  // Plugin has incorrect URL
        {
            NFEventLogger.logException("Error: Specified plugin URL of incorrect format.", e);
            windowController.getWindow().showError("Error: Specified plugin URL of incorrect format.");
        }
        return currentHeadlines;
    }
    
    /** Method to dynamically load a class file from a Jar.
     * Program will continue to run for plugins that cannot load properly however an error message will display,
     * and any valid plugins will continue to run successfully.
     * Returns null when the plugin is of an incorrect format for error-checking purposes. */
    public static Plugin loadClassFromJarFile(String pluginName)
    {
        Plugin newPlugin = null;
        String className = "";
        try
        {
            if(pluginName.endsWith(".jar"))
            {
                JarFile jarFile = new JarFile(pluginName);
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
            else
            {
                throw new InstantiationException("Error: Plugin type not supported.");
            }   
        }
        catch (ClassNotFoundException | IOException e)
        {
            NFEventLogger.logException("Error: Plugin class not found. Class name: " + className, e);
        }
        catch(InstantiationException | IllegalAccessException e)
        {
            NFEventLogger.logException("Error: Cannot instantiate plugin class. Class name: " + className, e);
        }
        catch(ClassCastException e)
        {
            NFEventLogger.logException("Error: Specified Plugin of incorrect format. Cannot be loaded. Class name: " + className, e);
        }
        if(newPlugin != null)   // Checks to set null on error conditions to be picked up by the calling function.
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
}