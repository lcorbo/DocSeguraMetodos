package ar.com.lpa.documentum;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

public class LPAServiceBPM {

	protected IDfSysObject documento = null;
	protected IDfSysObject parametros = null;
	protected IDfId idPackageDoc = null;
	protected IDfId idPackageBPM = null;
	protected IDfSession session = null;
	protected LPAServiceLogger logger = null;
	protected LPATDAWorkflowBPM BPM = null;
	protected boolean debug = false;

	private LPAServiceBPM() {

	}

	public LPAServiceBPM(IDfSession session, LPAServiceLogger logger,
			boolean debug, LPATDAWorkflowBPM BPM) {
		this.setSession(session);
		this.setLogger(logger);
		this.setDebug(debug);
		this.setBPM(BPM);
	}

	private void setSession(IDfSession session) {
		this.session = session;
	}

	private IDfSession getSession() {
		return this.session;
	}

	private void setDebug(boolean debug) {
		this.debug = debug;
	}

	private boolean getDebug() {
		return this.debug;
	}

	private void setLogger(LPAServiceLogger logger) {
		this.logger = logger;
	}

	private LPAServiceLogger getLogger() {
		return this.logger;
	}

	private void setBPM(LPATDAWorkflowBPM BPM) {
		this.BPM = BPM;
	}

	private LPATDAWorkflowBPM getBPM() {
		return this.BPM;
	}

	private void setDocumento(IDfSysObject documento) {
		this.documento = documento;
	}

	public IDfSysObject getDocumento() {
		return documento;
	}

	private void setParametros(IDfSysObject parametros) {
		this.parametros = parametros;
	}

	public IDfSysObject getParametros() {
		return parametros;
	}

	public IDfId getIdPackageDoc() {
		return idPackageDoc;
	}

	private void setIdPackageDoc(IDfId idPackageDoc) {
		this.idPackageDoc = idPackageDoc;
	}

	public IDfId getIdPackageBPM() {
		return idPackageBPM;
	}

	private void setIdPackageBPM(IDfId idPackageBPM) {
		this.idPackageBPM = idPackageBPM;
	}

	public void loadDocuments(IDfCollection pkgColl) throws Exception {
		try {
			if (this.getDebug())
				this.getLogger().write("recorre los package");
			while (pkgColl.next()) {

				IDfId packageId = pkgColl.getId("r_object_id");
				String docId = pkgColl.getString("r_component_id");
				String packageName = pkgColl.getString("r_package_name");
				int docCount = pkgColl.getValueCount("r_component_id");

				for (int i = 0; i <= (docCount - 1); i++) {
					IDfId docIdObj = pkgColl
							.getRepeatingId("r_component_id", i);

					if (docIdObj != null) {

						IDfId sysobjID = new DfId(docId);
						IDfSysObject dmc = (IDfSysObject) this.getSession()
								.getObject(sysobjID);
						if (packageName.equals(this.getBPM().getPackageBPM())) {
							if (this.getDebug())
								this.getLogger()
										.write("asigna el doc con parametros");
							this.setParametros(dmc);
							this.setIdPackageBPM(packageId);
						} else if (packageName.equals(this.getBPM()
								.getPackageAdjunto())) {
							if (this.getDebug())
								this.getLogger()
										.write("asigna el doc adjunto");
							this.setDocumento(dmc);
							this.setIdPackageDoc(packageId);
						}
					}
				}
			}
			pkgColl.close();
		} catch (Exception e) {
			throw e;
		}
	}
}