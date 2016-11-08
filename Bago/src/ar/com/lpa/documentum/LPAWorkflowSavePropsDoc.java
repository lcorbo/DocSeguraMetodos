package ar.com.lpa.documentum;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Bookmarks;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import com.documentum.mthdservlet.IDmMethod;

public class LPAWorkflowSavePropsDoc implements IDmMethod {

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

	private String consultaUsuariosFechas(String idWF, String paso)
			throws Exception {
		IDfCollection col = null;
		String resultado = "";
		try {
			IDfQuery cons = new DfQuery();
			cons.setDQL("select a.r_performer_name, c.dequeued_date from dmi_workitem_s a, dm_process_r b, dmi_queue_item_s c where a.r_workflow_id = '"
					+ idWF
					+ "' and a.r_act_def_id = b.r_act_def_id and c.item_id = a.r_object_id and b.r_act_name = '"
					+ paso
					+ "' and c.task_state = 'finished' and a.r_creation_date = (select max(a.r_creation_date) from dmi_workitem_s a, dm_process_r b, dmi_queue_item_s c where a.r_workflow_id = '"
					+ idWF
					+ "' and a.r_act_def_id = b.r_act_def_id and c.item_id = a.r_object_id and b.r_act_name = '"
					+ paso + "')");
			if (debug)
				logger.write("DQL: "
						+ cons.getDQL());
			col = cons.execute(session, IDfQuery.DF_QUERY);
			while (col.next()) {
				IDfTime fecha = col.getTime("dequeued_date");
				Format formato = new SimpleDateFormat("dd/MM/yyyy");
				resultado = resultado + col.getString("r_performer_name") + "-"
						+ formato.format(fecha.getDate()) + ";";
			}
			col.close();
			if (resultado.length() > 0)
				resultado = resultado.substring(0, resultado.length() - 1);
			if (debug)
				logger.write("resultado: "
						+ resultado);
		} catch (Exception e) {
			throw e;
		} finally {
			if (col != null) {
				try {
					col.close();
				} catch (Exception e) {
					logger.write("Error en doc:"+m_workitemId);
					logger.writeTrace(e);
				}
			}
		}
		return resultado;
	}

	private String consultaDescSectorBago(String sector) throws Exception {
		IDfCollection col = null;
		String resultado = "";
		try {
			IDfQuery cons = new DfQuery();
			cons.setDQL("select description from dm_group where group_name = '"
					+ sector + "'");
			if (debug)
				logger.write("DQL: "
						+ cons.getDQL());
			col = cons.execute(session, IDfQuery.DF_QUERY);
			if (col.next()) {
				resultado = col.getString("description");
			}
			col.close();
			if (debug)
				logger.write("resultado: "
						+ resultado);
		} catch (Exception e) {
			throw e;
		} finally {
			if (col != null) {
				try {
					col.close();
				} catch (Exception e) {
					logger.writeTrace(e);
				}
			}
		}
		return resultado;
	}
	
	private String consultanombreWorkflow(String idWf) throws Exception {
		IDfCollection col = null;
		String resultado = "";
		try {
			IDfQuery cons = new DfQuery();
			cons.setDQL("select d.object_name " +
					"from dmi_workitem_s a, dm_workflow_s d " +
					"where a.r_workflow_id = d.r_object_id " +
					"and a.r_workflow_id = '"+idWf+"'");
			if (debug)
				logger.write("DQL: "
						+ cons.getDQL());
			col = cons.execute(session, IDfQuery.DF_QUERY);
			if (col.next()) {
				resultado = col.getString("object_name");
			}
			resultado = resultado.split(" ")[0];
			col.close();
			if (debug)
				logger.write("Nombre WF: "
						+ resultado);
		} catch (Exception e) {
			throw e;
		} finally {
			if (col != null) {
				try {
					col.close();
				} catch (Exception e) {
					logger.writeTrace(e);
				}
			}
		}
		return resultado;
	}

	private void writeMultipleBookmarks(ArrayList<LPATDAWorkflowDoc> bookmarks,
			String path, IDfSysObject dmcDoc, LPATDABOF BOF, String idWF,
			String password, String marcador, String nombreWorkflow) throws Exception {
		LPAServiceWord word = null;
		boolean desprotegido = false;
		String state="";
		String vige="";
		LPATDAWorkflowDoc vig = new LPATDAWorkflowDoc();
		try {
			try {
				word = new LPAServiceWord();
			} catch (Exception e) {
				logger.write("Error en doc:"+m_workitemId);
				logger.writeTrace(e);
				throw e;
			}
			if (word.getWord() != null) {
				try {
					POIFSFileSystem fs = new POIFSFileSystem(
							new FileInputStream(path));
					HWPFDocument doc = new HWPFDocument(fs);
					Bookmarks lista = doc.getBookmarks();
					ArrayList<String> bookmarkNames = new ArrayList<String>();
					ArrayList<String> list = new ArrayList<String>();
					for (int i = 0; i < bookmarks.size(); i++)
						bookmarkNames.add(bookmarks.get(i).getBookmark());
					for (int i = 0; i < lista.getBookmarksCount(); i++)
						list.add(lista.getBookmark(i).getName());
					CollectionUtils operaciones = new CollectionUtils();
					ArrayList<String> diferencias = (ArrayList<String>) operaciones
							.disjunction(bookmarkNames, list);
					for (int i = 0; i < bookmarks.size(); i++) {
						for (int j = 0; j < diferencias.size(); j++) {
							if (bookmarks.get(i).getBookmark()
									.equalsIgnoreCase(diferencias.get(j))) {
								logger.write(m_workitemId+": El bookmark "
										+ diferencias.get(j)
										+ " no existe en el documento");
								bookmarks.remove(i);
								diferencias.remove(j);
							}
						}
					}
					word.abreDoc(path);
				} catch (Exception e) {
					logger.write("Error en doc:"+m_workitemId);
					logger.writeTrace(e);
					throw e;
				}
			}
			if (word.getDoc() != null) {
				try {
					word.desprotegeWord(password);
					desprotegido = true;
				} catch (Exception e) {
					logger.write("Error en doc:"+m_workitemId);
					logger.writeTrace(e);
					throw e;
				}
			}
			if (word.getDoc() != null) {
				for (int i = 0; i < bookmarks.size(); i++) {
					LPATDAWorkflowDoc bookmark = bookmarks.get(i);
					if (debug)
						logger.write("bookmark "
								+ bookmark.getId()
								+ ": "
								+ bookmark.getBookmark() + " (Tipo: " + bookmark.getTipo() + ")");
					if(bookmark.getBookmark().equalsIgnoreCase("Estado"))
					{
						state=dmcDoc.getCurrentStateName();
						logger.write("Estado es "+state);
					}
					if(bookmark.getBookmark().equalsIgnoreCase("Vigencia"))
					{
						vig=bookmark;
						logger.write("Bookmark vigencia adquirido");
					}
					try {
						if (bookmark.getTipo().equals("string")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									dmcDoc.getString(bookmark.getPropiedad()),
									marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("repeatingstring")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									dmcDoc.getAllRepeatingStrings(
											bookmark.getPropiedad(), ";"),
									marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("integer")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									String.valueOf(dmcDoc.getInt(bookmark
											.getPropiedad())), marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equalsIgnoreCase("date")) {
							
							if (!dmcDoc.getTime(bookmark.getPropiedad())
									.isNullDate()) {
								logger.write("Fecha de origen: "+dmcDoc.getTime(
										bookmark.getPropiedad()).getDate());
								Format formato = new SimpleDateFormat(
										"dd/MM/yyyy");
									word.escribeBookmarkDoc(
										bookmark.getBookmark(),
										formato.format(dmcDoc.getTime(
												bookmark.getPropiedad()).getDate()),
										marcador, bookmark.isReemplazar());
									logger.write("Fecha: "+formato.format(dmcDoc.getTime(
												bookmark.getPropiedad()).getDate()));
							}
						}
						if (bookmark.getTipo().equals("long")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									String.valueOf(dmcDoc.getLong(bookmark
											.getPropiedad())), marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("boolean")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									String.valueOf(dmcDoc.getBoolean(bookmark
											.getPropiedad())), marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("clase")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									dmcDoc.getType().getDescription(), marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("estado")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									dmcDoc.getCurrentStateName(), marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("usuarios")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									consultaUsuariosFechas(idWF,
											bookmark.getPropiedad()), marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("sectorBago")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									consultaDescSectorBago(dmcDoc
											.getString(bookmark.getPropiedad())),
									marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("multisectorBago")) {
							String sectores = "";
							String aux = "";
							aux = dmcDoc.getAllRepeatingStrings(
									bookmark.getPropiedad(), ";");
							StringTokenizer st = new StringTokenizer(aux, ";");
							while (st.hasMoreTokens()) {
								String sector = (String) st.nextElement();
								if (sectores.length() > 0)
									sectores = sectores + ";";
								sectores = sectores
										+ consultaDescSectorBago(sector);
							}
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									sectores, marcador, bookmark.isReemplazar());
						}
						if (bookmark.getTipo().equals("codigoBago")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									dmcDoc.getString(BOF.getPropiedadCodigo())
											+ " - "
											+ dmcDoc.getString(BOF
													.getPropiedadVersion()),
									marcador, bookmark.isReemplazar());
						}
						if(!nombreWorkflow.equalsIgnoreCase("LPAWorkflowRevision"))
						{
							if(!(state.equalsIgnoreCase("TempVigente") || state.equalsIgnoreCase("Vigente") || state.equalsIgnoreCase("No_Vigente")))
							{
								word.escribeBookmarkDoc(
										"Vigencia",
										"-",
										marcador, bookmark.isReemplazar());
							}
						}
						
					} catch (Exception e) {
						logger.write("Error en doc:"+m_workitemId);
						logger.writeTrace(e);
					}
				}
			}
			if (word.getDoc() != null) {
				if (desprotegido) {
					try {
						word.protegeDoc(password);
						logger.write("Se protege el documento");
					} catch (Exception e) {
						logger.write("Error en doc:"+m_workitemId);
						logger.writeTrace(e);
						throw e;
					}
				}
			}
			if (word.getDoc() != null) {
				try {
					word.cierraDoc();
					logger.write("Se cierra el doc");

				} catch (Exception e) {
					logger.write("Error en doc:"+m_workitemId);
					logger.writeTrace(e);
					throw e;
				}
			}
			if (word.getWord() != null) {
				try {
					word.cierraWord();
					logger.write("Se cierra el word");
				} catch (Exception e) {
					logger.write("Error en doc:"+m_workitemId);
					logger.writeTrace(e);
					throw e;
				}
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
		String nombreWorkflow = "";
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
			LPAServiceBPM ServiceBPM = new LPAServiceBPM(session, logger,
					debug, BPM);

			ServiceBPM.loadDocuments(workitem.getPackages(""));

			IDfSysObject dmcDoc = ServiceBPM.getDocumento();
			nombreWorkflow = consultanombreWorkflow(wfId.toString());
			ArrayList<LPATDAWorkflowDoc> bookmarks = DB.getBookmarksWord(dmcDoc
					.getTypeName(),nombreWorkflow);
			logger.write(m_workitemId+":Clase del SP:"+dmcDoc.getTypeName());
			if (bookmarks != null) {
				LPATDABOF BOF = DB.getBOF(dmcDoc.getTypeName());
				IDfFormat myFormat = session.getFormat(dmcDoc.getContentType());

				if (debug) {
					logger.write("Formato: "
							+ myFormat.getDescription());
					logger.write("MIME: "
							+ myFormat.getMIMEType());
				}

				if (myFormat.getDescription().startsWith("MS Word")) {
					File clase = new File(LPAWorkflowSavePropsDoc.class
							.getResource("LPAWorkflowSavePropsDoc.class")
							.toString());
					File carpeta = clase.getParentFile();
					String path = carpeta.getPath();
					path = path.substring(6);
					path = path + "\\";

					if (debug)
						logger.write("Path: " + path);

					String ext = myFormat.getDOSExtension();

					if (debug)
						logger.write("extension: "
								+ ext);
					String aux = dmcDoc.getObjectName();
					aux = aux.replace("\\", "-");
					aux = aux.replace("/", "-");
					aux = aux.replace(":", "-");
					aux = aux.replace("*", "-");
					aux = aux.replace("?", "-");
					aux = aux.replace("\"", "-");
					aux = aux.replace("<", "-");
					aux = aux.replace(">", "-");
					aux = aux.replace("|", "-");
					String filePath = "C:\\TempEdicion\\" + aux + "." + ext;
					if (debug) {
						logger.write("filePath: "
								+ filePath);
					}
					
					dmcDoc.getFileEx2(filePath, dmcDoc.getContentType(), 0, "",
							false);

					POIFSFileSystem fs = new POIFSFileSystem(
							new FileInputStream(filePath));
					HWPFDocument doc = new HWPFDocument(fs);
					Bookmarks lista = doc.getBookmarks();
					if(lista.getBookmarksCount()==0)
						throw new Exception("El Documento no tiene bookmarks");
					
										
					boolean word = false;
					while (!word) {
						try {
							DB.getConnection().setAutoCommit(false);
							DB.bloqueoWord();
							if (debug)
								logger.write("word bloqueado");
							writeMultipleBookmarks(bookmarks, filePath, dmcDoc,
									BOF, wfId.toString(), props.getPassDoc(),
									props.getBookmark(), nombreWorkflow);
							DB.getConnection().commit();
							word = true;
							if (debug) {
								logger.write("graba bookmarks correctamente");
								logger.write("word desbloqueado");
							}
						} catch (Exception e) {
							DB.getConnection().rollback();
						} finally {
							DB.getConnection().setAutoCommit(true);
						}
						Thread.sleep(60000);
					}

					File archivo = new File(filePath);
					boolean fetch = dmcDoc.fetch(null);
					if (debug)
						logger.write("fetch: "
								+ fetch);
					dmcDoc.setFile(filePath);
					dmcDoc.save();
					if (debug)
						logger.write("guarda el doc en documentum");

					if (archivo.exists()) {
						archivo.delete();
						if (debug)
							logger.write("borra archivo");
					}
				}
			}
			workitem.complete();
			if (debug)
				logger.write("Completa la tarea");

		} catch (Exception e) {
			ostream.write(e.getMessage().getBytes());
			e.printStackTrace();
			logger.write("Error en doc "+ m_workitemId);
			logger.writeTrace(e);
			throw e;
		} finally {
			if (session != null)
				sessionManager.release(session);
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
