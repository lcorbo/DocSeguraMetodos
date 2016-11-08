package ar.com.lpa.documentum;

import java.io.File;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

public class LPAWorkflowSavePropsDocMig implements IDmMethod {

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

	private void writeMultipleBookmarks(ArrayList<LPATDAWorkflowDoc> bookmarks,
			String path, IDfSysObject dmcDoc, LPATDABOF BOF, String idWF,
			String password, String marcador) throws Exception {
		LPAServiceWordJawin word = null;
		boolean desprotegido = false;
		try {
			try {
				word = new LPAServiceWordJawin();
			} catch (Exception e) {
				logger.writeTrace(e);
				throw e;
			}
			if (word.getWord() != null) {
				try {
					word.abreDoc(path);
				} catch (Exception e) {
					logger.writeTrace(e);
					throw e;
				}
			}
			if (word.getDoc() != null) {
				try {
					word.desprotegeWord(password);
					desprotegido = true;
				} catch (Exception e) {
					logger.writeTrace(e);
					throw e;
				}
			}
			if (word.getDoc() != null) {
				for (int i = 0; i < bookmarks.size(); i++) {
					LPATDAWorkflowDoc bookmark = bookmarks.get(i);
					if (debug)
						logger.write("writeMultipleBookmarks - bookmark "
								+ bookmark.getId()
								+ ": "
								+ bookmark.getBookmark());
					try {
						if (bookmark.getTipo().equals("string")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									dmcDoc.getString(bookmark.getPropiedad()),
									marcador);
						}
						if (bookmark.getTipo().equals("repeatingstring")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									dmcDoc.getAllRepeatingStrings(
											bookmark.getPropiedad(), ";"),
									marcador);
						}
						if (bookmark.getTipo().equals("integer")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									String.valueOf(dmcDoc.getInt(bookmark
											.getPropiedad())), marcador);
						}
						if (bookmark.getTipo().equals("date")) {
							if (!dmcDoc.getTime(bookmark.getPropiedad())
									.isNullDate()) {
								Format formato = new SimpleDateFormat(
										"dd/MM/yyyy");
								word.escribeBookmarkDoc(bookmark.getBookmark(),
										formato.format(dmcDoc.getTime(
												bookmark.getPropiedad())
												.getDate()), marcador);
							}
						}
						if (bookmark.getTipo().equals("long")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									String.valueOf(dmcDoc.getLong(bookmark
											.getPropiedad())), marcador);
						}
						if (bookmark.getTipo().equals("boolean")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									String.valueOf(dmcDoc.getBoolean(bookmark
											.getPropiedad())), marcador);
						}
						if (bookmark.getTipo().equals("clase")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									dmcDoc.getType().getDescription(), marcador);
						}
						if (bookmark.getTipo().equals("estado")) {
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									dmcDoc.getCurrentStateName(), marcador);
						}
						if (bookmark.getTipo().equals("usuarios")) {
							String usuario = "";
							if (bookmark.getBookmark().equals("Editor")) {
								IDfTime fecha = dmcDoc.getTime("p_feditor");
								Format formato = new SimpleDateFormat(
										"dd/MM/yyyy");
								usuario = dmcDoc.getString("p_editor") + "-"
										+ formato.format(fecha.getDate());
							} else if (bookmark.getBookmark().equals("Revisor")) {
								IDfTime fecha = dmcDoc.getTime("p_frevisor");
								Format formato = new SimpleDateFormat(
										"dd/MM/yyyy");
								usuario = dmcDoc.getAllRepeatingStrings(
										"p_revisor", ";")
										+ "-"
										+ formato.format(fecha.getDate());
							} else if (bookmark.getBookmark().equals(
									"Aprobador")) {
								IDfTime fecha = dmcDoc.getTime("p_faprobador");
								Format formato = new SimpleDateFormat(
										"dd/MM/yyyy");
								usuario = dmcDoc.getAllRepeatingStrings(
										"p_aprobador", ";")
										+ "-"
										+ formato.format(fecha.getDate());
							}
							word.escribeBookmarkDoc(bookmark.getBookmark(),
									usuario, marcador);
						}
						if (bookmark.getTipo().equals("sectorBago")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									consultaDescSectorBago(dmcDoc
											.getString(bookmark.getPropiedad())),
									marcador);
						}
						if (bookmark.getTipo().equals("codigoBago")) {
							word.escribeBookmarkDoc(
									bookmark.getBookmark(),
									dmcDoc.getString(BOF.getPropiedadCodigo())
											+ " - "
											+ dmcDoc.getString(BOF
													.getPropiedadVersion()),
									marcador);
						}
					} catch (Exception e) {
						logger.writeTrace(e);
					}
				}
			}
			if (word.getDoc() != null) {
				if (desprotegido) {
					try {
						word.protegeDoc(password);
					} catch (Exception e) {
						logger.writeTrace(e);
						throw e;
					}
				}
			}
			if (word.getDoc() != null) {
				try {
					word.cierraDoc();
				} catch (Exception e) {
					logger.writeTrace(e);
					throw e;
				}
			}
			if (word.getWord() != null) {
				try {
					word.cierraWord();
				} catch (Exception e) {
					logger.writeTrace(e);
					throw e;
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public void execute(Map params, OutputStream ostream) throws Exception {
		/*LPAServiceConfigurator props = new LPAServiceConfigurator();
		logger = new LPAServiceLogger(props.getLog());
		debug = props.getDebug();
		initWorkflowParams(params);
		IDfSessionManager sessionManager = login();
		LPAServiceDataBase DB = null;
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
			ArrayList<LPATDAWorkflowDoc> bookmarks = DB.getBookmarksWord(dmcDoc
					.getTypeName());

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
					File clase = new File(LPAWorkflowSavePropsDocMig.class
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
					String filePath = path + aux + "." + ext;
					if (debug) {
						logger.write("filePath: "
								+ filePath);
					}
					dmcDoc.getFileEx2(filePath, dmcDoc.getContentType(), 0, "",
							false);

					boolean word = false;
					while (!word) {
						try {
							DB.getConnection().setAutoCommit(false);
							DB.bloqueoWord();
							if (debug)
								logger.write("word bloqueado");
							writeMultipleBookmarks(bookmarks, filePath, dmcDoc,
									BOF, wfId.toString(), props.getPassDoc(),
									props.getBookmark());
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
			logger.writeTrace(e);
			throw e;
		} finally {
			if (session != null)
				sessionManager.release(session);
			
				
			if (DB != null)
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

		return null;*/
	}

}
