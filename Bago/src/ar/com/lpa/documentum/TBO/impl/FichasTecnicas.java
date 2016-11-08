/*
 * Created on Mar 10, 2003
 *
 * Documentum Developer Program 2003
 * 
 */
package ar.com.lpa.documentum.TBO.impl;

import ar.com.lpa.documentum.TBO.IFichasTecnicas;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;

/**
 * The class overrides the checkin method. Within this method, it starts
 * a workflow and attaches the document to the workflow.
 * 
 * <br><br>
 * Starting the Workflow:
 * <br>
 * The current implementation simply picks up the first start activity and
 * and the first INPUT port on the start activity and attaches a document to
 * the package on that port. However, it is possible to set these values using
 * the configuration file. The initConfig() file can be modified to add this
 * customization.
 * 
 * 
 * 
 * @author Aashish Patil (aashish.patil@documentum.com)
 *
 * 
 */
public class FichasTecnicas extends DfDocument
	implements IFichasTecnicas
{

	/**
	 * The activity to which to attach the document
	 */
	private IDfId m_activityId = null;

	/**
	 * Id of the workflow template (dm_process) that will be started when the 
     * object is checked into the docbase.
	 */
	private IDfId m_processId = null;
   
    /**
     * The name of the package to which to add the object being checked in. 
     */
    private String m_strPackageName = null;

	/**
	 * Name of the input port that should accept the document
	 */
	private String m_strPortName = null;

	/**
	 * A Descriptive string describing the vendor. Can put copyright information
	 * too. 
	 */
	private String m_strVendorString = "Documentum Developer Program";

	/**
	 * The business object verion. A string of form 'majorVersionNumber.minorVersionNumber'
	 */
	private String m_strVersion = "1.0";

	
	/* 
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @return com.documentum.fc.common.IDfId
	 * @throws com.documentum.fc.common.DfException
	 * 
	 * @see com.documentum.fc.client.IDfSysObject#checkinEx(boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public IDfId checkinEx(
		boolean arg0,
		String arg1,
		String arg2,
		String arg3,
		String arg4,
		String arg5)
		throws DfException
	{
		IDfId newId = super.checkinEx(arg0, arg1, arg2, arg3, arg4, arg5);
		startWorkflow("WF_Prueba",newId);
		return newId;
	}

	/**
	 * Returns the object id of the dm_activity object to which the checked in document will be attached
	 * @return IDfId    object id of the dm_activity object to which the checked in document will be attached
	 *
	 */
	public IDfId getActivityId()
	{
		return m_activityId;
	}
    
    /**
     * gets the name of the package to which the object being checked in is added.
     * 
     * @return String name of the package to which the object being checked in is added
     *
     */
    public String getPackageName()
    {
       return m_strPackageName;
    }

	/**
	 * Gets the name of the port on which the document will be sent.
    * 
	 * @return String   Port name on which the document will be sent
	 *
	 */
	public String getPortName()
	{
		return m_strPortName;
	}

	/**
	 * getter method for the process id.
	 * @return IDfId    object id of the dm_process object that defines the workflow template
	 *
	 */
	public IDfId getProcessId()
	{
        return m_processId;
	}

	/* Returns a string describing the vendor. 
	 * @return java.lang.String
	 * 
	 * @see com.documentum.fc.client.IDfBusinessObject#getVendorString()
	 */
	public String getVendorString()
	{
		return m_strVendorString;

	}

	/* 
	 * Returns the version of the business object. 
	 * 
	 * @return java.lang.String    A string of form 'majorVersionNumber.minorVersionNumber'
	 * 
	 * @see com.documentum.fc.client.IDfBusinessObject#getVersion()
	 */
	public String getVersion()
	{
		return m_strVersion;
	}


    /**
	 * Reads the name of the workflow to start from the XML file. 
     * The workflow is indexed by the content type. Thus, each content
     * type has a specified workflow with it. The list of workflows indexed by
     * contentType is further within the scope called 'contentType'
	 */
       
	
	/* 
	 * @param arg0
	 * @return boolean
	 * 
	 * @see com.documentum.fc.client.IDfBusinessObject#isCompatible(java.lang.String)
	 */
	public boolean isCompatible(String arg0)
	{
		if (arg0.equals(getVersion()))
		{
			return true;
		}

		return false;

	}

	/**
	 * Sets the object id of the dm_activity object to which the checked in document will be attached
	 * @param activityId    object id of the dm_activity object to which the checked in document will be attached
	 *
	 */
	public void setActivityId(IDfId activityId)
	{
		m_activityId = activityId;
	}


    /**
     * sets the package name to which to add the object being checked in.
     * 
     * @param packageName package name 
     */
    public void setPackageName(String packageName)
    {
       m_strPackageName = packageName;
    }

	/**
	 * Name of the port to on which the document will be sent.
    * 
	 * @param strPort   Port name on which the document will be sent.
	 *
	 */
	public void setPortName(String strPort)
	{
		m_strPortName = strPort;
	}

	/**
	 * Sets the Object id of the dm_process object that defines the workflow template
	 * @param processId Object id of the dm_process object that defines the workflow template
	 *
	 */
	public void setProcessId(IDfId processId)
	{
		m_processId = processId;
	}

	/**
	 * Starts the workflow and attaches the document specified by <code>documentId</code>
	 * to the workflow. For simplification purposes, the first start activity is 
     * chosen and first input port of that activity is chosen to attach the 
     * document.
	 * 
	 * @param documentId    Object id of the document that will be attached to the workflow after it is started.
	 *
	 */
	public void startWorkflow(String wfName, IDfId id) throws DfException {
		IDfSessionManager sessionMgr = null;
		IDfClientX clientx = new DfClientX();
		IDfLoginInfo li = clientx.getLoginInfo();
		li.setDomain("");
		li.setUser("Administrator");
		li.setPassword("Lpa23291");
		IDfClient client = clientx.getLocalClient();
		sessionMgr = client.newSessionManager();
		IDfSession session = sessionMgr.getSession("DocBase");
		IDfId wfId = (IDfId) session
		.getIdByQualification("dm_process where object_name = '"
				+ wfName + "'");

			IDfWorkflowBuilder workflowBuilder = session.newWorkflowBuilder(wfId);
		 workflowBuilder.getWorkflow().setObjectName(wfName);
		 if ((workflowBuilder.getStartStatus() != 0) || (!(workflowBuilder.isRunnable()))) {
		  DfLogger.warn(this, "startWorkflow - workflow '" + wfName + "' is not runnable or StartStauts=0!", null, null);
		  throw new DfException("cannot start Workflow!");
		 }
		 workflowBuilder.runWorkflow();

		 // Adding attachments:
		 IDfList attachIds = new DfList();
		 attachIds.appendId(id);

		 IDfList startActivities = workflowBuilder.getStartActivityIds();
		 int packageIndex = 0;
		 for (int i = 0; i < startActivities.getCount(); i++) {
		  IDfActivity activity = (IDfActivity) session.getObject(startActivities.getId(i));
		  workflowBuilder.addPackage(activity.getObjectName(), activity.getPortName(packageIndex),
		    activity.getPackageName(packageIndex), activity.getPackageType(packageIndex), null, false, attachIds);
		 }
		}

	/* 
	 * @param arg0
	 * @return boolean
	 * 
	 * @see com.documentum.fc.client.IDfBusinessObject#supportsFeature(java.lang.String)
	 */
	public boolean supportsFeature(String arg0)
	{
		return false;
	}

}
