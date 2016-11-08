package ar.com.lpa.documentum;

import java.util.Date;

public class LPATDAMigracionHistImpresiones {

	protected String codigo = "";
	protected String version = "";
	protected String usuario = "";
	protected Date fecha = null;
	protected int tipoCopia = 0;
	protected String comentario = "";
	protected String idDocumento = "";

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public int getTipoCopia() {
		return tipoCopia;
	}

	public void setTipoCopia(String tipoCopia) {
		if (tipoCopia.equals("printControlled"))
			this.tipoCopia = 1;
		else if (tipoCopia.equals("print"))
			this.tipoCopia = 2;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public String getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(String idDocumento) {
		this.idDocumento = idDocumento;
	}

}