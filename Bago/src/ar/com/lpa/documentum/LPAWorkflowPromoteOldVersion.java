package ar.com.lpa.documentum;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPermit;
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
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LPAWorkflowPromoteOldVersion
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
      LPAServiceRulesEngine RE = new LPAServiceRulesEngine();
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
        String[] gruposExtras = bof.getGruposExtras();
        if (gruposExtras != null) {
          for (int j = 0; j < gruposExtras.length; j++) {
            acl.grant(gruposExtras[j], RE
              .getAclPermit("ChangeACLRead_grupos_extra"), RE
              .getAclPermitEx("ChangeACLRead_grupos_extraEx"));
          }
        }
        acl.grant(this.m_userName, 
          RE.getAclPermit("ChangeACLRead_Administrator"), 
          RE.getAclPermitEx("ChangeACLRead_AdministratorEx"));
        acl.grant("dm_world", RE.getAclPermit("ChangeACLRead_world"), 
          RE.getAclPermitEx("ChangeACLRead_worldEx"));
        acl.grant("dm_owner", RE.getAclPermit("ChangeACLRead_owner"), 
          RE.getAclPermitEx("ChangeACLRead_ownerEx"));
        acl.save();
        if (debug)
          logger.write("Se guardo correctamente la ACL");
      }
    } catch (Exception e) {
      throw e;
    }
  }

  private void promoteCopia(LPATDABOF BOF, IDfSysObject dmcDoc, LPAServiceDataBase DB) throws Exception
  {
    IDfCollection col = null;
    try {
      IDfQuery cons = new DfQuery();
      cons.setDQL("select dm_dbo.dm_sysobject_s.r_object_id from dm_dbo.dm_sysobject_s, dm_dbo.dm_sysobject_r, dm_dbo.dm_policy_r, " + 
        BOF.getClase() + 
        " where " + 
        BOF.getClase() + 
        "." + 
        BOF.getPropiedadCodigo() + 
        " = '" + 
        dmcDoc.getString(BOF.getPropiedadCodigo()) + 
        "' and dm_dbo.dm_sysobject_s.r_policy_id = dm_dbo.dm_policy_r.r_object_id and dm_dbo.dm_sysobject_s.r_object_id = " + 
        BOF.getClase() + 
        ".r_object_id and " + 
        BOF.getClase() + 
        "." + 
        BOF.getPropiedadCopia() + 
        " = 1 and dm_dbo.dm_sysobject_s.r_object_id = dm_dbo.dm_sysobject_r.r_object_id and dm_dbo.dm_sysobject_r.r_version_label like '%CURRENT%' and dm_dbo.dm_policy_r.i_state_no = dm_dbo.dm_sysobject_s.r_current_state and dm_dbo.dm_policy_r.state_name = '" + 
        BOF.getEstadoVigente() + "'");
      if (debug)
        logger.write("DQL: " + 
          cons.getDQL());
      col = cons.execute(this.session, 1);
      IDfSysObject dcmObject2 = null;
      if (col.next()) {
        dcmObject2 = (IDfSysObject)this.session.getObject(
          new DfId(col
          .getString("r_object_id")));
      }
      dcmObject2.promote(dcmObject2.getNextStateName(), true, false);
      dcmObject2.setObjectName(dcmObject2.getString(BOF.getPropiedadCodigo()) + " - No_Vigente");
      dcmObject2.save();
      if (debug)
        logger.write("promoteCopia: Promote realizado.");
      modificarACL(DB, dcmObject2);
      if (debug)
        logger.write("promoteCopia: ACL Modificada");
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null)
        try {
          col.close();
        } catch (Exception e) {
          logger.writeTrace(e);
        }
    }
  }

  public void execute(Map params, OutputStream ostream) throws Exception
  {
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

      LPATDABOF BOF = DB.getBOF(dmcDoc.getTypeName());

      promoteCopia(BOF, dmcDoc, DB);
      if (debug) {
        logger.write("Promovida a No Vigente la copia Vigente del documento");
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