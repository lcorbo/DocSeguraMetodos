package ar.com.lpa.documentum;

public class LPATDAWorkflowLogger {

	private String mensaje = "";
	private String pasoAnterior = "";
	private String usuario = "";
	private String idDocumento = "";
	private String codDocumento = "";
	private String idWorkflow = "";
	private String nombreWorkflow = "";
	private String version = "";

	public LPATDAWorkflowLogger() {

	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getPasoAnterior() {
		return pasoAnterior;
	}

	public void setPasoAnterior(String pasoAnterior) {
		this.pasoAnterior = pasoAnterior;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(String idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getCodDocumento() {
		return codDocumento;
	}

	public void setCodDocumento(String codDocumento) {
		this.codDocumento = codDocumento;
	}

	public String getIdWorkflow() {
		return idWorkflow;
	}

	public void setIdWorkflow(String idWorkflow) {
		this.idWorkflow = idWorkflow;
	}

	public String getNombreWorkflow() {
		return nombreWorkflow;
	}

	public void setNombreWorkflow(String nombreWorkflow) {
		this.nombreWorkflow = nombreWorkflow;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}