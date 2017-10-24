package newsfeed.controller;

import java.awt.Dimension;
import javax.swing.SwingUtilities;
import newsfeed.view.*;

/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Main class for the Newsfeed application.
 * Sets up the swing invokeLater method and initialises all classes.
 * Where necessary, each action will operate on a separate thread.
 * Sets minimum window sizing to 800 x 600px.
 */
public class NewsFeed
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() // Or could have used a lambda here.
        {
            @Override public void run()
            {            
                NFWindowController controller = new NFWindowController();
                NFWindow window = new NFWindow(controller);
                controller.setWindow(window);
                controller.startTimerThread();
                
                window.setMinimumSize(new Dimension(800, 600));
                window.setVisible(true);
                
                controller.setPluginScheduler(new NFPluginScheduler(controller));
                controller.initPlugins(args);
            }
        });
    }
}
