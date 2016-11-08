package ar.com.lpa.documentum;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFormat;
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
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class LPAWorkflowReplaceContent
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

  private void copiarAtributo(IDfSysObject dmcDoc, IDfSysObject dmcDoc2, IDfAttr atributo) throws Exception
  {
    try {
      int tipo = atributo.getDataType();
      switch (tipo) {
      case 0:
        dmcDoc2.setBoolean(atributo.getName(), 
          dmcDoc.getBoolean(atributo.getName()));
        break;
      case 5:
        dmcDoc2.setDouble(atributo.getName(), 
          dmcDoc.getDouble(atributo.getName()));
        break;
      case 1:
        dmcDoc2.setInt(atributo.getName(), 
          dmcDoc.getInt(atributo.getName()));
        break;
      case 2:
        dmcDoc2.setString(atributo.getName(), 
          dmcDoc.getString(atributo.getName()));
        break;
      case 4:
        dmcDoc2.setTime(atributo.getName(), 
          dmcDoc.getTime(atributo.getName()));
      case 3:
      }
    } catch (Exception e) {
      throw e;
    }
  }

  private void copiarAtributoRepetitivo(IDfSysObject dmcDoc, IDfSysObject dmcDoc2, IDfAttr atributo) throws Exception
  {
    try {
      int tipo = atributo.getDataType();
      String aux = "";
      StringTokenizer st = null;
      switch (tipo) {
      case 0:
        aux = dmcDoc2.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int i = 0; i < st.countTokens(); i++) {
          dmcDoc2.remove(atributo.getName(), i);
        }
        aux = dmcDoc.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int j = 0; j < st.countTokens(); j++) {
          dmcDoc2.setRepeatingBoolean(atributo.getName(), j, 
            dmcDoc.getRepeatingBoolean(atributo.getName(), j));
        }
        break;
      case 5:
        aux = dmcDoc2.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int i = 0; i < st.countTokens(); i++) {
          dmcDoc2.remove(atributo.getName(), i);
        }
        aux = dmcDoc.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int j = 0; j < st.countTokens(); j++) {
          dmcDoc2.setRepeatingDouble(atributo.getName(), j, 
            dmcDoc.getRepeatingDouble(atributo.getName(), j));
        }
        break;
      case 1:
        aux = dmcDoc2.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int i = 0; i < st.countTokens(); i++) {
          dmcDoc2.remove(atributo.getName(), i);
        }
        aux = dmcDoc.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int j = 0; j < st.countTokens(); j++) {
          dmcDoc2.setRepeatingInt(atributo.getName(), j, 
            dmcDoc.getRepeatingInt(atributo.getName(), j));
        }
        break;
      case 2:
        aux = dmcDoc2.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int i = 0; i < st.countTokens(); i++) {
          dmcDoc2.remove(atributo.getName(), i);
        }
        aux = dmcDoc.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        int k = 0;
        while (st.hasMoreTokens()) {
          dmcDoc2.setRepeatingString(atributo.getName(), k, 
            st.nextToken());
          k++;
        }
        break;
      case 4:
        aux = dmcDoc2.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int i = 0; i < st.countTokens(); i++) {
          dmcDoc2.remove(atributo.getName(), i);
        }
        aux = dmcDoc.getAllRepeatingStrings(atributo.getName(), ";");
        st = new StringTokenizer(aux, ";");
        for (int j = 0; j < st.countTokens(); j++)
          dmcDoc2.setRepeatingTime(atributo.getName(), j, 
            dmcDoc.getRepeatingTime(atributo.getName(), j));
      case 3:
      }
    }
    catch (Exception e) {
      throw e;
    }
  }

  private void replaceContent(LPATDABOF BOF, IDfSysObject dmcDoc) throws Exception
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
      IDfFormat myFormat = this.session.getFormat(dcmObject2.getContentType());
      String ext = myFormat.getDOSExtension();
      File clase = new File(LPAWorkflowReplaceContent.class.getResource(
        "LPAWorkflowReplaceContent.class").toString());
      File carpeta = clase.getParentFile();
      String path = carpeta.getPath();
      path = path.substring(5);
      String filePath = path + File.separator + 
        dcmObject2.getObjectName() + "." + ext;
      if (debug)
        logger.write("path: " + 
          filePath);
      dcmObject2.getFileEx2(filePath, dcmObject2.getContentType(), 0, "", 
        false);
      dmcDoc.setFile(filePath);
      for (int i = 0; i < dmcDoc.getAttrCount(); i++) {
        IDfAttr atributo = dmcDoc.getAttr(i);
        if (atributo.getName().startsWith("p_")) {
          if (BOF.getPropiedadCopia().equals(atributo.getName())) {
            if (debug)
              logger.write("el atributo esCopia no se copia.");
          } else {
            if (debug)
              logger.write("nombre atributo: " + 
                atributo.getName());
            if (atributo.isRepeating())
              copiarAtributoRepetitivo(dcmObject2, dmcDoc, 
                atributo);
            else {
              copiarAtributo(dcmObject2, dmcDoc, atributo);
            }
          }
        }
        else if (atributo.getName().equals("title")) {
          copiarAtributo(dcmObject2, dmcDoc, atributo);
        }
      }
      dmcDoc.save();
      if (debug)
        logger.write("Doc reemplazado");
      File archivo = new File(filePath);
      if (archivo.exists())
        archivo.delete();
      if (debug)
        logger.write("Archivo temporal eliminado");
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

      DB.deleteRelations(dmcDoc.getString(BOF.getPropiedadCodigo()), 
        dmcDoc.getString(BOF.getPropiedadVersion()));
      if (debug) {
        logger.write("Borra las relaciones realizadas para esta version");
      }
      DB.deleteAnexos(dmcDoc.getString(BOF.getPropiedadCodigo()), 
        dmcDoc.getString(BOF.getPropiedadVersion()));
      if (debug) {
        logger.write("Borra los anexos realizados para esta version");
      }
      DB.deleteHistorialVersion(
        dmcDoc.getString(BOF.getPropiedadCodigo()), 
        dmcDoc.getString(BOF.getPropiedadVersion()));
      if (debug) {
        logger.write("Borrado el historial de esta version");
      }
      replaceContent(BOF, dmcDoc);
      if (debug) {
        logger.write("Reemplazado el contenido del documento");
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