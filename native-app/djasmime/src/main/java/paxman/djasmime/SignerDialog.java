package paxman.djasmime;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JDialog;

public class SignerDialog extends JDialog 
{
	private static final long serialVersionUID = -1685322003003303681L;
	
	PreviewPane preview;
	CertificatePane certs;	MessagePane message;
	
	
	public SignerDialog(String title) 
	{
		super();
		setTitle(title);
		setLayout(new BorderLayout());
		buildUI();
		
	    pack();
	    setLocationRelativeTo(null);
		
	}
	
	public void buildUI()
	{
		preview = new PreviewPane();
		certs = new CertificatePane();
		message = new MessagePane();
		add(preview);
	}	

	public void removePanes() {
		remove(preview);
		remove(certs);
		remove(message);
		pack();
	}
	
	public void showPreview(String address, String content) 
	{
		removePanes();
		preview.setAddress(address);	
		preview.setContent(content);				
		add(preview);
		pack();
	}
	
	public void showCertificateSelector(ArrayList<String> options) 
	{
		removePanes();
		certs.setCertificates(options);
		add(certs);
		pack();
	}
	
	public void showMessage(String messageContent, boolean messageType) 
	{
		removePanes();
		message.setMessage(messageContent, messageType);
		add(message);
		pack();
	}
	
	public void addListeners() 
	{
		certs.addPropertyChangeListener(new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
    			
                if ("value".equals(name)) {
                	System.err.println("certSelected");
        			System.err.flush();
        			
        			
                	if (evt.getNewValue().equals(2))
                	{
                		firePropertyChange("userCanceled", 0,1);
                		dispose();
                	}
                	if (evt.getNewValue().equals(0))
                	{	
                		
                		firePropertyChange("certificateSelected", 0, certs.getSelectedCertificate());
                	}
                }
            }
        });
		
		preview.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();

                if ("value".equals(name)) {
                	if (evt.getNewValue().equals(2))
                	{
                		firePropertyChange("userCanceled", 0,1);
                		dispose();
                	}
                	if (evt.getNewValue().equals(0))
                	{	
                		firePropertyChange("previewDone", 0,1);
                	}
                }
            }
        });
		
		message.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();

                if ("value".equals(name)) {
                	
                	if (evt.getNewValue().equals(0))
                	{	
                		firePropertyChange("userCanceled", 0,1);
                		dispose();
                	}
                }
            }
        });
	}
	
	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		super.addPropertyChangeListener(propertyName, listener);
		certs.addPropertyChangeListener(propertyName,listener);
		preview.addPropertyChangeListener(propertyName, listener);
	}
	
	public static void main(String[] args) {

		SignerDialog a = new SignerDialog("Signer dialog");
		a.showPreview("www.google.com","Velit assumenda aspernatur aperiam consequatur tenetur impedit. Sed quae autem ut eveniet veritatis molestiae aut dolores. Quia consequuntur reiciendis sit. Blanditiis dignis");
		a.showCertificateSelector((ArrayList<String>) Arrays.asList("tralala","fmdksmfds"));
		a.addPropertyChangeListener("certificateSelected",new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();

                if ("value".equals(name)) {
	                	System.out.println(evt.getNewValue());
                }
            }
        });
		a.addListeners();
		a.setVisible(true);
	}

}
