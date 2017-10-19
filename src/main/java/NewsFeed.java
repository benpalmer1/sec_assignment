package newsfeed.controller;

import javax.swing.SwingUtilities;

public class NewsFeed
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override public void run()
            {            
                Controller controller = new Controller();
                Window window = new Window(controller);
                controller.setWindow(window);
                window.setVisible(true);
            }
        });
    }
}
