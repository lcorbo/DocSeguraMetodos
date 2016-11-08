package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.methodserver.IDfMethod;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowPrepareMail implements IDfMethod {
	
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

	private static LPAServiceLogger logger = null;
	
	
	public IDfSysObject loadDocuments(IDfCollection pkgColl) throws Exception {
		try {
			IDfSysObject dmdoc=null;
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
						if (packageName.equals("t_fichas_tecnicas")) {
							dmdoc = dmc;
						}
					}
				}
			}
			pkgColl.close();
			return dmdoc;
		} catch (Exception e) {
			throw e;
		}
	}

	public int execute(Map arg0, PrintWriter arg1) throws Exception {
		logger = new LPAServiceLogger("C:\\LPALog.properties");
		initWorkflowParams(arg0);
		LPAServiceDataBase DB = null;
		IDfSessionManager sessionManager = null;
		IDfClientX clientx = new DfClientX();
		IDfLoginInfo li = clientx.getLoginInfo();
		li.setDomain("");
		li.setUser("Administrator");
		li.setPassword("Lpa23291");
		IDfClient client = clientx.getLocalClient();
		sessionManager = client.newSessionManager();
		sessionManager.setIdentity("DocBase",li);
		try {
			IDfId workitemID = new DfId(m_workitemId);
			session = sessionManager.getSession("DocBase");
			IDfWorkitem workitem = (IDfWorkitem) session.getObject(workitemID);

			if (workitem.getRuntimeState() == 0)
				workitem.acquire();

			if (debug)
				logger.write("Adquirio la tarea");

			IDfSysObject dmcDoc = loadDocuments(workitem.getPackages(""));
			
			String producto = dmcDoc.getString("nombre_producto");
			
			if (debug)
				logger.write("Producto: "+producto);
			
			ArrayList<String> direcciones = getDirecciones();
			
			enviarMail(direcciones,dmcDoc);
			
			return 0;

		} catch (Exception e) {
			arg1.write(e.getMessage());
			e.printStackTrace();
			logger.writeTrace(e);
			throw e;
		} finally {
			if (this.session != null)
				sessionManager.release(this.session);
		}
	}

	private void enviarMail(ArrayList<String> mails, IDfSysObject dmcDoc) throws DfException {
		// TODO Auto-generated method stub
		// Recipient's email ID needs to be mentioned.
		String fecha = dmcDoc.getTime("r_creation_date").toString();
		
		String path = dmcDoc.getString("r_folder_path");
		
		String nombre = dmcDoc.getString("object_name");
		
		String asunto = "Se ha publicado un nuevo documento de fichas tecnicas: "+nombre;
		
		String cuerpo = "El día "+fecha+" se ha publicado en "+path+" (link al documento) el documento "+nombre+".";
		
	      String to = "";

	      // Sender's email ID needs to be mentioned
	      String from = "usuario1@mail.com";

	      // Assuming you are sending email from localhost
	      String host = "w2003sql2005";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try{
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
	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	         }
	      }catch (MessagingException mex) {
	          mex.printStackTrace();
	       }
	}


	private ArrayList<String> getDirecciones() throws DfException {
		// TODO Auto-generated method stub
		ArrayList<String> resultado = new ArrayList<String>();
		IDfQuery cons = new DfQuery();
		cons.setDQL("select user_address from dm_user where user_name in (select users_names from dm_group where group_name = 'notificacion_escalado_vigentes' OR group_name = 'notificacion_lanza_vigentes');");
		IDfCollection col = null;
		col = cons.execute(this.session, 1);
		while(!col.next())
		{
			resultado.add(col.getString("user_address"));
		}
		return resultado;
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

}
