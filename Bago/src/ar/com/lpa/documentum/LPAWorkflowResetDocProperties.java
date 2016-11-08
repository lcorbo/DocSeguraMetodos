package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowResetDocProperties implements IDmMethod {

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

	public void execute(Map params, OutputStream ostream) throws Exception {
		// TODO Auto-generated method stub
		LPAServiceConfigurator props = new LPAServiceConfigurator();
	    logger = new LPAServiceLogger(props.getLog());
	    debug = props.getDebug();
	    initWorkflowParams(params);
	    IDfSessionManager sessionManager = login();
	    LPAServiceDataBase DB = null;
	    IDfWorkitem workitem = null;
	    try {
	        IDfId workitemID = new DfId(this.m_workitemId);
	        this.session = sessionManager.getSession(this.m_docbase);
	        DB = new LPAServiceDataBase(logger);
	        DB.conectar(props);
	        workitem = (IDfWorkitem)this.session.getObject(workitemID);
	        
	        
	        if (workitem.getRuntimeState() == 0) {
	          workitem.acquire();
	        }
	        if (debug) {
	          logger.write("Adquirio la tarea");
	        }
	        IDfCollection pkgColl = workitem.getPackages("");
	        String packageId = null;
	        if (debug)
				logger.write("recorre los packages");
	        while (pkgColl.next()) {
	        	String packageName = pkgColl.getString("r_package_name");
	        	if(debug)
	        		logger.write("Se registra el paquete: "+packageName);
	        	if(packageName.equalsIgnoreCase("Documento"))
	        	{
	        		packageId = pkgColl.getString("r_component_id");
	        	}
	        }
	        pkgColl.close();
	        if (debug)
				logger.write("Se cerro la coleccion");
	        if(debug && packageId==null)
	        	logger.write("No se obtuvo ningun id");
	        if(debug && packageId!=null)
	        	logger.write("r_component_id: "+packageId);
	        Connection con = DB.getConnection();
	        int resultado = resetProperties(con,packageId);
	        if(debug)
	        	logger.write("Se modificaron "+resultado+" registro/s");
	        if (con != null
					&& !con.isClosed())
				con.close();
	        if (debug)
				logger.write("Se cerró la conexion");
	        
	    }
	    catch (Exception e) {
	        ostream.write(e.getMessage().getBytes());
	        e.printStackTrace();
	        logger.writeTrace(e);
	        throw e;
	      }
	    finally {
	        if (this.session != null)
	          sessionManager.release(this.session);
	        if (debug)
				logger.write("Se libera la sesion");
	        
	        if (DB != null)
	          DB.desconectar();
	        if (debug)
				logger.write("Se desconecta la base de datos");
	        
	        if(workitem != null)
	        	workitem.complete();
	        if (debug)
				logger.write("Se completa el workitem");
	      }
	}
	
	protected void initWorkflowParams(Map params)
	  {
	    Set keys = params.keySet();
	    Iterator iter = keys.iterator();
	    while (iter.hasNext()) {
	      String key = (String)iter.next();
	      if ((key == null) || (key.length() == 0)) {
	        continue;
	      }
	      String[] value = (String[])params.get(key);

	      if (key.equalsIgnoreCase("user"))
	        this.m_userName = (value.length > 0 ? value[0] : "");
	      else if (key.equalsIgnoreCase("docbase_name"))
	        this.m_docbase = (value.length > 0 ? value[0] : "");
	      else if (key.equalsIgnoreCase("workitemId"))
	        this.m_workitemId = (value.length > 0 ? value[0] : "");
	      else if (key.equalsIgnoreCase("packageId"))
	        this.m_workitemId = (value.length > 0 ? value[0] : "");
	      else if (key.equalsIgnoreCase("ticket"))
	        this.m_ticket = (value.length > 0 ? value[0] : "");
	    }
	  }
	
	protected IDfSessionManager login() throws DfException
	  {
	    if ((this.m_docbase == null) || (this.m_userName == null) || (this.m_ticket == null)) {
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
	
	public int resetProperties(Connection con, String id) throws Exception {
		CallableStatement cs = null;
		try {
			cs = con.prepareCall(
					"{call ResetProperties(?)}");
			cs.setString(1, id);
			return cs.executeUpdate();
		} catch (Exception e) {

			throw e;

		} finally {

			if (cs != null) {

				try {
					cs.close();
				} catch (SQLException e) {

					logger.writeTrace(e);

				}

			}

		}
	}

}
