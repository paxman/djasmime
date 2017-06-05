package paxman.djasmime;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.bouncycastle.crypto.util.Pack;

public class MessagePane extends JOptionPane
{
	private static final long serialVersionUID = 3555771083427830858L;
	private JTextArea contentArea;
	JLabel label;
	
	public MessagePane() 
	{
		super(null,JOptionPane.ERROR_MESSAGE,JOptionPane.DEFAULT_OPTION);
		setMessage(buildUI());
	}

	public Component buildUI()
	{
		JPanel back = new JPanel();
		BoxLayout layout = new BoxLayout(back, BoxLayout.Y_AXIS);
		
		back.setLayout(layout);
		
		label = new JLabel();
		label.setAlignmentX(LEFT_ALIGNMENT );
		
		contentArea = new JTextArea("");
		contentArea.setAlignmentX(LEFT_ALIGNMENT);
		contentArea.setColumns(15);
		contentArea.setLineWrap(true);
		
		back.add(label);
		back.add(contentArea);
		
		return back;
		
	}	
	
	public void setMessage(String message, boolean error) 
	{	
		if(error)
		{	
			setMessageType(JOptionPane.ERROR_MESSAGE);
			label.setText("There was an error while signing!<br>The error was:");
			contentArea.setText(message);
			contentArea.setVisible(true);
		}
		else
		{	
			setMessageType(JOptionPane.INFORMATION_MESSAGE);
			label.setText("Success! Message content was successfully signed!");
			contentArea.setVisible(false);
		}
	}
	
	public static void main(String[] args) {
		MessagePane a = new MessagePane();
	}
}
