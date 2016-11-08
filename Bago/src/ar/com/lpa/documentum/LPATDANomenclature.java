package ar.com.lpa.documentum;

import java.util.StringTokenizer;

public class LPATDANomenclature {

	private boolean numerador = false;
	private String[] propiedades = null;
	private String[] tipoPropiedades = null;
	private String propiedadCodigo = "";
	private String propiedadNumerador = "";

	public LPATDANomenclature() {

	}

	public boolean isNumerador() {
		return numerador;
	}

	public void setNumerador(String numerador) {
		if (numerador.equals("SI"))
			this.numerador = true;
		else
			this.numerador = false;
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

	public String[] getTipoPropiedades() {
		return tipoPropiedades;
	}

	public void setTipoPropiedades(String tipoPropiedades) {
		String[] aux = null;
		if (tipoPropiedades != null) {
			StringTokenizer st = new StringTokenizer(tipoPropiedades, ";");
			aux = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				aux[i] = (String) st.nextElement();
				i++;
			}
		}
		this.tipoPropiedades = aux;
	}

	public String getPropiedadCodigo() {
		return propiedadCodigo;
	}

	public void setPropiedadCodigo(String propiedadCodigo) {
		this.propiedadCodigo = propiedadCodigo;
	}

	public String getPropiedadNumerador() {
		return propiedadNumerador;
	}

	public void setPropiedadNumerador(String propiedadNumerador) {
		this.propiedadNumerador = propiedadNumerador;
	}

}