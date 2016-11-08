package ar.com.lpa.documentum;

public class LPATDAMail {

	protected String[] to;
	protected String from;
	protected String subject;
	protected String body;
	protected String host;
	protected String destinatarios = "";
	protected String propiedadBPM = "";

	private LPATDAMail() {

	}

	public LPATDAMail(LPAServiceConfigurator props) {
		this.host = props.getHostSMTP();
		this.from = props.getFrom();
	}

	public String[] getTo() {
		return to;
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDestinatarios() {
		return destinatarios;
	}

	public void setDestinatarios(String destinatarios) {
		this.destinatarios = destinatarios;
	}

	public String getPropiedadBPM() {
		return propiedadBPM;
	}

	public void setPropiedadBPM(String propiedadBPM) {
		this.propiedadBPM = propiedadBPM;
	}

}