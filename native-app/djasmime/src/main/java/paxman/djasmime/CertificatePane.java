package paxman.djasmime;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CertificatePane extends JOptionPane {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8746083521215584738L;
	
	private JComboBox certBox; 
	
	public CertificatePane() 
	{
		super(null, JOptionPane.WARNING_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
		setMessage(buildUI());
	}

	public Component buildUI()
	{
		JPanel back = new JPanel();
		BoxLayout layout = new BoxLayout(back, BoxLayout.Y_AXIS);
		
		back.setLayout(layout);
		
		JLabel label = new JLabel("Select certificate to sign with:");
		label.setAlignmentX(LEFT_ALIGNMENT);
		
		certBox = new JComboBox();  
		certBox.setAlignmentX(LEFT_ALIGNMENT);
		
		back.add(label);
		back.add(certBox);
		
		return back;
		
	}	
	
	public void setCertificates( ArrayList<String> options) 
	{
		certBox.setModel(new DefaultComboBoxModel(options.toArray()));
		certBox.setSelectedIndex(0);
	}
	
	public String getSelectedCertificate() 
	{
		return (String) certBox.getSelectedItem();
	}

	public static void main(String[] args) {
		CertificatePane a = new CertificatePane();
		
		 JDialog dialog = a.createDialog(null, "Test");
		 dialog.pack();
	     dialog.setVisible(true);
	}

}
