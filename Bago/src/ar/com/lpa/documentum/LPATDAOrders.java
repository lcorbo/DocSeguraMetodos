package ar.com.lpa.documentum;

import java.util.StringTokenizer;

public class LPATDAOrders {

	private int ID = 0;
	private String nombreWorkflow = "";
	private String idDocumento = "";
	private String[] valoresPropiedadesBPM = null;

	public LPATDAOrders() {

	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
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

	public String[] getValoresPropiedadesBPM() {
		return valoresPropiedadesBPM;
	}

	public void setValoresPropiedadesBPM(String valoresPropiedadesBPM) {
		String[] aux = null;
		if (valoresPropiedadesBPM != null) {
			StringTokenizer st = new StringTokenizer(valoresPropiedadesBPM, ";");
			aux = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				aux[i] = (String) st.nextElement();
				i++;
			}
		}
		this.valoresPropiedadesBPM = aux;
	}

}