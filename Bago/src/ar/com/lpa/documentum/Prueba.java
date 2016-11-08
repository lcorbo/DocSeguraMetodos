package ar.com.lpa.documentum;

import org.apache.log4j.Logger;

public class Prueba {
	
	private LPAServiceLogger logger;
	
	public Prueba() throws Exception
	{
		//LPAServiceConfigurator props = new LPAServiceConfigurator("C:\\BagoJMS.properties");
		//logger = new LPAServiceLogger(props.getLog());
	}
	
	public void mensaje()
	{
		logger.write("Jojo");
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Prueba ejemplo=new Prueba();
			ejemplo.mensaje();
			ejemplo.logger.write("Prueba");
			ejemplo.logger.writeTrace(new Exception());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}

	}

}
