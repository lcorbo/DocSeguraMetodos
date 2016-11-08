package ar.com.lpa.documentum;

public class LPATDAWorkflowTasks {

	private String nombreWorkflow = "";
	private String nombrePaso = null;
	private String propiedadUsuarios = null;
	private String documento = "";

	public LPATDAWorkflowTasks() {

	}

	public String getNombreWorkflow() {
		return nombreWorkflow;
	}

	public void setNombreWorkflow(String nombreWorkflow) {
		this.nombreWorkflow = nombreWorkflow;
	}

	public String getNombrePaso() {
		return nombrePaso;
	}

	public void setNombrePaso(String nombrePaso) {
		this.nombrePaso = nombrePaso;
	}

	public String getPropiedadUsuarios() {
		return propiedadUsuarios;
	}

	public void setPropiedadUsuarios(String propiedadUsuarios) {
		this.propiedadUsuarios = propiedadUsuarios;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

}