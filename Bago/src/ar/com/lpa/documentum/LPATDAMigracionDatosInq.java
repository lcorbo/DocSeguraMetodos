package ar.com.lpa.documentum;

import java.util.Date;

public class LPATDAMigracionDatosInq {

	protected Date fechaEdicion = null;
	protected Date fechaRevision = null;
	protected Date fechaAprobacion = null;
	protected Date fechaVigencia = null;
	protected Date fechaVencimiento = null;
	protected String titulo = "";
	protected String sector = "";
	protected String filial = "";

	public Date getFechaEdicion() {
		return fechaEdicion;
	}

	public void setFechaEdicion(Date fechaEdicion) {
		this.fechaEdicion = fechaEdicion;
	}

	public Date getFechaRevision() {
		return fechaRevision;
	}

	public void setFechaRevision(Date fechaRevision) {
		this.fechaRevision = fechaRevision;
	}

	public Date getFechaAprobacion() {
		return fechaAprobacion;
	}

	public void setFechaAprobacion(Date fechaAprobacion) {
		this.fechaAprobacion = fechaAprobacion;
	}

	public Date getFechaVigencia() {
		return fechaVigencia;
	}

	public void setFechaVigencia(Date fechaVigencia) {
		this.fechaVigencia = fechaVigencia;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getFilial() {
		return filial;
	}

	public void setFilial(String filial) {
		if (filial.equalsIgnoreCase("BIOGENESIS BAGO SA"))
			this.filial = "AR-GA (Garin)";
		else if (filial.equalsIgnoreCase("MONTE GRANDE"))
			this.filial = "AR-MG (Monte Grande)";
		else if (filial.equalsIgnoreCase("ANL"))
			this.filial = "AR-GA (Garin)";
		else if (filial.equalsIgnoreCase("BIOGENESIS BAGO"))
			this.filial = "AR-GA (Garin)";
		else if (filial.equalsIgnoreCase("BIOGENESIS"))
			this.filial = "AR-GA (Garin)";
	}
}