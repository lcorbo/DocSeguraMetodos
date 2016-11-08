package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.text.DecimalFormat;
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
import com.documentum.fc.common.IDfTime;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowNomenclature implements IDmMethod {

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

	private void generaNomenclatura(LPAServiceDataBase DB, IDfSysObject dmc) throws Exception {
		try {
			
			if (debug){
				logger.write("DMC OBJECT ID: "+ dmc.getObjectId().toString());
				logger.write("DMC OBJECT NAME: "+ dmc.getObjectName());
				logger.write("DMC TITLE: "+ dmc.getTitle());
				logger.write("DMC TYPE: "+ dmc.getTypeName());
			}
			LPATDANomenclature nomenclador = new LPATDANomenclature();
			nomenclador = DB.getNomenclature(dmc.getTypeName());
			String prefijo = "";
			String codigo = "";
			String[] propiedades = nomenclador.getPropiedades();
			String[] tipoPropiedades = nomenclador.getTipoPropiedades();
			String condiciones = "";
			for (int i = 0; i < propiedades.length; i++) {
				String valorPropiedad = "";
				if (tipoPropiedades[i].equalsIgnoreCase("boolean")) {
					valorPropiedad = String.valueOf(dmc.getBoolean(propiedades[i]));
				} else if (tipoPropiedades[i].equalsIgnoreCase("double")) {
					valorPropiedad = String.valueOf(dmc.getDouble(propiedades[i]));
				} else if (tipoPropiedades[i].equalsIgnoreCase("int")) {
					valorPropiedad = String.valueOf(dmc.getInt(propiedades[i]));
				} else if (tipoPropiedades[i].equalsIgnoreCase("long")) {
					valorPropiedad = String.valueOf(dmc.getLong(propiedades[i]));
				} else if (tipoPropiedades[i].equalsIgnoreCase("string")) {
					valorPropiedad = String.valueOf(dmc.getString(propiedades[i]));
				} else if (tipoPropiedades[i].equalsIgnoreCase("time")) {
					IDfTime aux = dmc.getTime(propiedades[i]);
					valorPropiedad = String.valueOf(aux.getDate());
				}
				condiciones = condiciones + " and " + propiedades[i] + " = '" + valorPropiedad + "'";
				codigo = codigo + valorPropiedad + "-";
			}
			if (debug)
				logger.write("condiciones: "+ condiciones);
			
			DB.getConnection().setAutoCommit(false);

			if (nomenclador.isNumerador()){
				if (debug)
					logger.write("codigo - no es adjunto");
				DecimalFormat format = new DecimalFormat("000000");
				String ret = format.format(DB.getNumberNomenclature(dmc.getTypeName(), condiciones));
				codigo = codigo + ret;
			} else {
				if (debug)
					logger.write("codigo - es adjunto");
				codigo = dmc.getString(nomenclador.getPropiedadNumerador());
				DecimalFormat formatadj = new DecimalFormat("000");
				String ret = formatadj.format(DB.getNumberADJNomenclature(dmc.getTypeName(), condiciones));
				
				if (dmc.getTypeName().equals("t_adjuntos"))
					prefijo = "ADJ-";
				if (dmc.getTypeName().toString().equals("t_capacitacion"))
					prefijo = "CAP-";
				
				if (dmc.getTitle().equals(" ")){
					codigo = prefijo + codigo + "-" + ret;
				}else{
					codigo = prefijo + codigo + "-" + ret + "-" + dmc.getTitle();
				}
			}
			codigo = codigo.toUpperCase();
			
			if (codigo.length() > 32){
				codigo = codigo.substring(0, 32);
			}
			
			if (debug)
				logger.write("codigo: "	+ codigo);
			
			dmc.setString(nomenclador.getPropiedadCodigo(), codigo);
			dmc.save();
			DB.getConnection().commit();
			if (debug)
				logger.write("guarda el codigo en Documentum");
		} catch (Exception e) {
			if (!DB.getConnection().getAutoCommit())
				DB.getConnection().rollback();
			throw e;
		} finally {
			DB.getConnection().setAutoCommit(true);
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
			LPAServiceBPM ServiceBPM = new LPAServiceBPM(session, logger, debug, BPM);

			ServiceBPM.loadDocuments(workitem.getPackages(""));

			IDfSysObject dmcDoc = ServiceBPM.getDocumento();

			generaNomenclatura(DB, dmcDoc);
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