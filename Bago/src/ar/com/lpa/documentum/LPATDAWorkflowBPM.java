package ar.com.lpa.documentum;

import java.util.StringTokenizer;

public class LPATDAWorkflowBPM {

	private String workflow = "";
	private String packageAdjunto = "";
	private String packageBPM = "";
	private String claseBPM = "";
	private String[] propiedades = null;

	public LPATDAWorkflowBPM() {

	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	public String getPackageAdjunto() {
		return packageAdjunto;
	}

	public void setPackageAdjunto(String packageAdjunto) {
		this.packageAdjunto = packageAdjunto;
	}

	public String getPackageBPM() {
		return packageBPM;
	}

	public void setPackageBPM(String packageBPM) {
		this.packageBPM = packageBPM;
	}

	public String getClaseBPM() {
		return claseBPM;
	}

	public void setClaseBPM(String claseBPM) {
		this.claseBPM = claseBPM;
	}

	public String[] getPropiedades() {
		return propiedades;
	}

	public void setPropiedades(String propiedades) {
		String[] aux = null;
		if (propiedades != null) {
			StringTokenizer st = new StringTokenizer(propiedades, ";");
			aux = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				aux[i] = (String) st.nextElement();
				i++;
			}
		}
		this.propiedades = aux;
	}

	public boolean hasBPM() {
		boolean tiene = false;
		if (claseBPM != null) {
			if (!(claseBPM.equals("")))
				tiene = true;
		}
		return tiene;
	}

}