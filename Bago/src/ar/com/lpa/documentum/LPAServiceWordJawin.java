package ar.com.lpa.documentum;

import java.io.File;

import org.jawin.DispatchPtr;
import org.jawin.win32.COINIT;
import org.jawin.win32.Ole32;

public class LPAServiceWordJawin {

	private DispatchPtr word = null;
	private DispatchPtr doc = null;

	public LPAServiceWordJawin() throws Exception {
		try {
			abreWord();
		} catch (Exception e) {
			throw e;
		}
	}

	public DispatchPtr getWord() {
		return word;
	}

	private void setWord(DispatchPtr word) {
		this.word = word;
	}

	public DispatchPtr getDoc() {
		return doc;
	}

	private void setDoc(DispatchPtr doc) {
		this.doc = doc;
	}

	private void abreWord() throws Exception {
		try {
			Ole32.CoInitialize(COINIT.MULTITHREADED);
			DispatchPtr app = new DispatchPtr("Word.Application");
			app.put("Visible", false);
			this.setWord(app);
		} catch (Exception e) {
			throw e;
		}
	}

	public void abreDoc(String path) throws Exception {
		try {
			DispatchPtr docs = (DispatchPtr) this.getWord().get("Documents");
			DispatchPtr doc = (DispatchPtr) docs.invoke("Open",
					new File(path).getAbsolutePath());
			this.setDoc(doc);
		} catch (Exception e) {
			throw e;
		}
	}

	public void cierraDoc() throws Exception {
		try {
			this.getDoc().invoke("Close");
		} catch (Exception e) {
			throw e;
		}
	}

	public void desprotegeWord(String password) throws Exception {
		try {
			this.getDoc().invoke("Unprotect", password);
			this.getDoc().invoke("Save");
		} catch (Exception e) {
			throw e;
		}
	}

	public void protegeDoc(String password) throws Exception {
		try {
			this.getDoc().invoke("Protect", 1, false, password, false);
			this.getDoc().invoke("Save");
		} catch (Exception e) {
			throw e;
		}
	}

	public void escribeBookmarkDoc(String bookmark, String texto,
			String marcador) throws Exception {
		try {
			DispatchPtr books = (DispatchPtr) this.getDoc().get("Bookmarks");
			DispatchPtr book = (DispatchPtr) books.invoke("Item", bookmark);
			if(bookmark.equalsIgnoreCase("Vigencia"))
			{
				DispatchPtr comp = (DispatchPtr) books.invoke("Item", "Estado");
				DispatchPtr rango = (DispatchPtr) comp.get("Range");
				String text = rango.get("Text").toString();
				if(text.equalsIgnoreCase("TempPre"))
					texto="";
			}
			DispatchPtr range = (DispatchPtr) book.get("Range");
			range.put("Text", texto);
			range.invoke("Select");
			DispatchPtr seleccion = (DispatchPtr) this.getWord().get(
					"Selection");
			DispatchPtr range2 = (DispatchPtr) seleccion.get("Range");
			books.invoke("Add", bookmark, range2);
			book = (DispatchPtr) books.invoke("Item", marcador);
			range = (DispatchPtr) book.get("Range");
			range.invoke("Select");
			this.getDoc().invoke("Save");
		} catch (Exception e) {
			throw e;
		}
	}

	public void imprimeDoc(String impresora) throws Exception {
		try {
			this.getWord().put("ActivePrinter", impresora);
			this.getDoc().invoke("PrintOut");
		} catch (Exception e) {
			throw e;
		}
	}

	public void cierraWord() throws Exception {
		try {
			this.getWord().invoke("Quit");
			Ole32.CoUninitialize();
		} catch (Exception e) {
			throw e;
		}
	}

}