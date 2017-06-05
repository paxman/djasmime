package paxman.djasmime;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

public class Djasmime {

	public SignerDialog signDialog;
	private KeyStore keyStore;

	public Djasmime() {
		try {
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream("log.txt"))));
		} catch (FileNotFoundException e) {
			log(e.getMessage());
			
		}

		JsonParser parser;

		parser = Json.createParser(new InputStreamReader(System.in) );

		while (parser.hasNext()) {
			JsonParser.Event event = null;

			try {
				event = parser.next();

			} catch (JsonParsingException e) {
				continue;
			}

			if (event == null){
				continue;
			}

			switch(event) {

			case START_OBJECT:

				try {
					JsonObject obj = (JsonObject) parser.getObject();
					log("Got object: "+obj.toString());

					if(obj.containsKey("url") && obj.containsKey("content"))
					{		

						signDialog = new SignerDialog("S/MIME signing");
						signDialog.addListeners();
						signDialog.showPreview(obj.getString("url"),obj.getString("content"));
						signDialog.setVisible(true);
						
						signDialog.addPropertyChangeListener("previewDone", new PropertyChangeListener() 
						{
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								log("Content ack'd");
								if (load())
									signDialog.showCertificateSelector(getAliases());
								else
									signDialog.showMessage("Couldn't load keystore", true);
							}

						});

						signDialog.addPropertyChangeListener("certificateSelected", new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								log("Certificate selected");
								sign(evt.getNewValue(), obj.getString("content"));
							}

						});

						signDialog.addPropertyChangeListener("userCanceled", new PropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent evt) {
								quit("Canceled");
							}

						});

						

					}
				} 
				catch (Exception e){
					signDialog.showMessage(e.getMessage(), true);
					return;
				}

				break;
			default:
				break;

			}
		}

		parser.close();
	}

	public boolean load() {
		log("Loading keystore");

		Provider p = Security.getProvider("SunMSCAPI");

		KeyStore.Builder ksBuilder = null;
		KeyStore.ProtectionParameter cbhp = new KeyStore.CallbackHandlerProtection((CallbackHandler) new PasswordCallbackHandler(this));

		ksBuilder = KeyStore.Builder.newInstance("WINDOWS-MY", p, cbhp);

		try {
			keyStore = ksBuilder.getKeyStore();
		} catch (KeyStoreException e) {
			signDialog.showMessage(e.getMessage(), true);
			return false;
		}
		return true;
	}

	public void sign(Object certificate, String content ) {
		
		String cert = (String) certificate;
		
		log("Signing with cert: "+cert);
		
		try {
			keyStore.load(null, null);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}

		X509Certificate signCert = null;
		try {
			signCert = (X509Certificate) keyStore.getCertificate(cert);
		} catch (KeyStoreException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);	
			return;
		}

		KeyPair signKP = null;
		try {
			signKP = new KeyPair(signCert.getPublicKey(), (PrivateKey) keyStore.getKey(cert, "".toCharArray()));
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}

		List certList = new ArrayList();

		certList.add(signCert);

		Store certs = null;
		try {
			certs = new JcaCertStore(certList);
		} catch (CertificateEncodingException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}

		ContentSigner sha1Signer = null;
		try {
			sha1Signer = new JcaContentSignerBuilder("SHA1withRSA").build(signKP.getPrivate());
		} catch (OperatorCreationException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}

		CMSSignedDataStreamGenerator gen = new CMSSignedDataStreamGenerator();

		try {
			gen.addSignerInfoGenerator(
					new JcaSignerInfoGeneratorBuilder(
							new JcaDigestCalculatorProviderBuilder().build())
					.build(sha1Signer, signCert));
		} catch (CertificateEncodingException | OperatorCreationException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}

		try {
			gen.addCertificates(certs);
		} catch (CMSException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}

		MimeBodyPart mime_part = new MimeBodyPart();
		
		try {
			mime_part.setText(content, "utf-8", "plain");
		} catch (MessagingException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}	
		
		MimeMessage     msg = new MimeMessage((Session)null);

        try {
			Enumeration     en = mime_part.getAllHeaders();

			msg.setDataHandler(mime_part.getDataHandler());

			while (en.hasMoreElements())
			{
			    Header  hdr =(Header)en.nextElement();

			    msg.setHeader(hdr.getName(), hdr.getValue());
			}

			msg.saveChanges();

			en = msg.getAllHeaders();

			while (en.hasMoreElements())
			{
			    Header  hdr =(Header)en.nextElement();

			    if (Strings.toLowerCase(hdr.getName()).startsWith("content-"))
			    {
			    	mime_part.setHeader(hdr.getName(), hdr.getValue());
			    }
			}
		} catch (MessagingException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}
		
        
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		try {
			msg.writeTo(bstream);
		} catch (IOException | MessagingException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}
		
		OutputStream sigOut = null;
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		try {
			sigOut = gen.open(bOut,true);
			sigOut.write(bstream.toByteArray());
		} catch (IOException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}
		
		try {
			sigOut.close();
		} catch (IOException e) {
			log(e.getMessage());
			signDialog.showMessage(e.getMessage(), true);
			return;
		}
		
		message(Base64.toBase64String(bOut.toByteArray()),true);
	}

	private void message(String message, boolean success) 
	{
		try {
			JsonObjectBuilder jsonObjSuccess = Json.createObjectBuilder();
			
			if (success)
			 {
				jsonObjSuccess.add("success", true);
				jsonObjSuccess.add("signedContent", message);

			 }
			else
			{
				jsonObjSuccess.add("success", false);
				jsonObjSuccess.add("message", message);
				
			}
			
			String jsonMessage = jsonObjSuccess.build().toString();
			
			System.out.write(getBytes(jsonMessage.length()));
			System.out.write(jsonMessage.getBytes());
			System.out.flush();
			
			signDialog.showMessage("Successfully sent message to browser!", false);
			
		} catch (IOException e) {
			log(e.getMessage());
			signDialog.showMessage("Error sending message to browser: "+e.getMessage(), false);	
		}
		
		
	}

	public ArrayList<String> getAliases() {

		Enumeration<String> enumeration;
		ArrayList<String> Certificates = new ArrayList<String>();

		try {
			enumeration = keyStore.aliases();

			String alias ="";

			while(enumeration.hasMoreElements()){
				alias = (String) enumeration.nextElement();

				if(keyStore.isKeyEntry(alias))
				{
					X509Certificate a = (X509Certificate) keyStore.getCertificate(alias);
					boolean[] usages = a.getKeyUsage();

					if( usages[0] == true)
					{
						Certificates.add(alias);
					}
				}
			}
		} catch (KeyStoreException e) {
			log(e.getMessage());
		}

		return Certificates;		

	}

	public static byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (length & 0xFF);
		bytes[1] = (byte) ((length >> 8) & 0xFF);
		bytes[2] = (byte) ((length >> 16) & 0xFF);
		bytes[3] = (byte) ((length >> 24) & 0xFF);
		return bytes;
	}

	private static class PasswordCallbackHandler implements CallbackHandler
	{
		Djasmime parent;

		public PasswordCallbackHandler(Djasmime parent)
		{
			this.parent = parent;
		}

		public void handle(Callback[] paramArrayOfCallback) throws IOException, UnsupportedCallbackException
		{
			for (int i = 0; i < paramArrayOfCallback.length; i++)
			{
				Callback localCallback = paramArrayOfCallback[i];
				if (!(localCallback instanceof PasswordCallback))
				{
					continue;
				}
				PasswordCallback localPasswordCallback = (PasswordCallback)localCallback;

				try
				{	
					JPasswordField pwd = new JPasswordField(20);
					
					int action = JOptionPane.showConfirmDialog(parent.signDialog, pwd,"Enter keystore password",JOptionPane.OK_CANCEL_OPTION);
					if(action < 0)
					{
						JOptionPane.showMessageDialog(parent.signDialog,"Cancel, X or escape key selected");
					}
					else
					{
						localPasswordCallback.setPassword(pwd.getPassword());

					}
				}
				catch(Exception e)
				{
					parent.log(e.getMessage());
					parent.signDialog.showMessage(e.getMessage(), true);

				}

			}
		}


	}
	
	public void log(String message) {
		System.err.println(message);
		System.err.flush();
	}
	
	public void quit(String message) {
		log(message);
		System.exit(0);
	}
	
	public static void main(String[] args) {
		Djasmime a = new Djasmime();

	}
}
