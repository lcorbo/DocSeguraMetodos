package ar.com.lpa.documentum;


import java.util.StringTokenizer;

public class LPATDABOF {

	private String clase = "";
	private String lifeCycle = "";
	private String workflow = "";
	private String propiedadCodigo = "";
	private String propiedadGrupos = "";
	private String propiedadSector = "";
	private String[] propiedadesUsuarios = null;
	private String[] gruposExtras = null;
	private String[] gruposFueraVigencia = null;
	private String propiedadReemplazos = "";
	private String propiedadVersion = "";
	private String estadoVigente = "";
	private String estadoInicial = "";
	private String propiedadCopia = "";
	private String propiedadRevision="";

	public LPATDABOF() {

	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public String getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(String lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	public String getPropiedadCodigo() {
		return propiedadCodigo;
	}

	public void setPropiedadCodigo(String propiedadCodigo) {
		this.propiedadCodigo = propiedadCodigo;
	}

	public String getPropiedadGrupos() {
		return propiedadGrupos;
	}

	public void setPropiedadGrupos(String propiedadGrupos) {
		this.propiedadGrupos = propiedadGrupos;
	}

	public String getPropiedadSector() {
		return propiedadSector;
	}

	public void setPropiedadSector(String propiedadSector) {
		this.propiedadSector = propiedadSector;
	}

	public String getPropiedadReemplazos() {
		return propiedadReemplazos;
	}

	public void setPropiedadReemplazos(String propiedadReemplazos) {
		this.propiedadReemplazos = propiedadReemplazos;
	}

	public String getPropiedadVersion() {
		return propiedadVersion;
	}

	public void setPropiedadVersion(String propiedadVersion) {
		this.propiedadVersion = propiedadVersion;
	}

	public String getEstadoVigente() {
		return estadoVigente;
	}

	public void setEstadoVigente(String estadoVigente) {
		this.estadoVigente = estadoVigente;
	}

	public String getEstadoInicial() {
		return estadoInicial;
	}

	public void setEstadoInicial(String estadoInicial) {
		this.estadoInicial = estadoInicial;
	}

	public String[] getPropiedadesUsuarios() {
		return propiedadesUsuarios;
	}

	public void setPropiedadesUsuarios(String propiedadesUsuarios) {
		String[] aux = null;
		if (propiedadesUsuarios != null) {
			StringTokenizer st = new StringTokenizer(propiedadesUsuarios, ";");
			aux = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				aux[i] = (String) st.nextElement();
				i++;
			}
		}
		this.propiedadesUsuarios = aux;
	}

	public String[] getGruposExtras() {
		return gruposExtras;
	}

	public void setGruposExtras(String gruposExtras) {
		String[] aux = null;
		if (gruposExtras != null) {
			StringTokenizer st = new StringTokenizer(gruposExtras, ";");
			aux = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				aux[i] = (String) st.nextElement();
				i++;
			}
		}
		this.gruposExtras = aux;
	}

	public String[] getGruposFueraVigencia() {
		return gruposFueraVigencia;
	}

	public void setGruposFueraVigencia(String gruposFueraVigencia) {
		String[] aux = null;
		if (gruposFueraVigencia != null) {
			StringTokenizer st = new StringTokenizer(gruposFueraVigencia, ";");
			aux = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				aux[i] = (String) st.nextElement();
				i++;
			}
		}
		this.gruposFueraVigencia = aux;
	}

	public String getPropiedadCopia() {
		return propiedadCopia;
	}

	public void setPropiedadCopia(String propiedadCopia) {
		this.propiedadCopia = propiedadCopia;
	}

	public String getPropiedadRevision() {
		return propiedadRevision;
	}

	public void setPropiedadRevision(String propiedadRevision) {
		this.propiedadRevision = propiedadRevision;
	}

}