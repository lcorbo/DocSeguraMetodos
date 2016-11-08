package ar.com.lpa.documentum;

import java.util.Date;

public class LPATDAAttach {

	private int idRelacion = 0;
	private String idDocOrigen = "";
	private String idDocDestino = "";
	private String codigoDocOrigen = "";
	private String codigoDocDestino = "";
	private String versionDocOrigen = "";
	private String versionDocDestino = "";
	private String usuario = "";
	private Date fecha = null;

	public LPATDAAttach() {

	}

	public int getIdRelacion() {
		return idRelacion;
	}

	public void setIdRelacion(int idRelacion) {
		this.idRelacion = idRelacion;
	}

	public String getIdDocOrigen() {
		return idDocOrigen;
	}

	public void setIdDocOrigen(String idDocOrigen) {
		this.idDocOrigen = idDocOrigen;
	}

	public String getIdDocDestino() {
		return idDocDestino;
	}

	public void setIdDocDestino(String idDocDestino) {
		this.idDocDestino = idDocDestino;
	}

	public String getCodigoDocDestino() {
		return codigoDocDestino;
	}

	public void setCodigoDocDestino(String codigoDocDestino) {
		this.codigoDocDestino = codigoDocDestino;
	}

	public String getCodigoDocOrigen() {
		return codigoDocOrigen;
	}

	public void setCodigoDocOrigen(String codigoDocOrigen) {
		this.codigoDocOrigen = codigoDocOrigen;
	}

	public void setVersionDocDestino(String versionDocDestino) {
		this.versionDocDestino = versionDocDestino;
	}

	public String getVersionDocOrigen() {
		return versionDocOrigen;
	}

	public void setVersionDocOrigen(String versionDocOrigen) {
		this.versionDocOrigen = versionDocOrigen;
	}

	public String getVersionDocDestino() {
		return versionDocDestino;
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

}