package ar.com.lpa.documentum;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import com.documentum.fc.common.IDfValue;
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LPAJobExpiration
  implements IDmMethod
{
  protected IDfSessionManager m_sessionMgr = null;

  protected String m_docbase = null;

  protected String m_userName = null;

  protected String m_ticket = null;

  private IDfSession session = null;

  private static boolean debug = false;

  private static LPAServiceLogger logger = null;
  static final long MILLSECS_PER_DAY = 86400000L;

  private ArrayList<String> obtieneUsuarios(ArrayList<String> grupos)
    throws Exception
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
        col.close();
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
    try {
      IDfQuery cons = new DfQuery();
      cons.setDQL("select group_name from dm_group");
      IDfCollection col = cons.execute(this.session, 1);
      while (col.next()) {
        if (debug)
          logger.write("grupo: " + 
            col.getString("group_name"));
        resultado.add(col.getString("group_name"));
      }
      col.close();
    } catch (Exception e) {
      throw e;
    }
    return resultado;
  }

  private boolean esGrupo(String nombre) throws Exception {
    boolean isGroup = false;
    try {
      IDfQuery cons = new DfQuery();
      cons.setDQL("select r_is_group from dm_user where user_name = '" + 
        nombre + "'");
      IDfCollection col = cons.execute(this.session, 1);
      if ((col.next()) && 
        (col.getInt("r_is_group") == 1)) {
        isGroup = true;
      }
      col.close();
    } catch (Exception e) {
      throw e;
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
        col.close();
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

  private void insertaOrden(LPAServiceDataBase DB, String nombreWorkflow, IDfSysObject dmcDoc) throws Exception
  {
    LPATDAWorkflowBPM BPM = null;
    try {
      BPM = DB.getWorkflowBPM(nombreWorkflow);
      String propiedades = "";
      propiedades += dmcDoc.getString("p_editor");
      /*if (BPM.getPropiedades() != null) {
        if (debug)
          logger.write("tiene propiedades BPM");
        for (int i = 0; i < BPM.getPropiedades().length; i++) {
          String propiedad = BPM.getPropiedades()[i];
          if (debug)
            logger.write("nombrePropiedad: " + 
              propiedad);
          IDfValue valor = dmcDoc.getValue(propiedad);
          int tipo = valor.getDataType();
          switch (tipo) {
          case 0:
            propiedades = propiedades + 
              dmcDoc.getBoolean(propiedad) + ";";
            break;
          case 5:
            propiedades = propiedades + dmcDoc.getDouble(propiedad) + 
              ";";
            break;
          case 1:
            propiedades = propiedades + dmcDoc.getInt(propiedad) + 
              ";";
            break;
          case 2:
            propiedades = propiedades + dmcDoc.getString(propiedad) + 
              ";";
          case 3:
          case 4:
          }
        }
        propiedades = propiedades
          .substring(0, propiedades.length() - 1);
        if (debug)
          logger.write("valorPropiedades: " + 
            propiedades);
      }*/
      DB.insertWorkflowOrden(nombreWorkflow, dmcDoc.getObjectId()
        .toString(), propiedades, "NO");
    } catch (Exception e) {
      throw e;
    }
  }

  private void chequeaVencimiento(LPAServiceConfigurator props, LPATDAExpiration vencimiento, LPAServiceDataBase DB)
    throws Exception
  {
    IDfCollection col = null;
    try {
      GregorianCalendar gc = new GregorianCalendar();
      Date fechaHoy = gc.getTime();
      IDfQuery cons = new DfQuery();
      cons.setDQL("select dm_dbo.dm_sysobject_s.r_object_id from dm_dbo.dm_sysobject_s, dm_dbo.dm_sysobject_r, dm_dbo.dm_policy_r, " + 
        vencimiento.getClase() + 
        " where dm_dbo.dm_sysobject_s.r_policy_id = dm_dbo.dm_policy_r.r_object_id and dm_dbo.dm_sysobject_s.r_object_id = " + 
        vencimiento.getClase() + 
        ".r_object_id and dm_dbo.dm_sysobject_s.r_object_id = dm_dbo.dm_sysobject_r.r_object_id and dm_dbo.dm_sysobject_r.r_version_label like '%CURRENT%' and dm_dbo.dm_policy_r.i_state_no = dm_dbo.dm_sysobject_s.r_current_state and dm_dbo.dm_policy_r.state_name = '" + 
        vencimiento.getEstadoVigente() + "'");
      if (debug)
        logger.write("DQL: " + 
          cons.getDQL());
      col = cons.execute(this.session, 1);
      while (col.next()) {
        IDfSysObject dmc = (IDfSysObject)this.session.getObject(
          new DfId(col.getString("r_object_id")));
        Date fVencimiento = dmc.getTime(
          vencimiento.getPropiedadVencimiento()).getDate();
        if (fVencimiento != null) {
          if (debug) {
            logger.write("clase: " + 
              vencimiento.getClase());
            logger.write("idDocumento: " + 
              dmc.getObjectId().toString());
            logger.write("object_name: " + 
              dmc.getObjectName());
            logger.write("fechaVencimiento: " + 
              fVencimiento.toString());
          }
          long diferencia = (fechaHoy.getTime() - fVencimiento
            .getTime()) / 86400000L;
          diferencia *= -1L;
          diferencia += 1L;
          if (debug)
            logger.write("diferencia: " + 
              diferencia);
          if (diferencia == vencimiento.getDiasNotificacion()) {
            if (debug)
              logger.write("dia de notificacion");
            LPATDAMail mail = new LPATDAMail(props);
            mail.setTo(getUsuariosACL(dmc));
            mail = DB.getWorkflowMail(mail, "LPAJobExpiration");
            if (debug) {
              logger.write("subject: " + 
                mail.getSubject());
              logger.write("body: " + 
                mail.getBody());
            }
            String subject = mail.getSubject();
            if (subject.contains("CODIGO")) {
              subject = subject.replace("CODIGO", dmc.getString("p_codigo"));
            }
            if (subject.contains("TITULO")) {
              subject = subject.replace("TITULO", dmc.getTitle());
            }
            mail.setSubject(subject);
            String body = mail.getBody();
            if (body.contains("CODIGO")) {
              body = body.replace("CODIGO", dmc.getString("p_codigo"));
            }
            if (body.contains("TITULO")) {
              body = body.replace("TITULO", dmc.getTitle());
            }
            mail.setBody(body);
            LPAServiceMail serviceMail = new LPAServiceMail(mail, 
              logger);
            serviceMail.sendMails();
            if (debug)
              logger.write("notificacion enviada"); 
          } else {
            if (diferencia != vencimiento
              .getDiasPreVencimiento()) continue;
            if (debug) {
              logger.write("dia de pre-vencimiento");
              logger.write("lanzando workflow: " + 
                vencimiento.getWorkflowVencimiento());
            }
            insertaOrden(DB, vencimiento.getWorkflowVencimiento(), 
              dmc);
          }
        }
      }
      col.close();
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null)
        try {
          col.close();
        } catch (Exception e) {
          logger.writeTrace(e);
          throw e;
        }
    }
  }

  public void execute(Map params, OutputStream ostream) throws Exception
  {
    LPAServiceConfigurator props = new LPAServiceConfigurator();
    logger = new LPAServiceLogger(props.getLog());
    debug = props.getDebug();
    if (debug)
      logger.write("Empieza");
    LPAServiceDataBase DB = null;
    IDfSessionManager sessionMgr = null;
    try {
      String sUserName = System.getProperty("user.name");
      IDfClientX clientx = new DfClientX();
      IDfLoginInfo li = clientx.getLoginInfo();
      li.setDomain("");
      li.setUser(sUserName);
      li.setPassword("");
      if (debug)
        logger.write("Obteniendo sesion de Documentum");
      IDfClient client = clientx.getLocalClient();
      sessionMgr = client.newSessionManager();
      sessionMgr.setIdentity(props.getDocbase(), li);
      this.session = sessionMgr.getSession(props.getDocbase());
      if (debug)
        logger.write("login correcto");
      DB = new LPAServiceDataBase(logger);
      DB.conectar(props);
      ArrayList arrayVencimientos = DB
        .getVencimientos();
      for (int i = 0; i < arrayVencimientos.size(); i++) {
        chequeaVencimiento(props, (LPATDAExpiration)arrayVencimientos.get(i), DB);
      }
      if (debug)
        logger.write("Termina");
    } catch (Exception e) {
      ostream.write(e.getMessage().getBytes());
      e.printStackTrace();
      logger.writeTrace(e);
      throw e;
    } finally {
      if ((sessionMgr != null) && 
        (this.session != null)) {
        sessionMgr.release(this.session);
      }
      
        
      if (DB != null)
        DB.desconectar();
    }
  }
}