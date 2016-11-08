package ar.com.lpa.documentum;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfActivity;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfGroup;
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
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class LPAWorkflowMail
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

  private ArrayList<String> obtieneUsuarios(ArrayList<String> grupos) throws Exception
  {
    ArrayList resultado = new ArrayList();
    IDfCollection col = null;
    try {
      IDfQuery cons = new DfQuery();
      if (debug)
        logger.write("CantGrupos: " + 
          grupos.size());
      for (int i = 0; i < grupos.size(); i++) {
        cons.setDQL("select r_object_id from dm_group where group_name = '" + 
          (String)grupos.get(i) + "'");
        col = cons.execute(this.session, 1);
        if (col.next()) {
          IDfGroup grupo = (IDfGroup)this.session.getObject(
            new DfId(col
            .getString("r_object_id")));
          for (int j = 0; j < grupo.getAllUsersNamesCount(); j++) {
            if (debug)
              logger.write("usuario: " + 
                grupo.getAllUsersNames(j));
            resultado.add(grupo.getAllUsersNames(j));
          }
        }
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null) {
        col.close();
      }
    }
    return resultado;
  }

  private ArrayList<String> obtieneGrupos() throws Exception {
    ArrayList resultado = new ArrayList();
    IDfCollection col = null;
    try {
      IDfQuery cons = new DfQuery();
      cons.setDQL("select group_name from dm_group");
      col = cons.execute(this.session, 1);
      while (col.next()) {
        if (debug)
          logger.write("grupo: " + 
            col.getString("group_name"));
        resultado.add(col.getString("group_name"));
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null)
        col.close();
    }
    return resultado;
  }

  private boolean esGrupo(String nombre) throws Exception {
    boolean isGroup = false;
    IDfCollection col = null;

    label100: 
    try { IDfQuery cons = new DfQuery();
      cons.setDQL("select r_is_group from dm_user where user_name = '" + 
        nombre + "'");
      col = cons.execute(this.session, 1);
      if (col.next()) {
        if (col.getInt("r_is_group") != 1) break label100; isGroup = true;
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null)
        col.close();
    }
    return isGroup;
  }

  private String[] obtieneMails(Set<String> usuarios) throws Exception {
    String[] mails = (String[])null;
    ArrayList aux = new ArrayList();
    IDfCollection col = null;
    try {
      IDfQuery cons = new DfQuery();
      Iterator it = usuarios.iterator();
      while (it.hasNext()) {
        cons.setDQL("select user_address from dm_user where user_name = '" + 
          (String)it.next() + "' and user_address != ''");
        col = cons.execute(this.session, 1);
        if (col.next()) {
          if (debug)
            logger.write("mail: " + 
              col.getString("user_address"));
          aux.add(col.getString("user_address"));
        }
      }
      mails = new String[aux.size()];
      Iterator it2 = aux.iterator();
      int i = 0;
      while (it2.hasNext()) {
        mails[i] = ((String)it2.next());
        i++;
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null)
        col.close();
    }
    return mails;
  }

  private String[] getUsuariosACL(IDfSysObject dmc) throws Exception {
    String[] aux = (String[])null;
    try {
      boolean dm_world = false;
      ArrayList grupos = new ArrayList();
      ArrayList usuarios = new ArrayList();
      Set users = new HashSet();
      users.add(dmc.getOwnerName());
      IDfACL acl = dmc.getACL();
      int cant = acl.getAccessorCount();
      for (int i = 0; i < cant; i++) {
        if ((acl.getAccessorPermit(i) < 3) || 
          (!acl.getAccessorName(i).equals("dm_world"))) continue;
        dm_world = true;
      }

      if (dm_world) {
        grupos = obtieneGrupos();
      }
      for (int i = 0; i < cant; i++) {
        if (acl.getAccessorPermit(i) >= 3) {
          String nombre = acl.getAccessorName(i);
          if (debug)
            logger.write("grupo: " + 
              nombre);
          if (nombre.equals("dm_world"))
            continue;
          if (nombre.equals("dm_owner")) {
            continue;
          }
          if (debug)
            logger.write("se agrega el grupo: " + 
              nombre);
          if (esGrupo(nombre))
            grupos.add(acl.getAccessorName(i));
          else {
            users.add(nombre);
          }
        }
      }

      usuarios = obtieneUsuarios(grupos);
      for (int j = 0; j < usuarios.size(); j++) {
        if (debug)
          logger.write("usuario: " + 
            (String)usuarios.get(j));
        users.add((String)usuarios.get(j));
      }

      aux = obtieneMails(users);
    }
    catch (Exception e) {
      throw e;
    }
    return aux;
  }

  private String[] getUsuariosBPM(IDfSysObject dmc, IDfSysObject bpm, String propiedad) throws Exception
  {
    String[] aux = (String[])null;
    try {
      ArrayList grupos = new ArrayList();
      ArrayList usuarios = new ArrayList();
      Set users = new HashSet();
      StringTokenizer st = new StringTokenizer(bpm.getString(propiedad), 
        ";");
      while (st.hasMoreTokens()) {
        String usuario = (String)st.nextElement();
        if (esGrupo(usuario))
          grupos.add(usuario);
        else
          users.add(usuario);
      }
      usuarios = obtieneUsuarios(grupos);
      for (int j = 0; j < usuarios.size(); j++) {
        users.add((String)usuarios.get(j));
      }
      aux = obtieneMails(users);
    } catch (Exception e) {
      throw e;
    }
    return aux;
  }

  private String[] getUsuariosRelaciones(IDfSysObject dmc, LPAServiceDataBase DB, LPATDABOF bof) throws Exception
  {
    String[] aux = (String[])null;
    try {
      ArrayList relaciones = DB.getRelations(
        dmc.getString(bof.getPropiedadCodigo()), 
        dmc.getString(bof.getPropiedadVersion()));
      for (int i = 0; i < relaciones.size(); i++) {
        LPATDARelation relacion = (LPATDARelation)relaciones.get(i);
        IDfSysObject docRel = null;
        if (relacion.getCodigoDocDestino().equals(
          dmc.getString(bof.getPropiedadCodigo())))
          docRel = (IDfSysObject)this.session.getObject(
            new DfId(relacion
            .getIdDocOrigen()));
        else
          docRel = (IDfSysObject)this.session.getObject(
            new DfId(relacion
            .getIdDocDestino()));
        if (aux == null) {
          aux = getUsuariosACL(docRel);
        } else {
          String[] aux2 = getUsuariosACL(docRel);
          String[] aux3 = new String[aux.length + aux.length];
          System.arraycopy(aux, 0, aux3, 0, aux.length);
          System.arraycopy(aux2, 0, aux3, aux.length, aux2.length);
          aux = aux3;
        }
      }
    } catch (Exception e) {
      throw e;
    }
    return aux;
  }

  private void enviaMail(LPAServiceConfigurator props, LPAServiceDataBase DB, IDfSysObject dmc, String paso, IDfSysObject bpm) throws Exception
  {
    try {
      LPATDAMail mail = new LPATDAMail(props);
      if (debug)
        logger.write("Paso: " + paso);
      mail = DB.getWorkflowMail(mail, paso);
      LPATDABOF bof = DB.getBOF(dmc.getTypeName());
      if (debug) {
        logger.write("subject: " + 
          mail.getSubject());
        logger.write("body: " + 
          mail.getBody());
      }
      String subject = mail.getSubject();
      if (subject.contains("CODIGO")) {
        subject = subject.replace("CODIGO", 
          dmc.getString(bof.getPropiedadCodigo()));
      }
      if (subject.contains("TITULO")) {
        subject = subject.replace("TITULO", dmc.getTitle());
      }
      mail.setSubject(subject);
      String body = mail.getBody();
      if (body.contains("CODIGO")) {
        body = body.replace("CODIGO", 
          dmc.getString(bof.getPropiedadCodigo()));
      }
      if (body.contains("TITULO")) {
        body = body.replace("TITULO", dmc.getTitle());
      }
      mail.setBody(body);
      if (mail.getDestinatarios().equals("ACL"))
        mail.setTo(getUsuariosACL(dmc));
      else if (mail.getDestinatarios().equals("BPM"))
        mail.setTo(getUsuariosBPM(dmc, bpm, mail.getPropiedadBPM()));
      else if (mail.getDestinatarios().equals("Relaciones"))
        mail.setTo(getUsuariosRelaciones(dmc, DB, bof));
      LPAServiceMail serviceMail = new LPAServiceMail(mail, logger);
      serviceMail.sendMails();
      if (debug)
        logger.write("se envio el mail correctamente");
    } catch (Exception e) {
      throw e;
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

      enviaMail(props, DB, dmcDoc, 
        workitem.getActivity().getObjectName(), 
        ServiceBPM.getParametros());
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
}