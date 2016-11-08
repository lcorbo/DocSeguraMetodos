/*
 * Created on Mar 10, 2003
 *
 * @author	Aashish Patil (aashish.patil@documentum.com)
 */
package ar.com.lpa.documentum.TBO;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;


/**
 * Interface methods allow wokflow configuration. These methods, if used, override
 * the values specified in the configuration file.
 *  
 * @author Aashish Patil (aashish.patil@documentum.com)
 *
 * 
 */
public interface IFichasTecnicas extends IDfBusinessObject
{
	
    /**
     * Sets the Object id of the dm_process object that defines the workflow template
     * @param processId Object id of the dm_process object that defines the workflow template
     *
     */
    public void setProcessId(IDfId processId);
    
    /**
     * Sets the object id of the dm_activity object to which the checked in document will be attached
     * @param activityId    object id of the dm_activity object to which the checked in document will be attached
     *
     */
    public void setActivityId(IDfId activityId);
    
    
    /**
     * Name of the port to on which the document will be sent.
     * @param strPort   Port name on which the document will be sent.
     *
     */
    public void setPortName(String portName);
    
    /**
     * Set the name of the package that will contain the object being checked in.
     * 
     * @param packageName  The name of the package
     */
    public void setPackageName(String packageName);
}
