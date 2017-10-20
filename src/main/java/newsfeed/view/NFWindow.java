package newsfeed.view;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import newsfeed.controller.*;
import newsfeed.model.*;

public class NFWindow extends JFrame
{
    // A list-like container used to keep track of headlines.
    private DefaultListModel<String> headlines;
    private DefaultListModel<String> downloads;
    private NFWindowController controller;
    private JButton updateButton;
    private JButton cancelLoadButton;
    private JLabel timeLabel;
    private JLabel loadingIcon;
    
    public NFWindow(final NFWindowController controller)
    {
        super("Newsfeed");
        // Listener for exit by the system default window close button.
        this.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(WindowEvent winEvt) {
                    controller.stop();
                    System.exit(0);
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
        topPanel.add(new JLabel("Time: "));
        topPanel.add(timeLabel);
        
        //TOP MIDDLE AREA
        headlines = new DefaultListModel<>();
        headlines.addElement("test");
        headlines.addElement("test 2");
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        JScrollPane headlinesList = new JScrollPane(new JList<String>(headlines));
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
        bottomMiddlePanel.add(loadingIcon);
        bottomMiddlePanel.add(cancelLoadButton);
        
        
        // BOTTOM AREA
        downloads = new DefaultListModel<>();
        downloads.addElement("test");
        downloads.addElement("test 2");
        JPanel bottomDlListPanel = new JPanel();
        bottomDlListPanel.setLayout(new BoxLayout(bottomDlListPanel, BoxLayout.Y_AXIS));
        JScrollPane downloadsList = new JScrollPane(new JList<String>(downloads));
        TitledBorder dlTitle = BorderFactory.createTitledBorder("Downloads");
        Border dlOuter = BorderFactory.createCompoundBorder(dlTitle, new EmptyBorder(0, 5, 5, 5));
        bottomDlListPanel.setBorder(dlOuter);
        bottomDlListPanel.add(downloadsList, BorderLayout.CENTER);
        
        // Composite bottom panel for the border layout
        JComponent bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(bottomMiddlePanel);
        bottomPanel.add(bottomDlListPanel);
        
        setActionListeners();        
        // Set layout of the window.
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(middlePanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        pack();
    }
    
    private void setActionListeners()
    {
        // When the "Update Now" button is pressed, update the list.
        // TO DO
        updateButton.addActionListener(new ActionListener()
        {
            @Override public void actionPerformed(ActionEvent e)
            {
                //controller.update();
            }
        });
        
        // When the "Cancel" button is pressed
        // TO DO
        cancelLoadButton.addActionListener(new ActionListener()
        {   
            @Override public void actionPerformed(ActionEvent e)
            {
                downloads.clear();
            }
        });
    }
    
    public void startLoading()
    {
        loadingIcon.setVisible(true);
    }
    
    public void stopLoading()
    {
        loadingIcon.setVisible(false);
    }
    
    
    public void setTime(String time)    
    {
        timeLabel.setText(time);
    }
    
    
    public DefaultListModel<String> getHeadlines()
    {
        return this.headlines;
    }
    public void addHeadline(String headline)
    {
        headlines.addElement(headline);
    }
    
    public void deleteHeadline(String headline)
    {
        headlines.removeElement(headline);
    }
    
    
    public DefaultListModel<String> getDownloads()
    {
        return this.downloads;
    }
    
    public void addDownload(String download)
    {
        downloads.addElement(download);
    }
    
    public void deleteDownload(String download)
    {
        downloads.removeElement(download);
    }
    
    public void showError(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void showInformation(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.INFORMATION_MESSAGE);
    }
}
