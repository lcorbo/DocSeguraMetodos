package ar.com.lpa.documentum;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class LPAServiceDataBase {

	protected Connection connection;

	protected LPAServiceLogger logger;

	private LPAServiceDataBase() {

	}

	public LPAServiceDataBase(LPAServiceLogger logger) {
		this.setLogger(logger);
	}

	private void setLogger(LPAServiceLogger logger) {
		this.logger = logger;
	}

	private LPAServiceLogger getLogger() {
		return this.logger;
	}

	private void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public void conectar(LPAServiceConfigurator props) throws Exception {

		try {

			DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
			String host = "";
			if (props.getInstanceDB().equals(""))
				host = props.getHostDB();
			else
				host = props.getHostDB() + ";instanceName="	+ props.getInstanceDB();
			this.setConnection(DriverManager.getConnection("jdbc:sqlserver://" + host + ";DatabaseName=" + props.getDB(),props.getUserDB(),props.getPassDB()));

		} catch (Exception e) {
			throw e;
		}

	}

	public void desconectar() {
		try {
			if (this.getConnection() != null && !this.getConnection().isClosed())
				this.getConnection().close();
		} catch (SQLException e) {
			this.getLogger().writeTrace(e);
		}

	}

	public void deleteRelations(String codigo, String version) throws Exception {
		CallableStatement cs = null;
		try {
			cs = this.getConnection().prepareCall("{call DeleteRelaciones(?,?)}");

			cs.setString(1, codigo);
			cs.setString(2, version);
			cs.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
	}

	public void deleteAnexos(String codigo, String version) throws Exception {
		CallableStatement cs = null;
		try {
			cs = this.getConnection().prepareCall("{call DeleteAnexos(?,?)}");

			cs.setString(1, codigo);
			cs.setString(2, version);
			cs.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void deleteHistorialVersion(String codigo, String version) throws Exception {
		CallableStatement cs = null;
		try {
			cs = this.getConnection().prepareCall("{call DeleteHistorialDocVersion(?,?)}");

			cs.setString(1, codigo);
			cs.setString(2, version);
			cs.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public LPATDANomenclature getNomenclature(String clase) throws Exception {
		CallableStatement cs = null;
		ResultSet cursor = null;
		LPATDANomenclature nomenclador = null;

		try {
			cs = this.getConnection().prepareCall("{call SelectReglasNomenclador(?)}");
			
			cs.setString(1, clase);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				nomenclador = new LPATDANomenclature();
				nomenclador.setNumerador(cursor.getString("numerador"));
				nomenclador.setPropiedades(cursor.getString("propiedades"));
				nomenclador.setTipoPropiedades(cursor.getString("tipoPropiedades"));
				nomenclador.setPropiedadCodigo(cursor.getString("propiedadCodigo"));
				nomenclador.setPropiedadNumerador(cursor.getString("propiedadNumerador"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}

		return nomenclador;
	}

	public LPATDABOF getBOF(String clase) throws Exception {
		CallableStatement cs = null;
		ResultSet cursor = null;
		LPATDABOF BOF = null;

		try {
			cs = this.getConnection().prepareCall("{call SelectBOF(?)}");

			cs.setString(1, clase);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				BOF = new LPATDABOF();
				BOF.setClase(clase);
				BOF.setLifeCycle(cursor.getString("lifeCycle"));
				BOF.setWorkflow(cursor.getString("workflow"));
				BOF.setPropiedadCodigo(cursor.getString("propiedadCodigo"));
				BOF.setPropiedadGrupos(cursor.getString("propiedadGrupos"));
				BOF.setPropiedadSector(cursor.getString("propiedadSector"));
				BOF.setPropiedadesUsuarios(cursor.getString("propiedadesUsuarios"));
				BOF.setGruposExtras(cursor.getString("gruposExtra"));
				BOF.setGruposFueraVigencia(cursor.getString("gruposFueraVigencia"));
				BOF.setPropiedadReemplazos(cursor.getString("propiedadReemplazos"));
				BOF.setPropiedadVersion(cursor.getString("propiedadVersion"));
				BOF.setEstadoVigente(cursor.getString("estadoVigente"));
				BOF.setEstadoInicial(cursor.getString("estadoInicial"));
				BOF.setPropiedadCopia(cursor.getString("propiedadCopia"));
				BOF.setPropiedadRevision(cursor.getString("propiedadRevision"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}

		return BOF;
	}

	public LPATDAWorkflowBPM getWorkflowBPM(String workflow) throws Exception {
		CallableStatement cs = null;
		ResultSet cursor = null;
		LPATDAWorkflowBPM BPM = null;

		try {

			cs = this.getConnection().prepareCall("{call SelectWorkflowBPM(?)}");

			cs.setString(1, workflow);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				BPM = new LPATDAWorkflowBPM();
				BPM.setWorkflow(workflow);
				BPM.setPackageAdjunto(cursor.getString("packageAdjunto"));
				BPM.setPackageBPM(cursor.getString("packageBPM"));
				BPM.setClaseBPM(cursor.getString("claseBPM"));
				BPM.setPropiedades(cursor.getString("propiedades"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}

		return BPM;
	}

	public int getNumberNomenclature(String clase, String propiedades) throws Exception {
		CallableStatement cs = null;
		ResultSet cursor = null;
		int numero = 0;

		try {
			cs = this.getConnection().prepareCall("{call IncrementaNumeroNomenclador(?,?)}");

			cs.setString(1, clase);
			cs.setString(2, propiedades);
			cs.executeUpdate();

			cs.close();

			cs = this.getConnection().prepareCall("{call SelectNumeroNomenclador(?,?)}");

			cs.setString(1, clase);
			cs.setString(2, propiedades);
			cursor = cs.executeQuery();
			if (cursor.next()) {
				numero = cursor.getInt("numerador");
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}
		}

		return numero;
	}
	
	
	public int getNumberADJNomenclature(String clase, String propiedades) throws Exception {
		CallableStatement cs = null;
		ResultSet cursor = null;
		int numero = 0;

		try {
			logger.write("IncrementaNumeroAdjNomenclador");	
			cs = this.getConnection().prepareCall("{call IncrementaNumeroAdjNomenclador(?,?)}");
			logger.write("IncrementaNumeroAdjNomenclador - clase " + clase);	
			logger.write("IncrementaNumeroAdjNomenclador - propiedades " + propiedades);	
			cs.setString(1, clase);
			cs.setString(2, propiedades);
			cs.executeUpdate();

			cs.close();
			
			logger.write("SelectNumeroAdjNomenclador");	
			cs = this.getConnection().prepareCall("{call SelectNumeroAdjNomenclador(?,?)}");

			cs.setString(1, clase);
			cs.setString(2, propiedades);
			cursor = cs.executeQuery();
			if (cursor.next()) {
				numero = cursor.getInt("numerador_adj");
			}
			logger.write("SelectNumeroAdjNomenclador - numero : " + numero);	

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}
		}

		return numero;
	}

	public ArrayList<LPATDAWorkflowTasks> getWorklfowTasks(String workflowName) throws Exception {

		CallableStatement cs = null;
		ResultSet cursor = null;
		ArrayList<LPATDAWorkflowTasks> tasks = new ArrayList<LPATDAWorkflowTasks>();

		try {

			cs = this.getConnection().prepareCall("{call SelectWorkflowTasks(?)}");

			cs.setString(1, workflowName);

			cursor = cs.executeQuery();

			while (cursor.next()) {
				LPATDAWorkflowTasks task = new LPATDAWorkflowTasks();
				task.setNombreWorkflow(workflowName);
				task.setNombrePaso(cursor.getString("nombrePaso"));
				task.setPropiedadUsuarios(cursor.getString("propiedadUsuarios"));
				task.setDocumento(cursor.getString("documento"));
				tasks.add(task);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}

		return tasks;
	}

	public ArrayList<LPATDAWorkflowDoc> getBookmarksWord(String clase, String nombreWF) throws Exception {

		CallableStatement cs = null;
		ResultSet cursor = null;
		ArrayList<LPATDAWorkflowDoc> bookmarks = new ArrayList<LPATDAWorkflowDoc>();

		try {

			cs = this.getConnection().prepareCall("{call SelectBookmarksWord(?,?)}");

			cs.setString(1, clase);
			cs.setString(2,nombreWF);

			cursor = cs.executeQuery();

			while (cursor.next()) {
				LPATDAWorkflowDoc bookmark = new LPATDAWorkflowDoc();
				bookmark.setId(cursor.getInt("ID"));
				bookmark.setClase(clase);
				bookmark.setBookmark(cursor.getString("bookmark"));
				bookmark.setPropiedad(cursor.getString("propiedad"));
				bookmark.setTipo(cursor.getString("tipo"));
				bookmark.setReemplazar(cursor.getString("reemplazar").equalsIgnoreCase("S"));
				bookmarks.add(bookmark);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}

		return bookmarks;
	}

	public ArrayList<LPATDARelation> getRelations(String idDoc, String version)	throws Exception {

		CallableStatement cs = null;
		ResultSet cursor = null;
		ArrayList<LPATDARelation> relations = new ArrayList<LPATDARelation>();

		try {

			cs = this.getConnection().prepareCall("{call SelectRelaciones(?,?)}");

			cs.setString(1, idDoc);
			cs.setString(2, version);
			cursor = cs.executeQuery();

			while (cursor.next()) {
				LPATDARelation relation = new LPATDARelation();
				relation.setIdRelacion(cursor.getInt("idrelacion"));
				relation.setIdDocOrigen(cursor.getString("idOrigen"));
				relation.setIdDocDestino(cursor.getString("idDestino"));
				relation.setCodigoDocOrigen(cursor.getString("codigoDocOrigen"));
				relation.setCodigoDocDestino(cursor.getString("codigoDocDestino"));
				relation.setVersionDocOrigen(cursor.getString("versionDocOrigen"));
				relation.setVersionDocDestino(cursor.getString("versionDocDestino"));
				relation.setUsuario(cursor.getString("usuario"));
				relation.setFecha(cursor.getDate("fechaRelacion"));
				relations.add(relation);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}

		return relations;
	}

	public ArrayList<LPATDAAttach> getAttachs(String idDoc, String version) throws Exception {

		CallableStatement cs = null;
		ResultSet cursor = null;
		ArrayList<LPATDAAttach> attachs = new ArrayList<LPATDAAttach>();

		try {

			cs = this.getConnection().prepareCall("{call SelectAnexos(?,?)}");

			cs.setString(1, idDoc);
			cs.setString(2, version);
			cursor = cs.executeQuery();

			while (cursor.next()) {
				LPATDAAttach attach = new LPATDAAttach();
				attach.setIdRelacion(cursor.getInt("idrelacion"));
				attach.setIdDocOrigen(cursor.getString("idOrigen"));
				attach.setIdDocDestino(cursor.getString("idDestino"));
				attach.setCodigoDocOrigen(cursor.getString("codigoDocOrigen"));
				attach.setCodigoDocDestino(cursor.getString("codigoDocDestino"));
				attach.setVersionDocOrigen(cursor.getString("versionDocOrigen"));
				attach.setVersionDocDestino(cursor.getString("versionDocDestino"));
				attach.setUsuario(cursor.getString("usuario"));
				attach.setFecha(cursor.getDate("fechaRelacion"));
				attachs.add(attach);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}

		return attachs;
	}

	public void insertAttach(LPATDAAttach anexo) throws SQLException {
		CallableStatement cs = null;
		try {
			this.getConnection().setAutoCommit(false);

			cs = this.getConnection().prepareCall("{call InsertAnexo(?,?,?,?,?,?,?)}");

			cs.setString(1, anexo.getIdDocOrigen());
			cs.setString(2, anexo.getIdDocDestino());
			cs.setString(3, anexo.getUsuario());
			cs.setString(4, anexo.getCodigoDocOrigen());
			cs.setString(5, anexo.getCodigoDocDestino());
			cs.setString(6, anexo.getVersionDocOrigen());
			cs.setString(7, anexo.getVersionDocDestino());

			cs.executeUpdate();

			this.getConnection().commit();

		} catch (SQLException e) {
			if (this.getConnection() != null) {
				try {
					this.getConnection().rollback();
				} catch (SQLException se) {
					this.getLogger().writeTrace(e);
				}
			}
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}
			this.getConnection().setAutoCommit(true);
		}
	}

	public void insertRelation(LPATDARelation relacion) throws SQLException {
		CallableStatement cs = null;
		try {
			this.getConnection().setAutoCommit(false);

			cs = this.getConnection().prepareCall("{call InsertRelacion(?,?,?,?,?,?,?)}");

			cs.setString(1, relacion.getIdDocOrigen());
			cs.setString(2, relacion.getIdDocDestino());
			cs.setString(3, relacion.getUsuario());
			cs.setString(4, relacion.getCodigoDocOrigen());
			cs.setString(5, relacion.getCodigoDocDestino());
			cs.setString(6, relacion.getVersionDocOrigen());
			cs.setString(7, relacion.getVersionDocDestino());

			cs.executeUpdate();

			this.getConnection().commit();

		} catch (SQLException e) {
			if (this.getConnection() != null) {
				try {
					this.getConnection().rollback();
				} catch (SQLException se) {
					this.getLogger().writeTrace(e);
				}
			}
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}
			this.getConnection().setAutoCommit(true);
		}
	}

	public ArrayList<LPATDAOrders> getOrders() throws Exception {
		CallableStatement cs = null;
		ResultSet cursor = null;
		ArrayList<LPATDAOrders> orders = new ArrayList<LPATDAOrders>();

		try {
			logger.write("Comienza getOrders");
			cs = this.getConnection().prepareCall("{call SelectOrders()}");

			cursor = cs.executeQuery();

			while (cursor.next()) {
				LPATDAOrders order = new LPATDAOrders();
				order.setID(cursor.getInt("idOrden"));
				order.setNombreWorkflow(cursor.getString("nombreWorkflow"));
				order.setIdDocumento(cursor.getString("idDocumento"));
				order.setValoresPropiedadesBPM(cursor.getString("valoresPropiedadesBPM"));
				orders.add(order);
				logger.write("orden " + order.getID());
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}

		return orders;
	}

	public void updateOrden(int id) throws Exception {
		CallableStatement cs = null;

		try {

			cs = this.getConnection().prepareCall("{call UpdateOrden(?)}");

			cs.setInt(1, id);
			cs.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void deshabilitaImpresion(String codigo) throws Exception {
		CallableStatement cs = null;

		try {
			this.getConnection().setAutoCommit(false);
			cs = this.getConnection().prepareCall("{call DeshabilitaImpresion(?)}");

			cs.setString(1, codigo);
			cs.executeUpdate();
			this.getConnection().commit();
		} catch (Exception e) {
			this.getConnection().rollback();
			throw e;
		} finally {
			this.getConnection().setAutoCommit(true);
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void habilitaImpresion(String codigo, String sector) throws Exception {

		CallableStatement cs = null;

		try {
			this.getConnection().setAutoCommit(false);
			cs = this.getConnection().prepareCall("{call HabilitaImpresion(?,?)}");

			cs.setString(1, codigo);
			cs.setString(2, sector);
			cs.executeUpdate();
			this.getConnection().commit();
		} catch (Exception e) {

			this.getConnection().rollback();
			throw e;

		} finally {

			this.getConnection().setAutoCommit(true);

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void renuevaImpresion(String codigo, String sector, String version) throws Exception {

		CallableStatement cs = null;

		try {
			this.getConnection().setAutoCommit(false);
			cs = this.getConnection().prepareCall("{call RenuevaImpresion(?,?,?)}");

			cs.setString(1, codigo);
			cs.setString(2, sector);
			cs.setString(3, version);
			cs.executeUpdate();
			this.getConnection().commit();
		} catch (Exception e) {
			this.getConnection().rollback();
			throw e;
		} finally {
			this.getConnection().setAutoCommit(true);

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
	}

	public LPATDAWorkflowLogger getWorkflowLogger(String paso) throws Exception {

		LPATDAWorkflowLogger log = null;
		CallableStatement cs = null;
		ResultSet cursor = null;

		try {

			cs = this.getConnection().prepareCall("{call SelectInfoWorkflowLog(?)}");

			cs.setString(1, paso);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				log = new LPATDAWorkflowLogger();
				log.setMensaje(cursor.getString("mensajeLog"));
				log.setPasoAnterior(cursor.getString("pasoAnterior"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
		return log;
	}

	public void insertWorkflowComments(LPATDAWorkflowComments comentario) throws Exception {
		CallableStatement cs = null;

		try {

			cs = this.getConnection().prepareCall("{call InsertWorkflowComments(?,?,?,?,?)}");

			cs.setString(1, comentario.getNombreWorkflow());
			cs.setString(2, comentario.getIdDocumento());
			cs.setString(3, comentario.getIdComentario());
			cs.setString(4, comentario.getCodDocumento());
			cs.setString(5, comentario.getVersion());

			cs.execute();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void insertWorkflowLog(LPATDAWorkflowLogger log) throws Exception {
		CallableStatement cs = null;

		try {
			cs = this.getConnection().prepareCall("{call InsertWorkflowLog(?,?,?,?,?,?,?)}");

			cs.setString(1, log.getUsuario());
			cs.setString(2, log.getMensaje());
			cs.setString(3, log.getCodDocumento());
			cs.setString(4, log.getIdWorkflow());
			cs.setString(5, log.getNombreWorkflow());
			cs.setString(6, log.getVersion());
			cs.setString(7, log.getIdDocumento());

			cs.execute();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
	}

	public void insertWorkflowOrden(String workflow, String idDocumento, String propiedades, String precargado) throws Exception {
		CallableStatement cs = null;

		try {
			cs = this.getConnection().prepareCall("{call InsertOrden(?,?,?,?)}");

			cs.setString(1, workflow);
			cs.setString(2, idDocumento);
			cs.setString(3, propiedades);
			cs.setString(4, precargado);

			cs.execute();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
	}

	public LPATDAMail getWorkflowMail(LPATDAMail mail, String paso) throws Exception {

		CallableStatement cs = null;
		ResultSet cursor = null;

		try {
			cs = this.getConnection().prepareCall("{call SelectInfoWorkflowMail(?)}");

			cs.setString(1, paso);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				mail.setSubject(cursor.getString("subject"));
				mail.setBody(cursor.getString("body"));
				mail.setDestinatarios(cursor.getString("destinatarios"));
				mail.setPropiedadBPM(cursor.getString("propiedadBPM"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
		return mail;
	}

	public LPATDAExpiration getPropsVencimiento(String clase) throws Exception {

		LPATDAExpiration vencimiento = null;
		CallableStatement cs = null;
		ResultSet cursor = null;

		try {

			cs = this.getConnection().prepareCall("{call SelectInfoVencimientos(?)}");

			cs.setString(1, clase);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				vencimiento = new LPATDAExpiration();
				vencimiento.setPropiedadVigencia(cursor.getString("propiedadVigencia"));
				vencimiento.setPropiedadVencimiento(cursor.getString("propiedadVencimiento"));
				vencimiento.setDiasVencimiento(cursor.getInt("diasVencimiento"));
				vencimiento.setPropiedadNotificado(cursor.getString("propiedadNotificado"));
				vencimiento.setDiasNotificacion(cursor.getInt("diasNotificacion"));
				vencimiento.setDiasPreVencimiento(cursor.getInt("diasPreVencimiento"));
				vencimiento.setPropiedadNotificado(cursor.getString("workflowVencimiento"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
		return vencimiento;
	}

	public ArrayList<LPATDAExpiration> getVencimientos() throws Exception {

		ArrayList<LPATDAExpiration> array = new ArrayList<LPATDAExpiration>();
		LPATDAExpiration vencimiento = null;
		CallableStatement cs = null;
		ResultSet cursor = null;

		try {

			cs = this.getConnection().prepareCall("{call SelectTodosVencimientos()}");

			cursor = cs.executeQuery();

			while (cursor.next()) {
				vencimiento = new LPATDAExpiration();
				vencimiento.setClase(cursor.getString("clase"));
				vencimiento.setPropiedadVencimiento(cursor.getString("propiedadVencimiento"));
				vencimiento.setDiasVencimiento(cursor.getInt("diasVencimiento"));
				vencimiento.setPropiedadNotificado(cursor.getString("propiedadNotificado"));
				vencimiento.setDiasNotificacion(cursor.getInt("diasNotificacion"));
				vencimiento.setDiasPreVencimiento(cursor.getInt("diasPreVencimiento"));
				vencimiento.setWorkflowVencimiento(cursor.getString("workflowVencimiento"));
				vencimiento.setEstadoVigente(cursor.getString("estadoVigente"));
				array.add(vencimiento);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}

			}

		}
		return array;
	}

	public LPATDAMigracionDatosInq getDatosInQ(String codigo, String version) throws Exception {

		LPATDAMigracionDatosInq datos = null;
		CallableStatement cs = null;
		ResultSet cursor = null;

		try {

			cs = this.getConnection().prepareCall("{call Sp_InfoDocInQ(?,?)}");

			cs.setString(1, codigo + "%");
			cs.setString(2, version);

			cursor = cs.executeQuery();

			if (cursor.next()) {
				datos = new LPATDAMigracionDatosInq();
				datos.setFechaEdicion(cursor.getDate("FechaEdicion"));
				datos.setFechaRevision(cursor.getDate("FechaRevision"));
				datos.setFechaAprobacion(cursor.getDate("FechaAprobacion"));
				datos.setFechaVigencia(cursor.getDate("FechaVigencia"));
				datos.setFechaVencimiento(cursor.getDate("FechaVencimiento"));
				datos.setTitulo(cursor.getString("Titulo"));
				datos.setSector(cursor.getString("CodSector").toLowerCase());
				datos.setFilial(cursor.getString("CodEmpresa"));
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
		return datos;
	}

	public ArrayList<LPATDAMigracionHistImpresiones> getHistImpresionesInQ(String codigo, String id, String version) throws Exception {

		ArrayList<LPATDAMigracionHistImpresiones> array = new ArrayList<LPATDAMigracionHistImpresiones>();
		CallableStatement cs = null;
		ResultSet cursor = null;

		try {

			cs = this.getConnection().prepareCall("{call HistorialImpresion(?,?)}");

			cs.setString(1, codigo + "%");
			int majorVersion = Integer.parseInt(version.substring(0, version.indexOf('.')));
			DecimalFormat formatter = new DecimalFormat("#00.###");
			String versionAux = formatter.format(majorVersion);
			cs.setString(2, versionAux);

			cursor = cs.executeQuery();

			while (cursor.next()) {
				LPATDAMigracionHistImpresiones datos = new LPATDAMigracionHistImpresiones();
				datos.setCodigo(codigo);
				datos.setComentario(cursor.getString("Comentarios"));
				datos.setFecha(cursor.getDate("FechaImpresion"));
				datos.setTipoCopia(cursor.getString("TipoImpresion"));
				datos.setUsuario(cursor.getString("NombreUsuario"));
				datos.setVersion(version);
				datos.setIdDocumento(id);
				array.add(datos);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (cursor != null) {
				try {
					cursor.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
		return array;
	}

	public void insertHistoriaMig(LPATDAMigracionHistImpresiones datos) throws Exception {

		CallableStatement cs = null;
		try {
			java.sql.Date Fecha = new java.sql.Date(datos.getFecha().getTime());
			cs = this.getConnection().prepareCall("{call InsertHistorialImpresionMigracion(?,?,?,?,?,?,?)}");

			cs.setString(1, datos.getUsuario());
			cs.setInt(2, datos.getTipoCopia());
			cs.setString(3, datos.getComentario());
			cs.setString(4, datos.getCodigo());
			cs.setString(5, datos.getVersion());
			cs.setString(6, datos.getIdDocumento());
			cs.setDate(7, Fecha);
			cs.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void bloqueoWord() throws Exception {
		CallableStatement cs = null;

		try {
			cs = this.getConnection().prepareCall("{call BloqueoWord()}");

			cs.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
	}

	public void insertWorkflowOrdenMail(String usuarios, String asunto,	String cuerpo) throws Exception {
		CallableStatement cs = null;

		try {

			cs = this.getConnection().prepareCall("{call InsertOrdenMail(?,?,?,?)}");

			cs.setString(1, usuarios);
			cs.setString(2, asunto);
			cs.setString(3, cuerpo);

			cs.execute();

		} catch (Exception e) {
			throw e;
		} finally {
			if (cs != null) {
				try {
					cs.close();
				} catch (SQLException e) {
					this.getLogger().writeTrace(e);
				}
			}

		}
		
	}
}
