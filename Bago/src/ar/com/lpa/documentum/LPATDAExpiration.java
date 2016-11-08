package ar.com.lpa.documentum;

public class LPATDAExpiration {

	private String clase = "";
	private String propiedadVigencia = "";
	private String propiedadVencimiento = "";
	private int diasVencimiento = 0;
	private String propiedadNotificado = "";
	private int diasNotificacion = 0;
	private int diasPreVencimiento = 0;
	private String workflowVencimiento = "";
	private String estadoVigente = "";

	public LPATDAExpiration() {

	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public String getPropiedadVigencia() {
		return propiedadVigencia;
	}

	public void setPropiedadVigencia(String propiedadVigencia) {
		this.propiedadVigencia = propiedadVigencia;
	}

	public String getPropiedadVencimiento() {
		return propiedadVencimiento;
	}

	public void setPropiedadVencimiento(String propiedadVencimiento) {
		this.propiedadVencimiento = propiedadVencimiento;
	}

	public int getDiasVencimiento() {
		return diasVencimiento;
	}

	public void setDiasVencimiento(int diasVencimiento) {
		this.diasVencimiento = diasVencimiento;
	}

	public String getPropiedadNotificado() {
		return propiedadNotificado;
	}

	public void setPropiedadNotificado(String propiedadNotificado) {
		this.propiedadNotificado = propiedadNotificado;
	}

	public int getDiasNotificacion() {
		return diasNotificacion;
	}

	public void setDiasNotificacion(int diasNotificacion) {
		this.diasNotificacion = diasNotificacion;
	}

	public int getDiasPreVencimiento() {
		return diasPreVencimiento;
	}

	public void setDiasPreVencimiento(int diasPreVencimiento) {
		this.diasPreVencimiento = diasPreVencimiento;
	}

	public String getWorkflowVencimiento() {
		return workflowVencimiento;
	}

	public void setWorkflowVencimiento(String workflowVencimiento) {
		this.workflowVencimiento = workflowVencimiento;
	}

	public String getEstadoVigente() {
		return estadoVigente;
	}

	public void setEstadoVigente(String estadoVigente) {
		this.estadoVigente = estadoVigente;
	}

}