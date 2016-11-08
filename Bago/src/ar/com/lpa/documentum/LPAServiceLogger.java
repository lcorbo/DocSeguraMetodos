package ar.com.lpa.documentum;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LPAServiceLogger {

	protected Logger log;

	private LPAServiceLogger() {

	}

	public LPAServiceLogger(String path) throws IOException {
		log = Logger.getLogger(LPAServiceLogger.class);
		PropertyConfigurator.configure(path);
	}

	private void setLog(Logger log) {
		this.log = log;
	}

	private Logger getLog() {
		return this.log;
	}

	public void write(String mensaje) {
		log.debug(mensaje);
	}

	public void writeTrace(String mensaje, Exception e) {
		log.error(mensaje, e);
	}

	public void writeTrace(Exception e) {
		log.error(e);

	}
}