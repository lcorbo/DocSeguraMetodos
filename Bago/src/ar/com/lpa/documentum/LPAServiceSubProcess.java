package ar.com.lpa.documentum;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQueueItem;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfId;

public class LPAServiceSubProcess {

	protected IDfSession session = null;
	protected LPAServiceLogger logger = null;
	protected boolean debug = false;

	private LPAServiceSubProcess() {

	}

	public LPAServiceSubProcess(IDfSession session, LPAServiceLogger logger,
			boolean debug) {
		this.setSession(session);
		this.setLogger(logger);
		this.setDebug(debug);
	}

	private void setSession(IDfSession session) {
		this.session = session;
	}

	private IDfSession getSession() {
		return this.session;
	}

	private void setLogger(LPAServiceLogger logger) {
		this.logger = logger;
	}

	private LPAServiceLogger getLogger() {
		return this.logger;
	}

	private void setDebug(boolean debug) {
		this.debug = debug;
	}

	private boolean getDebug() {
		return this.debug;
	}

	public void CompleteTask(String user, String workflowID, String paso)
			throws Exception {

		IDfCollection tasks = null;

		try {
			if (this.getDebug()) {
				this.getLogger().write(
						"user: " + user);
				this.getLogger().write(
						"workflowID: "
								+ workflowID);
				this.getLogger().write(
						"paso: " + paso);
			}
			tasks = this.getSession().getTasks(user, IDfSession.DF_TASKS, null,
					null);

			while (tasks.next()) {

				try {
					IDfQueueItem queueItem = (IDfQueueItem) this
							.getSession()
							.getObject(new DfId(tasks.getString("r_object_id")));
					if (queueItem.getTaskName().equals(paso)) {
						if (this.getDebug())
							this.getLogger()
									.write("corresponde al paso");
						IDfWorkitem workitem = queueItem.getWorkitem();
						if (this.getDebug())
							this.getLogger().write(
									"workflowID: "
											+ workitem.getWorkflowId()
													.toString());
						if (workitem.getWorkflowId().toString()
								.equals(workflowID)) {
							if (this.getDebug())
								this.getLogger()
										.write("corresponde al workflow padre");
							if (workitem.getRuntimeState() == 0)
								workitem.acquire();
							workitem.complete();
							if (this.getDebug())
								this.getLogger()
										.write("completa la tarea");
						}
					}
				} catch (Exception e) {
					this.getLogger().writeTrace(e);
				}

			}

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (tasks != null)
					tasks.close();
			} catch (Exception e) {
				this.getLogger().writeTrace(e);
			}

		}
	}

}