package ar.com.lpa.documentum;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfActivity;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkflowBuilder;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfValue;
import com.documentum.mthdservlet.IDmMethod;

public class LPAJobExecuteOrders implements IDmMethod {

	protected IDfSessionManager m_sessionMgr = null;

	protected String m_docbase = null;

	protected String m_userName = null;

	protected String m_ticket = null;

	private IDfSession session = null;

	private static boolean debug = false;

	private static LPAServiceLogger logger = null;

	private static ArrayList<LPATDAOrders> orders = null;
	private static ArrayList<LPATDAWorkflowTasks> tasks = null;
	private static LPATDAWorkflowBPM BPM = null;
	private static IDfSysObject docBPM = null;

	private void lanzaWorkflow(IDfSysObject dcmObject, String Workflow)	throws Exception {
		try {
			if (debug)
				logger.write("Comienza lanzado del workflow");
			IDfId wfId = (IDfId) session.getIdByQualification("dm_process where object_name = '" + Workflow + "'");

			IDfWorkflowBuilder workflowBuilder = session.newWorkflowBuilder(wfId);

			if (workflowBuilder.isRunnable() && workflowBuilder.getStartStatus() == 0) {
				IDfId initWf = workflowBuilder.initWorkflow();
				workflowBuilder.runWorkflow();

				IDfWorkflow workflow = (IDfWorkflow) session.getObject(initWf);
				if (tasks != null) {
					for (int l = 0; l < tasks.size(); l++) {
						LPATDAWorkflowTasks task = tasks.get(l);
						if (debug)
							logger.write("Paso: " + task.getNombrePaso());
						IDfList performers = new DfList();
						String usuarios = "";
						
						if (task.getDocumento().equalsIgnoreCase("attach"))
							usuarios = dcmObject.getAllRepeatingStrings(task.getPropiedadUsuarios(), ";");
						else
							usuarios = docBPM.getAllRepeatingStrings(task.getPropiedadUsuarios(), ";");
						
						if (debug)
							logger.write("Usuarios: " + usuarios);
						StringTokenizer st = new StringTokenizer(usuarios, ";");
						while (st.hasMoreTokens()) {
							String usuario = (String) st.nextElement();
							if (debug)
								logger.write("Usuario: " + usuario);
							performers.append(usuario);
						}
						workflow.setPerformers(task.getNombrePaso(), performers);
					}
				}

				IDfList objList = null;
				IDfList objList2 = null;
				if (dcmObject != null) {
					objList = new DfList();
					objList.appendId(dcmObject.getObjectId());
				}
				if (docBPM != null) {
					objList2 = new DfList();
					objList2.appendId(docBPM.getObjectId());
				}
				IDfList activitysIds = workflowBuilder.getStartActivityIds();
				
				for (int j = 0; j < activitysIds.getCount(); j++) {
					
					IDfId activityId = activitysIds.getId(j);
					IDfActivity dcmActivity = (IDfActivity) session.getObject(activityId);
					String activityName = dcmActivity.getObjectName();
					if (debug)
						logger.write("actividad " + j + ": " + activityName);
					
					for (int p = 0; p < dcmActivity.getPackageCount(); p++) {
						
						if (dcmActivity.getPortType(p).equalsIgnoreCase("INPUT")) {
							String packType = dcmActivity.getPackageType(p);
							if (debug)
								logger.write("packType: " + packType);
							String packName = dcmActivity.getPackageName(p);
							if (debug)
								logger.write("packName: " + packName);
							String portName = dcmActivity.getPortName(p);
							
							if (dcmObject == null && docBPM == null) {
								workflowBuilder.addPackage(activityName, portName, packName, packType, null, false, null);
								if (debug)
									logger.write("sin adjuntos");
							} else {
								if (dcmObject != null) {
									if (packName.equals(BPM.getPackageAdjunto())) {
										workflowBuilder.addPackage(activityName, portName, packName, packType, null, false, objList);
										if (debug)
											logger.write("anexo adjunto");
									}
								}
								if (docBPM != null) {
									if (packName.equals(BPM.getPackageBPM())) {
										workflowBuilder.addPackage(activityName, portName, packName, packType, null, false, objList2);
										if (debug)
											logger.write("anexo doc BPM");
									}
								}
							}
						}

					}

				}
				if (debug)
					logger.write("Se lanzo el workflow correctamente");
			} else {
				if (debug)
					logger.write("LanzarWorkflow: El workflow no es runneable");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void creaDocBPM(LPATDAOrders orden, String path) throws Exception {
		try {
			docBPM = (IDfSysObject) session.newObject(BPM.getClaseBPM());
			if (debug)
				logger.write("docBPM creado");
			if (BPM.getPropiedades() != null) {
				for (int i = 0; i < BPM.getPropiedades().length; i++) {
					String propiedad = BPM.getPropiedades()[i];
					String valorPropiedad = orden.getValoresPropiedadesBPM()[i];
					if (debug) {
						logger.write("propiedad: " + propiedad);
						logger.write("valorPropiedad: "	+ valorPropiedad);
					}
					IDfValue valor = docBPM.getValue(propiedad);
					int tipo = valor.getDataType();
					switch (tipo) {
					case IDfValue.DF_BOOLEAN:
						docBPM.setBoolean(propiedad, Boolean.parseBoolean(valorPropiedad));
						break;
					case IDfValue.DF_DOUBLE:
						docBPM.setDouble(propiedad,	Double.parseDouble(valorPropiedad));
						break;
					case IDfValue.DF_INTEGER:
						docBPM.setInt(propiedad, Integer.parseInt(valorPropiedad));
						break;
					case IDfValue.DF_STRING:
						docBPM.setString(propiedad, valorPropiedad);
						break;
					}
				}
			}
			docBPM.link(path);
			docBPM.save();
			if (debug)
				logger.write("docBPM guardado");
		} catch (Exception e) {
			throw e;
		}
	}

	public void execute(Map params, OutputStream ostream) throws Exception {
		LPAServiceConfigurator props = new LPAServiceConfigurator();
		logger = new LPAServiceLogger(props.getLog());
		debug = props.getDebug();
		if (debug)
			logger.write("Empieza");
		IDfSessionManager sessionMgr = null;
		LPAServiceDataBase DB = null;
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
			session = sessionMgr.getSession(props.getDocbase());
			if (debug)
				logger.write("login correcto");
			DB = new LPAServiceDataBase(logger);
			DB.conectar(props);
			orders = DB.getOrders();
			if (debug)
				logger.write("cant ordenes " + orders.size());
			for (int i = 0; i < orders.size(); i++) {
				try {
					LPATDAOrders orden = orders.get(i);
					if (debug) {
						logger.write("Ejecutando Orden: " + orden.getID());
						logger.write("Ejecutando nombreWorkflow: " + orden.getNombreWorkflow());
						logger.write("Ejecutando idDocumento: "	+ orden.getIdDocumento());
					}
					
					IDfSysObject dmc = null;
					if (!(orden.getIdDocumento().equals("")))
						dmc = (IDfSysObject) session.getObject(new DfId(orden.getIdDocumento()));
					tasks = DB.getWorklfowTasks(orden.getNombreWorkflow());
					if (debug) {
						if (tasks != null) {
							for (int j = 0; j < tasks.size(); j++) {
								LPATDAWorkflowTasks task = tasks.get(j);
								logger.write("Tarea: " + task.getNombrePaso());
								logger.write("PropiedadUsuarios: " + task.getPropiedadUsuarios());
							}
						}
					}
					BPM = DB.getWorkflowBPM(orden.getNombreWorkflow());
					if (debug)
						logger.write("evalua si tiene BPM: " + BPM.hasBPM());
					if (BPM.hasBPM()) {
						if (!(BPM.getClaseBPM().equals(""))) {
							if (debug)
								logger.write("BPM clase: " + BPM.getClaseBPM());
							creaDocBPM(orden, props.getPathBPM());
						}
					}
					lanzaWorkflow(dmc, orden.getNombreWorkflow());
					DB.updateOrden(orden.getID());
					if (debug)
						logger.write("Finalizada Orden: " + orden.getID());
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
			if (sessionMgr != null) {
				if (session != null)
					sessionMgr.release(session);
			}
			
				
			if (DB != null)
				DB.desconectar();
		}

	}
}