import com.google.api.client.http.FileContent;
import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.security.GeneralSecurityException;
import javax.swing.JOptionPane;

public class DemoProj extends JFrame implements ActionListener
{
    private static final Font MENU_FONT = new Font("Helventica", Font.PLAIN, 14);
    private static final Font BODY_FONT = new Font("Arial", Font.PLAIN, 14);

    JTextArea t;
    JFrame f;

    DemoProj()
    {
        f = new JFrame("Text editor");

        try
        {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        }
        catch (Exception e)
        {
        }

        t = new JTextArea();
        t.setFont(BODY_FONT);

        JMenuBar mb = new JMenuBar();
        mb.setFont(MENU_FONT);

        JMenu m1 = new JMenu(" File ");
        JMenuItem mi1 = new JMenuItem(" New ");
        JMenuItem mi2 = new JMenuItem(" Open ");
        JMenuItem mi3 = new JMenuItem(" Save ");
        mi1.addActionListener(this);
        mi2.addActionListener(this);
        mi3.addActionListener(this);
        m1.add(mi1);
        m1.add(mi2);
        m1.add(mi3);

        JMenu m2 = new JMenu(" Edit ");
        JMenuItem mi4 = new JMenuItem(" Cut ");
        JMenuItem mi5 = new JMenuItem(" Copy ");
        JMenuItem mi6 = new JMenuItem(" Paste ");
        mi4.addActionListener(this);
        mi5.addActionListener(this);
        mi6.addActionListener(this);
        m2.add(mi4);
        m2.add(mi5);
        m2.add(mi6);

        JMenuItem mc = new JMenuItem(" Close ");
        mc.addActionListener(this);

        mb.add(m1);
        mb.add(m2);
        mb.add(mc);

        f.setJMenuBar(mb);
        f.add(t);
        f.setSize(500, 500);
        f.show();
    }

    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();

        if (s.equals(" Cut "))
        {
            t.cut();
        }
        else if (s.equals(" Copy "))
        {
            t.copy();
        }
        else if (s.equals(" Paste "))
        {
            t.paste();
        }
        else if (s.equals(" Save "))
        {
            try 
            {
            	String filename = "";
            	filename = JOptionPane.showInputDialog("Enter the File Name");
            	if ( ! filename.equals(null)) 
    			{ 
    				File fi = new File("C:\\Users\\Madhuri\\Documents\\Project\\Files\\" + filename + ".txt"); 
    				try
    				{ 
    					FileWriter wr = new FileWriter(fi, false); 
    					BufferedWriter w = new BufferedWriter(wr); 
    					w.write(t.getText()); 
    					w.flush(); 
    					w.close(); 
    				} 
    				catch (Exception evt) 
    				{ 
    					JOptionPane.showMessageDialog(f, evt.getMessage()); 
    				} 
    			} 
    			else
    			{
    				JOptionPane.showMessageDialog(f, "the user cancelled the operation"); 
    			}
                DriveQuickstart.Drive(filename);
            } 
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            catch (GeneralSecurityException e1)
            {
                e1.printStackTrace();
            }
        }
        else if (s.equals("Open"))
        {
            // Create an object of JFileChooser class
            JFileChooser j = new JFileChooser("f:");

            // Invoke the showsOpenDialog function to show the save dialog
            int r = j.showOpenDialog(null);

            // If the user selects a file
            if (r == JFileChooser.APPROVE_OPTION)
            {
                // Set the label to the path of the selected directory
                File fi = new File(j.getSelectedFile().getAbsolutePath());

                try
                {
                    // String
                    String s1 = "", sl = "";

                    // File reader
                    FileReader fr = new FileReader(fi);

                    // Buffered reader
                    BufferedReader br = new BufferedReader(fr);

                    // Initilize sl
                    sl = br.readLine();

                    // Take the input from the file
                    while ((s1 = br.readLine()) != null) {
                        sl = sl + "\n" + s1;
                    }

                    // Set the text
                    t.setText(sl);
                }
                catch (Exception evt)
                {
                    JOptionPane.showMessageDialog(f, evt.getMessage());
                }
            }
            else
            {
                JOptionPane.showMessageDialog(f, "the user cancelled the operation");
            }
        }
        else if (s.equals("New"))
        {
            t.setText("");
        }
        else if (s.equals("Close"))
        {
            f.setVisible(false);
        }
    }

    public static void main(String args[])
    {
        DemoProj e = new DemoProj();

    }
}

