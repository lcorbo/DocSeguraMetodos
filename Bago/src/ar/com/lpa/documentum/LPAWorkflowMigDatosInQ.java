package ar.com.lpa.documentum;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
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
import com.documentum.fc.common.IDfTime;
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LPAWorkflowMigDatosInQ implements IDmMethod {

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

	private void cargaDatos(LPAServiceDataBase DB, IDfSysObject dmcDoc)
			throws Exception {
		try {
			LPATDABOF bof = DB.getBOF(dmcDoc.getTypeName());
			String version = dmcDoc.getString(bof.getPropiedadVersion());
			int majorVersion = Integer.parseInt(version.substring(0,
					version.indexOf('.')));
			DecimalFormat formatter = new DecimalFormat("#00.###");
			version = formatter.format(majorVersion);
			LPATDAMigracionDatosInq datos = DB.getDatosInQ(
					dmcDoc.getString(bof.getPropiedadCodigo()), version);
			Format formato = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			IDfClientX clientx = new DfClientX();
			IDfTime fecha = null;
			if (datos.getFechaEdicion() != null) {
				fecha = clientx.getTime(
						formato.format(datos.getFechaEdicion()),
						"mm/dd/yyyy hh:mi:ss");
				dmcDoc.setTime("p_feditor", fecha);
				if (debug)
					logger.write("graba Fecha Edicion");
			}
			if (datos.getFechaRevision() != null) {
				fecha = clientx.getTime(
						formato.format(datos.getFechaRevision()),
						"mm/dd/yyyy hh:mi:ss");
				dmcDoc.setTime("p_frevisor", fecha);
				if (debug)
					logger.write("graba Fecha Revision");
			}
			if (datos.getFechaAprobacion() != null) {
				fecha = clientx.getTime(
						formato.format(datos.getFechaAprobacion()),
						"mm/dd/yyyy hh:mi:ss");
				dmcDoc.setTime("p_faprobador", fecha);
				if (debug)
					logger.write("graba Fecha Aprobacion");
			}
			if (datos.getFechaVigencia() != null) {
				fecha = clientx.getTime(
						formato.format(datos.getFechaVigencia()),
						"mm/dd/yyyy hh:mi:ss");
				dmcDoc.setTime("p_fvigencia", fecha);
				if (debug)
					logger.write("graba Fecha Vigencia");
			}
			if (datos.getFechaVencimiento() != null) {
				fecha = clientx.getTime(
						formato.format(datos.getFechaVencimiento()),
						"mm/dd/yyyy hh:mi:ss");
				dmcDoc.setTime("p_fvencimiento", fecha);
				if (debug)
					logger.write("graba Fecha Vencimiento");
			}
			dmcDoc.setTitle(datos.getTitulo());
			if (debug)
				logger.write("graba titulo");
			dmcDoc.setString("p_sector", datos.getSector());
			if (debug)
				logger.write("graba sector");
			dmcDoc.setString("p_filial", datos.getFilial());
			if (debug)
				logger.write("graba filial");
			dmcDoc.save();
			if (debug)
				logger.write("documento guardado");
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

			cargaDatos(DB, dmcDoc);

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