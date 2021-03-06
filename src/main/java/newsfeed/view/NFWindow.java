/**
 * @author Benjamin Nicholas Palmer
 * Student 17743075 - Curtin University
 * Window class to control the main application window in Swing.
 * Contains various areas that are loaded into a Container and displayed to the user.
 * Most actions are delegated to the controller to maintain class responsibility.
 */
package newsfeed.view;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import newsfeed.controller.*;

public class NFWindow extends JFrame
{
    private DefaultListModel<String> headlines;
    private DefaultListModel<String> downloads;
    private NFWindowController controller;
    private JButton updateButton;
    private JButton cancelLoadButton;
    private JLabel timeLabel;
    private JLabel loadingIcon;
    
    /** Window constructor for the Newsfeed application.
    ** Sets the formatting for multiple JPanels to make the general layout.
    ** loader.gif sourced from http://www.ajaxload.info under the terms of the DWTFYWT Public License. */
    public NFWindow(final NFWindowController controller)
    {
        super("Newsfeed");
        this.controller = controller;
        
        // Listener for exit by the system default window close button.
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(WindowEvent winEvt) {
                controller.stopNewsfeed();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //TOP AREA
        JComponent topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBorder(new EmptyBorder(5, 5, 0, 10));
        timeLabel = new JLabel("");
        updateButton = new JButton("Update Now");
        updateButton.setToolTipText("Force update all news feeds now.");
        topPanel.add(updateButton);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(timeLabel);
        
        //TOP MIDDLE AREA
        headlines = new DefaultListModel<>();
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        JScrollPane headlinesList = new JScrollPane(new JList<>(headlines));
        TitledBorder title = BorderFactory.createTitledBorder("Headlines");
        Border outer = BorderFactory.createCompoundBorder(title, new EmptyBorder(0, 5, 5, 5));
        middlePanel.setBorder(outer);
        middlePanel.add(headlinesList, BorderLayout.CENTER);
        
        //BOTTOM MIDDLE AREA
        JPanel bottomMiddlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        URL url = this.getClass().getResource("/loader.gif");
        ImageIcon icon = new ImageIcon(url);
        loadingIcon = new JLabel();
        cancelLoadButton = new JButton("Cancel");
        loadingIcon.setIcon(icon);
        icon.setImageObserver(loadingIcon);
        loadingIcon.setVisible(false);
        cancelLoadButton.setToolTipText("Cancel current loading of headlines. Scheduled updates remain unaffected.");
        bottomMiddlePanel.add(cancelLoadButton);
        bottomMiddlePanel.add(loadingIcon);
         
        // BOTTOM AREA
        downloads = new DefaultListModel<>();
        JPanel bottomDlListPanel = new JPanel();
        bottomDlListPanel.setLayout(new BoxLayout(bottomDlListPanel, BoxLayout.Y_AXIS));
        JScrollPane downloadsList = new JScrollPane(new JList<>(downloads));
        TitledBorder dlTitle = BorderFactory.createTitledBorder("Current Downloads");
        Border dlOuter = BorderFactory.createCompoundBorder(dlTitle, new EmptyBorder(0, 5, 5, 5));
        bottomDlListPanel.setBorder(dlOuter);
        bottomDlListPanel.add(downloadsList, BorderLayout.CENTER);
        
        // Composite bottom panel for the border layout
        JComponent bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(bottomMiddlePanel);
        bottomPanel.add(bottomDlListPanel);
        
        setButtonActionListeners();        
        // Set layout of the window.
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(middlePanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }
    
    // Method to set action listeners for the GUI buttons.
    private void setButtonActionListeners()
    {
        // When the "Update Now" button is pressed, update the list.
        updateButton.addActionListener(new ActionListener()
        {
            @Override public void actionPerformed(ActionEvent e)
            {
                controller.update();
            }
        });
        
        // When the "Cancel" button is pressed
        cancelLoadButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                synchronized(downloads)
                {
                    downloads.clear();
                }
                controller.cancelAllRunning();
            }
        });
    }
    
    public String getTime()
    {
        synchronized(timeLabel)
        {
            return timeLabel.getText();
        }
    }
    public void setTime(String time)    
    {
        synchronized(timeLabel)
        {
            timeLabel.setText(time);
        }
    }
    
    public void startLoading()
    {
        synchronized(loadingIcon)
        {
            loadingIcon.setVisible(true);
        }
    }
    public void stopLoading()
    {
        synchronized(loadingIcon)
        {
            loadingIcon.setVisible(false);
        }
    } 
    public boolean isLoading()
    {
        synchronized(loadingIcon)
        {
            return loadingIcon.isVisible();
        }
    }

    public void addHeadline(String headline)
    {
        synchronized(headlines)
        {
            headlines.add(0, headline);
        }
    }
    public void deleteHeadline(String headline)
    {
        synchronized(headlines)
        {
            headlines.removeElement(headline);
        }
    }

    public int downloadsCount()
    {
        synchronized(downloads){
            return downloads.getSize();
        }
    }    
    public void addDownload(String download)
    {
        synchronized(downloads)
        {
            downloads.add(0, download);
        }
    }
    public void deleteDownload(String download)
    {
        synchronized(downloads)
        {
            downloads.removeElement(download);
        }
    }
    
    // Methods to show error and information dialogs to the user.
    public void showError(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void showInformation(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.INFORMATION_MESSAGE);
    }
}
