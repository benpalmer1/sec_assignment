package newsfeed.controller;

import java.awt.Dimension;
import javax.swing.SwingUtilities;
import newsfeed.view.*;
import newsfeed.model.*;

public class NewsFeed
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() // Or could have used a lambda here.
        {
            @Override public void run()
            {            
                WindowController controller = new WindowController();
                Window window = new Window(controller);
                controller.setWindow(window);
                controller.startTimerThread();
                window.setMinimumSize(new Dimension(800, 600));
                window.setVisible(true);
            }
        });
    }
}
