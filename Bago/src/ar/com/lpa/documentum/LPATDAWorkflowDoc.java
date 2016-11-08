package ar.com.lpa.documentum;

public class LPATDAWorkflowDoc {

	private int id = 0;
	private String clase = "";
	private String bookmark = "";
	private String propiedad = "";
	private String tipo = "";
	private boolean reemplazar = true;


	public LPATDAWorkflowDoc() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClase() {
		return clase;
	}

	public void setClase(String clase) {
		this.clase = clase;
	}

	public String getBookmark() {
		return bookmark;
	}

	public void setBookmark(String bookmark) {
		this.bookmark = bookmark;
	}

	public String getPropiedad() {
		return propiedad;
	}

	public void setPropiedad(String propiedad) {
		this.propiedad = propiedad;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	public boolean isReemplazar() {
		return reemplazar;
	}

	public void setReemplazar(boolean reemplazar) {
		this.reemplazar = reemplazar;
	}

}