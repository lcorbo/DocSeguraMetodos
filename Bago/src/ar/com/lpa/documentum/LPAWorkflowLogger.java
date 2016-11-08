package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
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
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowLogger implements IDmMethod {

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

	private void grabaLog(LPAServiceDataBase DB, IDfSysObject dmc, String paso,
			String workflowID, String workflowName, LPATDABOF bof)
			throws Exception {
		IDfCollection col = null;
		try {
			LPATDAWorkflowLogger log = new LPATDAWorkflowLogger();
			if (debug)
				logger.write("Paso: " + paso);
			log = DB.getWorkflowLogger(paso);
			log.setIdWorkflow(workflowID);
			log.setNombreWorkflow(workflowName);
			if (debug) {
				logger.write("Mensaje: "
						+ log.getMensaje());
				logger.write("PasoAnterior: "
						+ log.getPasoAnterior());
			}
			if (dmc != null) {
				log.setIdDocumento(dmc.getObjectId().toString());
				log.setCodDocumento(dmc.getString(bof.getPropiedadCodigo()));
				log.setVersion(dmc.getString(bof.getPropiedadVersion()));
			}
			if (log.getPasoAnterior() == null) {
				log.setUsuario("SystemAdministrator");
				DB.insertWorkflowLog(log);
				if (debug)
					logger.write("Graba log del Workflow en Base de datos");
			} else {
				IDfQuery cons = new DfQuery();
				cons.setDQL("select a.r_performer_name from dmi_workitem_s a, dm_process_r b, dmi_queue_item_s c where a.r_workflow_id = '"
						+ workflowID
						+ "' and a.r_act_def_id = b.r_act_def_id and c.task_state <> 'dormant' and c.item_id = a.r_object_id and b.r_act_name = '"
						+ log.getPasoAnterior()
						+ "' and	a.r_creation_date = (select max(a.r_creation_date) from dmi_workitem_s a, dm_process_r b, dmi_queue_item_s c where a.r_workflow_id = '"
						+ workflowID
						+ "' and a.r_act_def_id = b.r_act_def_id and c.item_id = a.r_object_id and b.r_act_name = '"
						+ log.getPasoAnterior() + "')");
				if (debug)
					logger.write("DQL: "
							+ cons.getDQL());
				col = cons.execute(session, IDfQuery.DF_QUERY);
				while (col.next()) {
					if (debug)
						logger.write("Usuario: "
								+ col.getString("r_performer_name"));
					log.setUsuario(col.getString("r_performer_name"));
					DB.insertWorkflowLog(log);
					if (debug)
						logger.write("Graba log del Workflow en Base de datos");
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (col != null)
				col.close();
		}
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
			DB = new LPAServiceDataBase(logger);
			DB.conectar(props);
			IDfWorkitem workitem = (IDfWorkitem) session.getObject(workitemID);

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

			LPATDABOF bof = DB.getBOF(dmcDoc.getTypeName());

			grabaLog(DB, dmcDoc, workitem.getActivity().getObjectName(),
					wfId.toString(), process.getObjectName(), bof);

			workitem.complete();
			if (debug)
				logger.write("Completa la tarea");

		} catch (Exception e) {
			ostream.write(e.getMessage().getBytes());
			e.printStackTrace();
			logger.writeTrace(e);
			throw e;
		} finally {
			if (session != null)
				sessionManager.release(session);
			
				
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

		if (m_docbase == null || m_userName == null || m_ticket == null)
			return null;

		// now login
		IDfClient dfClient = DfClient.getLocalClient();

		if (dfClient != null) {
			IDfLoginInfo li = new DfLoginInfo();
			li.setUser(m_userName);
			li.setPassword(m_ticket);
			li.setDomain(null);

			IDfSessionManager sessionMgr = dfClient.newSessionManager();
			sessionMgr.setIdentity(m_docbase, li);
			return sessionMgr;
		}

		return null;
	}

}
