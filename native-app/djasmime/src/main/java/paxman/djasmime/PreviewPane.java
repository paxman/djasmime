package paxman.djasmime;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class PreviewPane extends JOptionPane
{
	private static final long serialVersionUID = 3184087722747566116L;
	
	private JLabel addressLabel;
	private JTextArea contentArea;
	
	public PreviewPane() 
	{
		super(null,JOptionPane.WARNING_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
		setMessage(buildUI());
	}
	
	public Component buildUI()
	{
		JPanel back = new JPanel();
		BoxLayout layout = new BoxLayout(back, BoxLayout.Y_AXIS);
		
		back.setLayout(layout);
		
		JLabel label = new JLabel("Website:");
		label.setAlignmentX(LEFT_ALIGNMENT );
		
		addressLabel = new JLabel("");
		addressLabel.setAlignmentX(LEFT_ALIGNMENT);
		
		JLabel mid = new JLabel("is requesting you to sign the following content");
		mid.setAlignmentX(LEFT_ALIGNMENT);
		
		contentArea = new JTextArea("");
		contentArea.setAlignmentX(LEFT_ALIGNMENT);
		contentArea.setColumns(15);
		contentArea.setLineWrap(true);
		
		
		back.add(label);
		back.add(addressLabel);
		back.add(mid);
		back.add(contentArea);
		
		return back;
		
	}	
	
	public void setAddress(String address) {
		addressLabel.setText("<html><body><h1 style='color:red;'>"+address+"</h1></body></html>");
	}
	
	public void setContent(String content) 
	{		
		contentArea.setText(content);
	}
	
	public static void main(String[] args) {
	
		PreviewPane a = new PreviewPane();
		a.setAddress("www.test.com");
		a.setContent("Extensions and apps can exchange messages with native applications using an API that is similar to the other message passing APIs. Native applications that support this feature must register a native messaging host that knows how to communicate with the extension. Chrome starts the host in a separate process and communicates with it using standard input and standard output streams.");
		
		 JDialog dialog = a.createDialog(null, "Preview");
		 dialog.pack();
	     dialog.setVisible(true);
	}
}