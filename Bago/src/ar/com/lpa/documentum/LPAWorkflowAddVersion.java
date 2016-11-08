package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowAddVersion implements IDmMethod {

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

	private void copiaRelaciones(LPAServiceDataBase DB,
			ArrayList<LPATDARelation> relations, String codigo, String version)
			throws Exception {
		try {
			for (int i = 0; i < relations.size(); i++) {
				LPATDARelation relacion = relations.get(i);
				logger.write("Relacion Nº "+ i + ": "+relacion.getCodigoDocOrigen() + " " + relacion.getVersionDocOrigen() + " - "+relacion.getCodigoDocDestino() + " " + relacion.getVersionDocDestino());
				if (relacion.getCodigoDocOrigen().equals(codigo))
					relacion.setVersionDocOrigen(version);
				else
					relacion.setVersionDocDestino(version);
				DB.insertRelation(relacion);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void copiaAdjuntos(LPAServiceDataBase DB,
			ArrayList<LPATDAAttach> attachs, String codigo, String version)
			throws Exception {
		try {
			for (int i = 0; i < attachs.size(); i++) {
				LPATDAAttach relacion = attachs.get(i);
				if (relacion.getCodigoDocOrigen().equals(codigo))
					relacion.setVersionDocOrigen(version);
				else
					relacion.setVersionDocDestino(version);
				DB.insertAttach(relacion);
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

			String version = dmcDoc.getString(BOF.getPropiedadVersion());
			ArrayList<LPATDARelation> relations = DB.getRelations(
					dmcDoc.getString(BOF.getPropiedadCodigo()), version);
			if(debug)
				logger.write("Nº of relations: "+relations.size());
			ArrayList<LPATDAAttach> attachs = DB.getAttachs(
					dmcDoc.getString(BOF.getPropiedadCodigo()), version);
			String nuevaVersion = "";
			if (workitem.getActivity().getObjectName().contains("minor")) {
				if (debug)
					logger.write("minorVersion: "
							+ version);
				int minorVersion = Integer.parseInt(version.substring(
						version.indexOf('.') + 1, version.length()));
				minorVersion = minorVersion + 1;
				String aux = version.substring(0, version.indexOf('.'));
				nuevaVersion = aux + "." + minorVersion;
				if (debug)
					logger.write("minorVersion: "
							+ nuevaVersion);
				dmcDoc.setString(BOF.getPropiedadVersion(), nuevaVersion);
			} else {
				if (debug)
					logger.write("majorVersion: "
							+ version);
				int majorVersion = Integer.parseInt(version.substring(0,
						version.indexOf('.')));
				majorVersion = majorVersion + 1;
				nuevaVersion = majorVersion + ".0";
				if (debug)
					logger.write("majorVersion: "
							+ nuevaVersion);
				dmcDoc.setString(BOF.getPropiedadVersion(), nuevaVersion);
			}
			dmcDoc.save();
			if (debug)
				logger.write("Version aumentada");

			copiaRelaciones(DB, relations,
					dmcDoc.getString(BOF.getPropiedadCodigo()), nuevaVersion);
			if (debug)
				logger.write("Copia las relaciones para la nueva version");

			copiaAdjuntos(DB, attachs,
					dmcDoc.getString(BOF.getPropiedadCodigo()), nuevaVersion);
			if (debug)
				logger.write("Copia los adjuntos para la nueva version");

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
