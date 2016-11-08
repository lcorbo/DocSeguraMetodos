package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfActivity;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflowBuilder;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;

public class FichasTecnicasJob implements IDmMethod{
	
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
	
	public void startWorkflow(String wfName, IDfId id, IDfSession session) throws DfException {
		IDfId wfId = (IDfId) session
		.getIdByQualification("dm_process where object_name = '"
				+ wfName + "'");

			IDfWorkflowBuilder workflowBuilder = session.newWorkflowBuilder(wfId);
		 workflowBuilder.getWorkflow().setObjectName(wfName);
		 if ((workflowBuilder.getStartStatus() != 0) || (!(workflowBuilder.isRunnable()))) {
		  DfLogger.warn(this, "startWorkflow - workflow '" + wfName + "' is not runnable or StartStauts=0!", null, null);
		  throw new DfException("cannot start Workflow!");
		 }
		 workflowBuilder.initWorkflow();
		 workflowBuilder.runWorkflow();

		 // Adding attachments:
		 IDfList attachIds = new DfList();
		 attachIds.appendId(id);

		 IDfList startActivities = workflowBuilder.getStartActivityIds();
		 int packageIndex = 0;
		 for (int i = 0; i < startActivities.getCount(); i++) {
		  IDfActivity activity = (IDfActivity) session.getObject(startActivities.getId(i));
		  workflowBuilder.addPackage(activity.getObjectName(), activity.getPortName(packageIndex),
		    activity.getPackageName(packageIndex), activity.getPackageType(packageIndex), null, false, attachIds);
		 }
		}
	
	public void execute(Map params, OutputStream ostream) throws Exception {
		//LPAServiceConfigurator props = new LPAServiceConfigurator();
		//logger = new LPAServiceLogger(props.getLog());
		//debug = props.debug;
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
			
			
			cons.setDQL("select r_object_id,carpeta_creada from t_fichas_tecnicas where any i_folder_id in  " +
					"(select r_object_id from dm_folder where object_name = '"+props.get("clase")+"')");
			
			col = cons.execute(this.session, 1);
			
			String wfName = props.get("wfname");
				
			IDfId id = null;
			
			while(col.next())
			{
				id = col.getId("r_object_id");
				if(!col.getBoolean("carpeta_creada"))
					startWorkflow(wfName,id,session);
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
