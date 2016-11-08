package ar.com.lpa.documentum;

public class LPATDAWorkflowComments {

	private String nombreWorkflow = "";
	private String idDocumento = "";
	private String codDocumento = "";
	private String version = "";
	private String idComentario = "";

	public LPATDAWorkflowComments() {

	}

	public String getNombreWorkflow() {
		return nombreWorkflow;
	}

	public void setNombreWorkflow(String nombreWorkflow) {
		this.nombreWorkflow = nombreWorkflow;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIdComentario() {
		return idComentario;
	}

	public void setIdComentario(String idComentario) {
		this.idComentario = idComentario;
	}

}