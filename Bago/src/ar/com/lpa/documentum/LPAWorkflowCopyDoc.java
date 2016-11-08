package ar.com.lpa.documentum;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfProcess;
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
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class LPAWorkflowCopyDoc implements IDmMethod {

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

	private static boolean debug = false;

	private static LPAServiceLogger logger = null;

	private IDfACL crearACL(IDfSysObject child, LPATDABOF BOF) throws Exception {
		IDfACL acl = null;
		try {
			if (debug)
				logger.write("Comienza la generacion de la ACL");
			acl = (IDfACL) session.newObject("dm_acl");
			acl.setObjectName("acl_" + child.getObjectId().toString());
			String grupos = child.getAllRepeatingStrings(
					BOF.getPropiedadGrupos(), ";");
			if (debug)
				logger.write("grupos: "
						+ grupos);
			StringTokenizer st = new StringTokenizer(grupos, ";");
			while (st.hasMoreTokens()) {
				String grupo = (String) st.nextElement();
				acl.grant(grupo, IDfACL.DF_PERMIT_READ, null);
			}
			String[] gruposExtras = BOF.getGruposExtras();
			if (gruposExtras != null) {
				for (int j = 0; j < gruposExtras.length; j++) {
					acl.grant(gruposExtras[j], IDfACL.DF_PERMIT_READ, null);
				}
			}
			acl.grant(child.getString(BOF.getPropiedadSector()),
					IDfACL.DF_PERMIT_READ, null);
			acl.grant(this.m_userName, IDfACL.DF_PERMIT_DELETE,
					IDfACL.DF_XPERMIT_DELETE_OBJECT_STR);
			acl.grant("dm_world", IDfACL.DF_PERMIT_NONE, null);
			acl.grant("dm_owner", IDfACL.DF_PERMIT_READ, null);
			acl.save();
			if (debug)
				logger.write("Se genero correctamente la ACL");
		} catch (Exception e) {
			throw e;
		}
		return acl;
	}

	public void execute(Map params, OutputStream ostream) throws Exception {
		LPAServiceConfigurator props = new LPAServiceConfigurator();
		logger = new LPAServiceLogger(props.getLog());
		debug = props.getDebug();
		initWorkflowParams(params);
		IDfSessionManager sessionManager = login();
		LPAServiceDataBase DB = null;
		try {
			IDfId workitemID = new DfId(m_workitemId);
			session = sessionManager.getSession(m_docbase);
			IDfWorkitem workitem = (IDfWorkitem) session.getObject(workitemID);
			DB = new LPAServiceDataBase(logger);
			DB.conectar(props);
			if (workitem.getRuntimeState() == 0)
				workitem.acquire();

			if (debug)
				logger.write("Adquirio la tarea");

			IDfId wfId = workitem.getWorkflowId();
			IDfWorkflow wf = (IDfWorkflow) session.getObject(wfId);
			IDfId pId = wf.getProcessId();
			IDfProcess process = (IDfProcess) session.getObject(pId);
			LPATDAWorkflowBPM BPM = DB.getWorkflowBPM(process.getObjectName());
			LPAServiceBPM ServiceBPM = new LPAServiceBPM(session, logger,
					debug, BPM);

			ServiceBPM.loadDocuments(workitem.getPackages(""));

			IDfSysObject dmcDoc = ServiceBPM.getDocumento();

			LPATDABOF BOF = new LPATDABOF();
			BOF = DB.getBOF(dmcDoc.getTypeName());
			IDfId newDocIdObj = dmcDoc.saveAsNew(true);
			IDfSysObject newDmcDoc = (IDfSysObject) session
					.getObject(newDocIdObj);
			newDmcDoc.setBoolean(BOF.getPropiedadCopia(), true);
			newDmcDoc.setACL(crearACL(newDmcDoc, BOF));
			newDmcDoc.save();
			if (debug)
				logger.write("documento duplicado");

			workitem.complete();
			if (debug)
				logger.write("Completa la tarea");

		} catch (Exception e) {
			ostream.write(e.getMessage().getBytes());
			e.printStackTrace();
			logger.writeTrace(e);
			throw e;
		} finally {
			if (this.session != null)
				sessionManager.release(this.session);
			
				
			if (DB != null)
				DB.desconectar();
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