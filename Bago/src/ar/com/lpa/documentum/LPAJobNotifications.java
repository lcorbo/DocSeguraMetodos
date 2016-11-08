package ar.com.lpa.documentum;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfActivity;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import com.documentum.mthdservlet.IDmMethod;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class LPAJobNotifications
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

  private String[] obtieneMail(String usuario)
    throws Exception
  {
    IDfCollection col = null;
    String[] mail = (String[])null;
    try {
      IDfQuery cons = new DfQuery();
      cons.setDQL("select user_address from dm_user where user_name = '" + 
        usuario + "' and user_address != ''");
      col = cons.execute(this.session, 1);
      if (col.next()) {
        if (debug)
          logger.write("obtieneMail - mail: " + 
            col.getString("user_address"));
        mail = new String[1];
        mail[0] = col.getString("user_address");
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (col != null)
        col.close();
    }
    return mail;
  }

  public void execute(Map params, OutputStream ostream) throws Exception {
    LPAServiceConfigurator props = new LPAServiceConfigurator();
    logger = new LPAServiceLogger(props.getLog());
    debug = props.getDebug();
    if (debug)
      logger.write("Empieza");
    LPAServiceDataBase DB = null;
    IDfSessionManager sessionMgr = null;
    IDfCollection col = null;
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
      GregorianCalendar gc = new GregorianCalendar();
      String query = "select dmi_workitem.r_object_id, dmi_workitem.r_performer_name, dmi_workitem.r_runtime_state, dmi_workitem.r_creation_date from dmi_workitem where ((r_runtime_state = 0) or (r_runtime_state = 1))";
      if (debug)
        logger.write("Query: " + query);
      IDfQuery cons = new DfQuery();
      cons.setDQL(query);
      col = cons.execute(this.session, 0);
      while (col.next()) {
        try {
          IDfTime fecha = col.getTime("r_creation_date");
          Date fechaHoy = gc.getTime();
          long diferencia = (fechaHoy.getTime() - fecha.getDate()
            .getTime()) / 86400000L;
          if (debug)
            logger.write("diferencia: " + 
              diferencia);
          if (diferencia % props.getDiasNotificacionesTareas() == 0L) {
            IDfId workitemID = new DfId(
              col.getString("r_object_id"));
            IDfWorkitem workitem = (IDfWorkitem)this.session
              .getObject(workitemID);
            IDfId wfId = workitem.getWorkflowId();
            IDfWorkflow wf = (IDfWorkflow)this.session.getObject(wfId);
            IDfId pId = wf.getProcessId();
            IDfProcess process = (IDfProcess)this.session
              .getObject(pId);
            String nombreTarea = workitem.getActivity()
              .getObjectName();
            String nombreWorkflow = process.getObjectName();
            IDfCollection packages = workitem.getPackages("");
            int i = 1;
            String adjuntos = "";
            while (packages.next()) {
              IDfSysObject dmc = (IDfSysObject)this.session.getObject(packages.getId("r_component_id"));
              adjuntos = adjuntos + "Adjunto " + i + " : " + dmc.getObjectName() + "; ";
              i++;
            }
            LPATDAMail mail = new LPATDAMail(props);
            mail.setTo(obtieneMail(col
              .getString("r_performer_name")));
            mail = DB.getWorkflowMail(mail, "LPAJobNotifications");
            String body = mail.getBody();
            if (body.contains("WORKFLOW")) {
              body = body.replace("WORKFLOW", nombreWorkflow);
            }
            if (body.contains("TAREA")) {
              body = body.replace("TAREA", nombreTarea);
            }
            if (body.contains("ADJUNTOS")) {
              body = body.replace("ADJUNTOS", adjuntos);
            }
            mail.setBody(body);
            LPAServiceMail serviceMail = new LPAServiceMail(mail, 
              logger);
            serviceMail.sendMails();
            if (debug)
              logger.write("Enviada la notificacion");
          }
        } catch (Exception e) {
          logger.writeTrace(e);
        }
      }
      if (debug)
        logger.write("Termina");
    } catch (Exception e) {
      ostream.write(e.getMessage().getBytes());
      e.printStackTrace();
      logger.writeTrace(e);
      throw e;
    } finally {
      if (this.session != null)
        sessionMgr.release(this.session);
      if (col != null)
        col.close();
      
        
      if (DB != null)
        DB.desconectar();
    }
  }
}