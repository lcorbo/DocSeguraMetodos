package ar.com.lpa.documentum;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfPermit;
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
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class LPAWorkflowACLEdit
  implements IDmMethod
{
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

  private void modificarACL(LPAServiceDataBase DB, IDfSysObject doc) throws Exception
  {
    try {
      LPATDABOF bof = DB.getBOF(doc.getTypeName());
      IDfACL acl = doc.getACL();
      if (acl.getObjectName().startsWith("acl_")) {
        if (debug)
          logger.write("Comienza la modificacion de la ACL");
        IDfList lista = acl.getPermissions();
        for (int i = 0; i < lista.getCount(); i++) {
          acl.revokePermit((IDfPermit)lista.get(i));
        }
        acl.save();
        String grupos = doc.getAllRepeatingStrings(
          bof.getPropiedadGrupos(), ";");
        if (debug)
          logger.write("grupos: " + 
            grupos);
        StringTokenizer st = new StringTokenizer(grupos, ";");
        while (st.hasMoreTokens()) {
          String grupo = (String)st.nextElement();
          acl.grant(grupo, 3, "EXECUTE_PROC");
        }
        acl.grant(doc.getString("p_editor"), 6, 
          "EXECUTE_PROC");
        String[] gruposExtras = bof.getGruposExtras();
        if (gruposExtras != null) {
          for (int j = 0; j < gruposExtras.length; j++) {
            acl.grant(gruposExtras[j], 6, 
              "EXECUTE_PROC");
          }
        }
        if (bof.getPropiedadSector() != null) {
          acl.grant(doc.getString(bof.getPropiedadSector()), 3, 
            "EXECUTE_PROC");
          acl.grant(
            doc.getString(bof.getPropiedadSector()) + "-edit", 
            6, "EXECUTE_PROC");
        }
        acl.grant(this.m_userName, 7, 
          "DELETE_OBJECT");
        acl.grant("dm_world", 1, "EXECUTE_PROC");
        acl.grant("dm_owner", 3, "EXECUTE_PROC");
        acl.save();
        if (debug)
          logger.write("Se guardo correctamente la ACL");
      } else {
        IDfACL newAcl = null;
        newAcl = (IDfACL)this.session.newObject("dm_acl");
        newAcl.setObjectName("acl_" + doc.getObjectId().toString());
        String grupos = doc.getAllRepeatingStrings(
          bof.getPropiedadGrupos(), ";");
        if (debug)
          logger.write("grupos: " + 
            grupos);
        StringTokenizer st = new StringTokenizer(grupos, ";");
        while (st.hasMoreTokens()) {
          String grupo = (String)st.nextElement();
          newAcl.grant(grupo, 3, "EXECUTE_PROC");
        }
        newAcl.grant(doc.getString("p_editor"), 6, 
          "EXECUTE_PROC");
        String[] gruposExtras = bof.getGruposExtras();
        if (gruposExtras != null) {
          for (int j = 0; j < gruposExtras.length; j++) {
            newAcl.grant(gruposExtras[j], 6, 
              "EXECUTE_PROC");
          }
        }
        if (bof.getPropiedadSector() != null) {
          newAcl.grant(doc.getString(bof.getPropiedadSector()), 3, 
            "EXECUTE_PROC");
          newAcl.grant(doc.getString(bof.getPropiedadSector()) + 
            "-edit", 6, "EXECUTE_PROC");
        }
        newAcl.grant(this.m_userName, 7, 
          "DELETE_OBJECT");
        newAcl.grant("dm_world", 1, "EXECUTE_PROC");
        newAcl.grant("dm_owner", 3, "EXECUTE_PROC");
        newAcl.save();
        if (debug)
          logger.write("Se guardo correctamente la ACL");
        doc.setACL(newAcl);
        doc.save();
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
      IDfId workitemID = new DfId(this.m_workitemId);
      this.session = sessionManager.getSession(this.m_docbase);
      DB = new LPAServiceDataBase(logger);
      DB.conectar(props);
      IDfWorkitem workitem = (IDfWorkitem)this.session.getObject(workitemID);

      if (workitem.getRuntimeState() == 0) {
        workitem.acquire();
      }
      if (debug) {
        logger.write("Adquirio la tarea");
      }
      IDfId wfId = workitem.getWorkflowId();
      IDfWorkflow wf = (IDfWorkflow)this.session.getObject(wfId);
      IDfId pId = wf.getProcessId();
      IDfProcess process = (IDfProcess)this.session.getObject(pId);
      LPATDAWorkflowBPM BPM = DB.getWorkflowBPM(process.getObjectName());
      LPAServiceBPM ServiceBPM = new LPAServiceBPM(this.session, logger, 
        debug, BPM);

      ServiceBPM.loadDocuments(workitem.getPackages(""));

      IDfSysObject dmcDoc = ServiceBPM.getDocumento();

      modificarACL(DB, dmcDoc);
      if (debug) {
        logger.write("ACL modificada");
      }
      workitem.complete();
      if (debug)
        logger.write("Completa la tarea");
    }
    catch (Exception e) {
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

  protected IDfSessionManager login() throws DfException {
    if ((this.m_docbase == null) || (this.m_userName == null) || 
      (this.m_ticket == null)) {
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