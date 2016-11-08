package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfActivity;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflowBuilder;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;

public class DesarrolloCrearEstructura implements IDmMethod{
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
				logger.debug("Carpeta " + name + " creada en " + root + " con ACL " + ACL);
		} catch (DfException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	
	public void execute(Map params, OutputStream ostream) throws Exception {
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
			
			IDfQuery cons = new DfQuery();
			
			IDfCollection col = null;
			
			
			cons.setDQL("select r_object_id, object_name , r_folder_path from carpeta_desarrollo where r_link_cnt = 0");
			
			col = cons.execute(this.session, 1);
			
			String acl = "";
			
			String ruta = "";
			
			while(col.next())
			{
				ruta = col.getString("r_folder_path");
				acl = "acl_desa_proyectos";
				IDfFolder folder = session.getFolderByPath(ruta);
				IDfACL dfACL=(IDfACL) session.getObjectByQualification("dm_acl where object_name='"+acl+"'");
				folder.setACL(dfACL);
				createFolder(ruta, "FIND", "acl_desa_proyectos_find");
				createFolder(ruta, "FIRE", "acl_desa_proyectos_fire");
				createFolder(ruta, "Documentacion de Desarrollo", "acl_desa_proyectos_docu_desarro");
				ruta += "/Documentacion de Desarrollo";
				createFolder(ruta, "Documentacion fuera de vigencia", "acl_desa_proyectos_fuera_vigenci");
				acl = "acl_desa_proyectos_docu_vigente";
				createFolder(ruta, "Documentacion VIGENTE", acl);
				createFolder(ruta+"/Documentacion VIGENTE","Materia Prima", acl);
				createFolder(ruta+"/Documentacion VIGENTE","Material de empaque", acl);
				createFolder(ruta+"/Documentacion VIGENTE","Semielaborado y Producto Terminado", acl);
				createFolder(ruta+"/Documentacion VIGENTE","Validaciones", acl);
				acl = "acl_desa_proyectos_lotes_entrega";
				createFolder(ruta,"Informacion de lotes entregados", acl);
				ruta += "/Informacion de lotes entregados";
				createFolder(ruta,"Batch records y CoAs", acl);
				createFolder(ruta,"DMF", acl);
				createFolder(ruta,"Estabilidad", acl);
				createFolder(ruta,"Estabilidad retrospectiva", acl);
				createFolder(ruta,"Perfiles de disolucion", acl);
				createFolder(ruta,"Varios", acl);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			throw e;
		} finally {
			if (this.session != null)
				sessionManager.release(this.session);
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
