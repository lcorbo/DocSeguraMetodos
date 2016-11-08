package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowCheckFolder implements IDmMethod{
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
			if (this.debug)
				this.logger.write("recorre los package");
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
							if (this.debug)
								this.logger.write("asigna el doc adjunto");
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
	
	public void createFolder(String root, String name, String ACL) throws DfException
	{
		IDfSysObject folder = null;
		try {
			folder = (IDfSysObject) session.newObject("dm_folder");
			folder.setObjectName(name);
			folder.link("root");
			IDfACL dfACL=(IDfACL) session.getObjectByQualification("dm_acl where object_name='"+ACL+"'");
			folder.setACL(dfACL);
			folder.save();
			if (debug)
				logger.write("Carpeta " + name + " creada en " + root + " con ACL" + ACL);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	public void execute(Map params, OutputStream ostream) throws Exception {
		LPAServiceConfigurator props = new LPAServiceConfigurator();
		logger = new LPAServiceLogger(props.getLog());
		debug = props.debug;
		initWorkflowParams(params);
		IDfSessionManager sessionManager = login();
		try {
			IDfId workitemID = new DfId(m_workitemId);
			session = sessionManager.getSession(m_docbase);
			IDfWorkitem workitem = (IDfWorkitem) session.getObject(workitemID);

			if (workitem.getRuntimeState() == 0)
				workitem.acquire();

			if (debug)
				logger.write("Adquirio la tarea");

			IDfSysObject dmcDoc = loadDocuments(workitem.getPackages(""));
			
			String producto = dmcDoc.getString("nombre_producto");
			
			if (debug)
				logger.write("Producto: "+producto);
			
			IDfQuery cons = new DfQuery();
			
			IDfCollection col = null;
			
			String ruta = "/Fichas Tecnicas";
			
			cons.setDQL("select object_name from dm_folder where folder('"+ruta+"/"+producto+"')");
			
			col = cons.execute(this.session, 1);
			
			if(!col.next())
			{
				if (debug)
					logger.write("No existe la carpeta");
				createFolder(ruta, producto, "acl_fichas_tecnicas");
				ruta += producto;
				createFolder(ruta, "Escalado","acl_fichas_tecnicas");
				String escalado = ruta + "/Escalado";
				createFolder(escalado,"Antecedentes", "acl_ft_escalado_antece");
				createFolder(escalado,"Vigentes", "acl_ft_escalado_vigentes");
				createFolder(ruta,"Lanzamiento", "acl_fichas_tecnicas");
				String lanzamiento = ruta + "/Lanzamiento";
				createFolder(lanzamiento,"Antecedentes", "acl_ft_lanza_ante");
				createFolder(lanzamiento,"Vigentes", "acl_ft_lanza_vigente");
			}

		} catch (Exception e) {
			ostream.write(e.getMessage().getBytes());
			e.printStackTrace();
			logger.writeTrace(e);
			throw e;
		} finally {
			if (this.session != null)
				sessionManager.release(this.session);
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
	
}
