package ar.com.lpa.documentum;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfDocument;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.methodserver.IDfMethod;
import com.documentum.mthdservlet.IDmMethod;

public class FichasTecnicasCrearCarpetas implements IDfMethod {

	protected IDfSessionManager m_sessionMgr = null;

	protected String m_docbase = null;

	protected String m_userName = null;

	protected String m_workitemId = null;

	protected String m_ticket = null;

	private static final String USER_KEY = "user";

	private static final String DOCBASE_KEY = "docbase_name";

	private static final String WORKITEM_KEY_2 = "workitemId";

	private static final String TICKET_KEY = "ticket";

	private static final String WORKITEM_KEY = "packageId";

	private IDfSession session = null;

	private static boolean debug = true;

	private static Logger logger = null;
	
	private LPAProperties props = null;
	
	
	public IDfSysObject loadDocuments(IDfCollection pkgColl) throws Exception {
		try {
			IDfSysObject dmdoc=null;
			if (this.debug)
				this.logger.debug("Recorre los packages");
			while (pkgColl.next()) {

				IDfId packageId = pkgColl.getId("r_object_id");
				String docId = pkgColl.getString("r_component_id");
				String packageName = pkgColl.getString("r_package_name");
				int docCount = pkgColl.getValueCount("r_component_id");

				for (int i = 0; i <= (docCount - 1); i++) {
					IDfId docIdObj = pkgColl
							.getRepeatingId("r_component_id", i);

					if (docIdObj != null) {

						IDfId sysobjID = new DfId(docId);
						IDfSysObject dmc = (IDfSysObject) session
								.getObject(sysobjID);
								this.logger.debug("asigna el doc adjunto");
							dmdoc = dmc;
					}
				}
			}
			pkgColl.close();
			return dmdoc;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void createFolder(String root, String name, String ACL) throws DfException
	{
		IDfSysObject folder = null;
		try {
			folder = (IDfSysObject) session.newObject("dm_folder");
			folder.setObjectName(name);
			folder.link(root);
			IDfACL dfACL=(IDfACL) session.getObjectByQualification("dm_acl where object_name='"+ACL+"'");
			folder.setACL(dfACL);
			folder.save();
			if (debug)
				logger.debug("Carpeta " + name + " creada en " + root + " con ACL" + ACL);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	protected void initWorkflowParams(Map params) {

		// get the 4 WF-related parameters always passed in by Server
		Set keys = params.keySet();
		Iterator iter = keys.iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			if ((key == null) || (key.length() == 0)) {
				continue;
			}
			String[] value = (String[]) params.get(key);

			if (key.equalsIgnoreCase(USER_KEY))
				m_userName = (value.length > 0) ? value[0] : "";
			else if (key.equalsIgnoreCase(DOCBASE_KEY))
				m_docbase = (value.length > 0) ? value[0] : "";
			else if (key.equalsIgnoreCase(WORKITEM_KEY_2))
				m_workitemId = (value.length > 0) ? value[0] : "";
			else if (key.equalsIgnoreCase(WORKITEM_KEY))
				m_workitemId = (value.length > 0) ? value[0] : "";
			else if (key.equalsIgnoreCase(TICKET_KEY))
				m_ticket = (value.length > 0) ? value[0] : "";
		}
	}

	protected IDfSessionManager login() throws DfException {
		if ((this.m_docbase == null) || (this.m_userName == null)
				|| (this.m_ticket == null)) {
			return null;
		}

		IDfClient dfClient = DfClient.getLocalClient();

		if (dfClient != null) {
			IDfLoginInfo li = new DfLoginInfo();
			li.setUser(this.m_userName);
			li.setPassword(this.m_ticket);
			li.setDomain(null);

			IDfSessionManager sessionMgr = dfClient.newSessionManager();
			sessionMgr.setIdentity(this.m_docbase, li);
			return sessionMgr;
		}

		return null;
	}

	@Override
	public int execute(Map arg0, PrintWriter arg1) throws Exception {
		//LPAServiceConfigurator props = new LPAServiceConfigurator();
		//logger = new LPAServiceLogger(props.getLog());
		//debug = props.debug;
		initWorkflowParams(arg0);
		logger = Logger.getLogger(this.getClass());
		props = new LPAProperties();
		PropertyConfigurator.configure(props.get("log"));
		IDfSessionManager sessionManager = null;
		IDfClientX clientx = new DfClientX();
		IDfLoginInfo li = clientx.getLoginInfo();
		li.setDomain("");
		li.setUser(props.get("user"));
		li.setPassword(props.getEncrypted("pass"));
		IDfClient client = clientx.getLocalClient();
		sessionManager = client.newSessionManager();
		sessionManager.setIdentity(props.get("docbase"),li);
		try {
			IDfId workitemID = new DfId(m_workitemId);
			session = sessionManager.getSession(props.get("docbase"));
			IDfWorkitem workitem = (IDfWorkitem) session.getObject(workitemID);

			if (workitem.getRuntimeState() == 0)
				workitem.acquire();

			if (debug)
				;
				logger.debug("Adquirio la tarea");

			IDfSysObject dmcDoc = loadDocuments(workitem.getPackages(""));
			
			dmcDoc.isDeleted();
			
			String producto = dmcDoc.getString("producto");
			
			if (debug)
				logger.debug("Producto: "+producto);
			
			IDfQuery cons = new DfQuery();
			
			IDfCollection col = null;
			
			String ruta = props.get("cabinet");
			
			cons.setDQL("select object_name from dm_folder where folder('"+ruta+"/"+producto+"')");
			
			col = cons.execute(this.session, 1);
			
			if(!col.next())
			{
				if (debug)
					logger.debug("No existe la carpeta");
				createFolder(ruta, producto, "acl_fichas_tecnicas");
				ruta += "/" + producto;
				createFolder(ruta, "Escalado","acl_fichas_tecnicas");
				String escalado = ruta + "/Escalado";
				createFolder(escalado,"Antecedentes", "acl_ft_escalado_antece");
				createFolder(escalado,"Vigentes", "acl_ft_escalado_vigentes");
				createFolder(ruta,"Lanzamiento", "acl_fichas_tecnicas");
				String lanzamiento = ruta + "/Lanzamiento";
				createFolder(lanzamiento,"Antecedentes", "acl_ft_lanza_ante");
				createFolder(lanzamiento,"Vigentes", "acl_ft_lanza_vigente");
				
				String estado = dmcDoc.getString("estado");
				
				dmcDoc.setBoolean("carpeta_creada", true);
				
				if(estado.equalsIgnoreCase("Vigentes"))
				{
					ArrayList<String> direcciones = getDirecciones();
				
					enviarMail(direcciones,dmcDoc);
				}
			}
			return 0;

		} catch (Exception e) {
			//arg1.write(e.getMessage().getBytes().toString());
			e.printStackTrace ();
			logger.error("Error - ",e);
			return 1;
		} finally {
			if (this.session != null)
				sessionManager.release(this.session);
		}
	}
	
	private void enviarMail(ArrayList<String> mails, IDfSysObject dmcDoc) throws Exception {
		// TODO Auto-generated method stub
		// Recipient's email ID needs to be mentioned.
	    try{
		String fecha = dmcDoc.getTime("r_creation_date").toString();
		
		String url = props.get("url")+dmcDoc.getId("r_object_id");
		
		//String path = dmcDoc.getString("r_folder_path");
		
		String nombre = dmcDoc.getString("object_name");
		
		logger.debug("Documento: "+nombre);
		
		String asunto = "Se ha publicado un nuevo documento de fichas tecnicas: "+nombre;
		
		String cuerpo = "El día "+fecha+" se ha publicado en "+ url +" el documento "+nombre+".";
		
	    String to = "";
	      

	      // Sender's email ID needs to be mentioned
	      //String user = props.get("userSMTP");

	      // Assuming you are sending email from localhost
	      String host = props.get("hostSMTP");
	      
	      //String pass = props.getEncrypted("passSMTP");
	      
	      String from = props.get("mailSMTP");

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);
	      //properties.setProperty("mail.smtp.user", user);
	      //properties.setProperty("mail.smtp.password", pass);
	      //properties.setProperty("mail.smtp.auth", "true"); 
	      
	      /*Authenticator auth = new Authenticator() {
  			protected PasswordAuthentication getPasswordAuthentication() {
  				String user = props.get("userSMTP");
  				String pass = "";
  				try{
  					pass = props.getEncrypted("passSMTP");
  				}
  				catch(Exception e)
  				{
  					logger.debug("Error: ",e);
  				}
				return new PasswordAuthentication(user, pass);
			}
		  };*/

	      // Get the default Session object.
	      Session session = Session.getInstance(properties,null);

	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         // Set Subject: header field
	         message.setSubject(asunto);

	         // Now set the actual message
	         message.setText(cuerpo);
	         
	         for(String mail:mails)
	         {
	        	 to = mail;
	        	 logger.debug("Mail: "+mail);
	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Send message
	         Transport.send(message);
	         logger.debug("Sent message successfully....");
	         }
	      }catch (Exception e) {
	    	  throw e;
	    	  
	       }
	}


	private ArrayList<String> getDirecciones() throws Exception {
		// TODO Auto-generated method stub
		try {
			ArrayList<String> resultado = new ArrayList<String>();
			IDfQuery cons = new DfQuery();
			String query = "select user_address from dm_user where user_name in (select users_names from dm_group where group_name in ("+props.get("grupos")+"));";
			cons.setDQL(query);
			IDfCollection col = null;
			col = cons.execute(this.session, 1);
			while(col.next())
			{
				resultado.add(col.getString("user_address"));
			}
			logger.debug("Mails a mandar:" + resultado.size());
			return resultado;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

}
