package newsfeed.model;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import newsfeed.controller.WindowController;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Implementable interface required to be extended for plug-in support.
 */

public abstract class Plugin
{
    //Window controller reference
    private WindowController windowController;
    private String pageText;
    
    // Template methods required to be implemented in the plugin
    public abstract ArrayList<String> getHeadlines(String pageText);   // Method to parse the page HTML
    public abstract String getSource();
    public abstract int getRefresh();
    
    public void commonLoadingMethod()
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
                
            }
            catch(IOException e) {  // An error
                
            }
        }
        catch(MalformedURLException e)  // Plugin has incorrect URL
        {
            
        }
    }
    
    public void setWindowController(WindowController controller)
    {
        this.windowController = controller;
    }
}