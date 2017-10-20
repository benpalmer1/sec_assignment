package newsfeed.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.swing.Timer;
import newsfeed.view.*;
import newsfeed.model.*;

public class Controller
{
    private Window window = null;
    
    private LinkedBlockingQueue<Path>   queue = new LinkedBlockingQueue<>(1000);
//    private ArrayBlockingQueue<Path>    queue = new ArrayBlockingQueue<>(1000); 
//    private SynchronousQueue<Path>      queue = new SynchronousQueue<>();
    private ExecutorService executorService;
    private boolean running = true;
    private Timer timeTimer;
        
    public Controller()
    {
        executorService = Executors.newFixedThreadPool(10);
    }

    public void setWindow(Window window)
    {
        this.window = window;
    }
    
    private void filterThreadStart(String searchPath, final String searchTerm)
    {
        // Process the elements in the dequeue/filter thread
        Runnable filterThread = () ->
        {
            while(running) // Loop through any items on the search queue.
            { 
                executorService.execute(new Runnable()
                {
                    public void run()
                    {  
                        String fileName = "";
                        try
                        {
                            Path newFile = queue.take();
                            fileName = newFile.toString();
                            String tempOut = "";
                            BufferedReader reader = new BufferedReader(new FileReader(fileName));
                            String newLine = "";
                        
                            while((newLine = reader.readLine()) != null)
                            {
                                if(newLine.contains(searchTerm))
                                {
                                    System.out.println(newLine);
                                    window.addResult(fileName);
                                }
                            }
                            reader.close();
                        }
                        catch(InterruptedException e)
                        {
                            // Stop here.
                        }
                        catch(CharConversionException e)
                        {
                            // ignore a file that cannot be read properly due to binary type.
                        }
                        catch(IOException e)
                        {
                            // ignore for file search purposes. Add to the the window output.
                        }
                    }
                });
            }
            
        }; // End of the dequeue thread.  
        
        new Thread(filterThread).start(); // Start the thread.
    }
    
    
    public void search(String searchPath, final String searchTerm)
    {   
        running = true;
        
        // Call the method to start the file filtering thread:
        filterThreadStart(searchPath, searchTerm);
    
        // Start the enqueue thread.
        Runnable enqueueThread = () ->
        {
            try
            {
                // Recurse through the directory tree
                Files.walkFileTree(Paths.get(searchPath), new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    {
                        //dequeue these results in separate method
                        try {
                            queue.put(file);
                        }
                        catch(InterruptedException e) {
                            // Stop here.
                        }
                        
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            catch(IOException e)
            {
                // This error handling is a bit quick-and-dirty, but it will suffice here.
                window.showError(e.getClass().getName() + ": " + e.getMessage());
            }
        };
        new Thread(enqueueThread).start();
        // End of enqueue thread
    }
    
    /* Using another thread to set the time every minute so
    * that execution of the rest of the software is unaffectved by the clock. */
    public void startTimerThread()
    {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar initial = Calendar.getInstance();
        window.setTime(dateFormat.format(initial.getTime()));
        
        // Start update timer
        timeTimer = new Timer(1000, (ActionEvent e) ->
        {
            Calendar now = Calendar.getInstance();
            window.setTime(dateFormat.format(now.getTime()));
        });
        
        timeTimer.start();
    }
    
    public void stop()
    {
        running = false;
        timeTimer.stop();
        executorService.shutdown();
    }
}
