package newsfeed.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import newsfeed.controller.*;
import newsfeed.model.*;

public class Window extends JFrame
{
    // A list-like container used to keep track of headlines.
    private DefaultListModel<String> headlines;
    private Controller controller;
    private JButton updateButton;
    private JButton cancelLoadButton;
    private JLabel timeLabel;
    
    public Window(final Controller controller)
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
        topPanel.setBorder(new EmptyBorder(5, 10, 0, 10));
        timeLabel = new JLabel("");
        updateButton = new JButton("Update Now");
        updateButton.setToolTipText("Force update all news feeds now.");
        topPanel.add(new JLabel("Time:"));
        topPanel.add(timeLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(updateButton);
        
        //MIDDLE AREA
        headlines = new DefaultListModel<>();
        headlines.addElement("test");
        headlines.addElement("test 2");
        headlines.addElement("test 2");
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        //iddlePanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        JScrollPane resultsList = new JScrollPane(new JList<String>(headlines));
        TitledBorder title = BorderFactory.createTitledBorder("Headlines");
        Border outer = BorderFactory.createCompoundBorder(title, new EmptyBorder(0, 5, 5, 5));
        //resultsList.setBorder(outer);
        middlePanel.setBorder(outer);
        middlePanel.add(resultsList, BorderLayout.CENTER);
        
        //BOTTOM AREA
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelLoadButton = new JButton("Cancel");
        bottomPanel.add(cancelLoadButton);
        cancelLoadButton.setToolTipText("Cancel current loading of headlines. "
                                        + "Scheduled updates remain unaffected.");
        
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
                headlines.clear();
            }
        });
    }
    
    public void setTime(String time)    
    {
        timeLabel.setText(time);
    }
    
    public void addResult(String result)
    {
        headlines.addElement(result);
    }
    
    public void showError(String message)
    {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
