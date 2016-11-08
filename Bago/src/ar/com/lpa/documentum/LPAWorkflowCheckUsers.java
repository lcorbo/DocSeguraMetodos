package ar.com.lpa.documentum;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class LPAWorkflowCheckUsers implements IDmMethod {

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

	private boolean chequeaUsuarios(IDfSysObject dmcDoc, LPATDABOF BOF)
			throws Exception {
		boolean valido = true;
		try {
			ArrayList<String> usuarios = new ArrayList<String>();
			for (int i = 0; i < BOF.getPropiedadesUsuarios().length; i++) {
				StringTokenizer st = new StringTokenizer(
						dmcDoc.getAllRepeatingStrings(
								BOF.getPropiedadesUsuarios()[i], ";"), ";");
				while (st.hasMoreTokens()) {
					String usuario = (String) st.nextElement();
					if (debug)
						logger.write("usuario: "
								+ usuario);
					if (usuarios.contains(usuario)) {
						valido = false;
					} else {
						usuarios.add(usuario);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		if (debug)
			logger.write("validos: "
					+ valido);
		return valido;
	}

	private void actualizaPerformers(IDfWorkflow workflow, IDfSysObject dmcDoc,
			ArrayList<LPATDAWorkflowTasks> tasks) throws Exception {
		try {
			if (tasks != null) {
				for (int l = 0; l < tasks.size(); l++) {
					LPATDAWorkflowTasks task = tasks.get(l);
					IDfList performers = new DfList();
					String usuarios = dmcDoc.getAllRepeatingStrings(
							task.getPropiedadUsuarios(), ";");
					StringTokenizer st = new StringTokenizer(usuarios, ";");
					while (st.hasMoreTokens()) {
						String usuario = (String) st.nextElement();
						if (debug)
							logger.write("Usuario: "
									+ usuario);
						performers.append(usuario);
					}
					workflow.setPerformers(task.getNombrePaso(), performers);
				}
			}
		} catch (Exception e) {
			throw e;
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
			LPATDABOF BOF = DB.getBOF(dmcDoc.getTypeName());

			IDfSysObject dmcBPM = ServiceBPM.getParametros();
			boolean valido = chequeaUsuarios(dmcDoc, BOF);
			dmcBPM.setBoolean("bpm_check_user", valido);
			dmcBPM.save();

			if (valido) {
				ArrayList<LPATDAWorkflowTasks> tasks = DB
						.getWorklfowTasks(process.getObjectName());
				actualizaPerformers(wf, dmcDoc, tasks);
			}

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