package ar.com.lpa.documentum;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;


public class LPAServiceWord {
	private ActiveXComponent word = null;
	private Dispatch doc = null;
	private String path = null;

	public LPAServiceWord() throws Exception {
		try {
			abreWord();
		} catch (Exception e) {
			throw e;
		}
	}

	public ActiveXComponent getWord() {
		return word;
	}

	private void setWord(ActiveXComponent word) {
		this.word = word;
	}

	public Dispatch getDoc() {
		return doc;
	}

	private void setDoc(Dispatch doc) {
		this.doc = doc;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private void abreWord() throws Exception {
		try {
			ActiveXComponent oWord = new ActiveXComponent("Word.Application"); 
		    oWord.setProperty("Visible", new Variant(false));
			this.setWord(oWord);
			this.setPath(path);
		} catch (Exception e) {
			throw e;
		}
	}

	public void abreDoc(String path) throws Exception {
		try {
			Dispatch oDocuments = this.getWord().getProperty("Documents").toDispatch(); 
		    Dispatch oDocument = Dispatch.call(oDocuments, "Open", path).toDispatch(); 
			this.setDoc(oDocument);
			
		} catch (Exception e) {
			throw e;
		}
	}


	public void cierraDoc() throws Exception {
		try {
			Dispatch.call(this.getDoc(), "Close", new Variant(true)); 
		} catch (Exception e) {
			throw e;
		}
	}

	public void desprotegeWord(String password) throws Exception {
		try {
			Dispatch.call(this.getDoc(),"Unprotect", password);
			Dispatch.call(this.getDoc(), "Save");
		} catch (Exception e) {
			throw e;
		}
	}

	public void protegeDoc(String password) throws Exception {
		try {
			Dispatch.call(this.getDoc(),"Save");
			Dispatch.call(this.getDoc(),"Protect", 2, false, password, false);
		} catch (Exception e) {
			throw e;
		}
	}

	public void escribeBookmarkDoc(String bookmark, String texto,
			String marcador, boolean reemplazar) throws Exception {
		String reemp = "";
		try {
			Dispatch books = Dispatch.call(this.getDoc(),"Bookmarks").getDispatch();
			Dispatch book = Dispatch.call(this.getDoc(), "Bookmarks", bookmark).getDispatch(); 
			if(bookmark.equalsIgnoreCase("Vigencia"))
			{
				Dispatch comp = Dispatch.call(this.getDoc(), "Bookmarks", "Estado").getDispatch(); 
				Dispatch rango = Dispatch.get(comp,"Range").getDispatch();
				String text = Dispatch.get(rango,"Text").toString();
				if(text.equalsIgnoreCase("TempPre"))
					texto="";
			}
			Dispatch range = Dispatch.get(book,"Range").getDispatch();
			if(!reemplazar)
			{
				reemp = Dispatch.get(range,"Text").toString();
				if(!reemp.isEmpty())
					reemp += ";";
			}
			Dispatch.put(range,"Text", reemp+texto);
			Dispatch.call(range,"Select");
			Dispatch seleccion = Dispatch.get(this.getWord(),"Selection").getDispatch();
			Dispatch range2 = Dispatch.get(seleccion,"Range").getDispatch();
			Dispatch.call(books,"Add", bookmark, range2);
			book = Dispatch.call(this.getDoc(),"Bookmarks", marcador).getDispatch();
			range = Dispatch.get(book,"Range").getDispatch();
			Dispatch.call(range,"Select");
			Dispatch.call(this.getDoc(), "Save");
		} catch (Exception e) {
			throw e;
		}
	}

	public void imprimeDoc(String impresora) throws Exception {
		try {
			Dispatch.put(this.getDoc(),"ActivePrinter", impresora);
			Dispatch.call(this.getDoc(),"PrintOut");
		} catch (Exception e) {
			throw e;
		}
	}

	public void cierraWord() throws Exception {
		try {
		    this.getWord().invoke("Quit", new Variant[0]);
		} catch (Exception e) {
			throw e;
		}
	}

}
