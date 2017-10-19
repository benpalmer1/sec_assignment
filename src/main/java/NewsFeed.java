package edu.curtin.cs.filesearcher;

import javax.swing.SwingUtilities;

public class FileSearcher
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override public void run()
            {            
                FSController controller = new FSController();
                FSWindow window = new FSWindow(controller);
                controller.setWindow(window);
                window.setVisible(true);
            }
        });
    }
}
