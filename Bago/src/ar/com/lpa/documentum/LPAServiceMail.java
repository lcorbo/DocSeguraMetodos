package ar.com.lpa.documentum;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class LPAServiceMail {

	protected LPAServiceLogger logger = null;
	protected LPATDAMail mail = null;

	private LPAServiceMail() {

	}

	public LPAServiceMail(LPATDAMail mail, LPAServiceLogger logger) {
		try {
			this.setMail(mail);
			this.setLogger(logger);
		} catch (Exception e) {
			logger.writeTrace(e);
		}
	}

	private void setMail(LPATDAMail mail) {
		this.mail = mail;
	}

	private LPATDAMail getMail() {
		return this.mail;
	}

	private void setLogger(LPAServiceLogger logger) {
		this.logger = logger;
	}

	private LPAServiceLogger getLogger() {
		return this.logger;
	}

	public void sendMails() {
		if (this.getMail().getTo().length > 0) {
			for (int i = 0; i < this.getMail().getTo().length; i++) {
				try {
					envioMail(this.getMail().getTo()[i]);
				} catch (Exception e) {
					this.getLogger().writeTrace(e);
				}
			}
		}
	}

	private void envioMail(String para) throws Exception {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", this.getMail().getHost());
		Session sesion = Session.getDefaultInstance(props, null);
		try {
			Message mensaje = new MimeMessage(sesion);
			mensaje.setSubject(this.getMail().getSubject());
			mensaje.setFrom(new InternetAddress(this.getMail().getFrom()));
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(
					para));
			mensaje.setContent(this.getMail().getBody(), "text/html");
			Transport.send(mensaje);
		} catch (Exception e) {
			throw e;
		}
	}

	public void sendMailsAttach(String nombre, File archivo) {
		if (this.getMail().getTo().length > 0) {
			for (int i = 0; i < this.getMail().getTo().length; i++) {
				try {
					envioMailAdjunto(nombre, archivo, this.getMail().getTo()[i]);
				} catch (Exception e) {
					this.getLogger().writeTrace(e);
				}
			}
		}
	}

	private void envioMailAdjunto(String nombre, File archivo, String para)
			throws Exception {
		Properties props = System.getProperties();
		props.put("mail.smtp.host", this.getMail().getHost());
		Session sesion = Session.getDefaultInstance(props, null);
		try {
			Message mensaje = new MimeMessage(sesion);
			MimeMultipart multiParte = new MimeMultipart();

			BodyPart texto = new MimeBodyPart();
			texto.setContent(this.getMail().getBody(), "text/html");
			multiParte.addBodyPart(texto);
			if (archivo != null) {
				BodyPart adjunto = new MimeBodyPart();
				DataSource source = new FileDataSource(archivo);
				adjunto.setDataHandler(new DataHandler(source));
				adjunto.setFileName(nombre);
				multiParte.addBodyPart(adjunto);
			}
			mensaje.setSubject(this.getMail().getSubject());
			mensaje.setFrom(new InternetAddress(this.getMail().getFrom()));
			mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(
					para));
			mensaje.setContent(multiParte);
			Transport.send(mensaje);
		} catch (Exception e) {
			throw e;
		}
	}
}