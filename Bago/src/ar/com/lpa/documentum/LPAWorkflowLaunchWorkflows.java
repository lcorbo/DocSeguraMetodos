package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

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
import com.documentum.fc.common.IDfValue;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowLaunchWorkflows implements IDmMethod {

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

	private IDfSysObject obtieneDoc(String clase, String propiedadCodigo,
			String codigo, String estado, String propiedadCopia)
			throws Exception {
		IDfSysObject doc = null;
		IDfCollection col = null;
		try {
			IDfQuery cons = new DfQuery();
			cons.setDQL("select dm_dbo.dm_sysobject_s.r_object_id from dm_dbo.dm_sysobject_s, dm_dbo.dm_policy_r, "
					+ clase
					+ " where "
					+ clase
					+ "."
					+ propiedadCodigo
					+ " = '"
					+ codigo
					+ "' and "
					+ clase
					+ "."
					+ propiedadCopia
					+ " = 0 and dm_dbo.dm_sysobject_s.r_policy_id = dm_dbo.dm_policy_r.r_object_id and dm_dbo.dm_policy_r.i_state_no = dm_dbo.dm_sysobject_s.r_current_state and dm_dbo.dm_sysobject_s.r_object_id = "
					+ clase
					+ ".r_object_id and dm_dbo.dm_policy_r.state_name = '"
					+ estado + "'");
			if (debug)
				logger.write("DQL: "
						+ cons.getDQL());
			col = cons.execute(session, IDfQuery.DF_QUERY);
			if (col.next()) {
				String id = col.getString("r_object_id");
				doc = (IDfSysObject) session.getObject(new DfId(id));
			}
			col.close();
		} catch (Exception e) {
			throw e;
		} finally {
			if (col != null) {
				try {
					col.close();
				} catch (Exception e) {
					logger.writeTrace(e);
					throw e;
				}
			}
		}
		return doc;
	}

	private void insertaOrden(LPAServiceDataBase DB, String nombreWorkflow,
			IDfSysObject dmcDoc) throws Exception {
		LPATDAWorkflowBPM BPM = null;
		try {
			BPM = DB.getWorkflowBPM(nombreWorkflow);
			String propiedades = "";
			if (BPM.getPropiedades() != null) {
				if (debug)
					logger.write("tiene propiedades BPM");
				for (int i = 0; i < BPM.getPropiedades().length; i++) {
					String propiedad = BPM.getPropiedades()[i];
					if (debug)
						logger.write("nombrePropiedad: "
								+ propiedad);
					IDfValue valor = dmcDoc.getValue(propiedad);
					int tipo = valor.getDataType();
					switch (tipo) {
					case IDfValue.DF_BOOLEAN:
						propiedades = propiedades
								+ dmcDoc.getBoolean(propiedad) + ";";
						break;
					case IDfValue.DF_DOUBLE:
						propiedades = propiedades + dmcDoc.getDouble(propiedad)
								+ ";";
						break;
					case IDfValue.DF_INTEGER:
						propiedades = propiedades + dmcDoc.getInt(propiedad)
								+ ";";
						break;
					case IDfValue.DF_STRING:
						propiedades = propiedades + dmcDoc.getString(propiedad)
								+ ";";
						break;
					}
				}
				propiedades = propiedades
						.substring(0, propiedades.length() - 1);
				if (debug)
					logger.write("valorPropiedades: "
							+ propiedades);
			}
			DB.insertWorkflowOrden(nombreWorkflow, dmcDoc.getObjectId()
					.toString(), propiedades, "NO");
		} catch (Exception e) {
			throw e;
		}
	}

	private void insertaOrdenes(LPAServiceDataBase DB, IDfSysObject dmcDoc,
			LPATDABOF BOF, String workflow, LPAServiceConfigurator props)
			throws Exception {
		try {
			String reemplazos = dmcDoc.getAllRepeatingStrings(
					BOF.getPropiedadReemplazos(), ";");
			if (debug)
				logger.write("documentos: "
						+ reemplazos);
			StringTokenizer st = new StringTokenizer(reemplazos, ";");
			while (st.hasMoreTokens()) {
				insertaOrden(
						DB,
						workflow,
						obtieneDoc(props.getClase(), BOF.getPropiedadCodigo(),
								(String) st.nextElement(),
								BOF.getEstadoVigente(), BOF.getPropiedadCopia()));
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
			insertaOrdenes(DB, dmcDoc, BOF, workitem.getActivity()
					.getObjectName(), props);

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