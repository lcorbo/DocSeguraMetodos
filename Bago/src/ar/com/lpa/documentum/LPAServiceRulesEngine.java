package ar.com.lpa.documentum;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import org.jruby.embed.ScriptingContainer;

public class LPAServiceRulesEngine
{
  private ScriptingContainer container = null;

  private void setContainer(ScriptingContainer container) {
    this.container = container;
  }

  private ScriptingContainer getContainer() {
    return this.container;
  }

  public LPAServiceRulesEngine() {
    setContainer(new ScriptingContainer());
    File clase = new File(LPAServiceRulesEngine.class.getResource(
      "LPAServiceRulesEngine.class").toString());
    File carpeta = clase.getParentFile();
    String path = carpeta.getPath();
    path = path.substring(6);
    path = path.replace("%20", " ");
    getContainer().setLoadPaths(Arrays.asList(new String[] { path }));
  }

  public int getAclPermit(String grupo) {
    getContainer().runScriptlet("require 'acl_permit'");
    Object greeter = getContainer().runScriptlet("AclPermit.new");
    String tipoSeguridad = (String)getContainer().callMethod(greeter, 
      "acl_permit", grupo, String.class);
    return Integer.parseInt(tipoSeguridad);
  }

  public String getAclPermitEx(String grupo) {
    getContainer().runScriptlet("require 'acl_permit'");
    Object greeter = getContainer().runScriptlet("AclPermit.new");
    String tipoSeguridadEx = (String)getContainer().callMethod(greeter, 
      "acl_permit", grupo, String.class);
    if ((tipoSeguridadEx.equalsIgnoreCase("NONE")) || 
      (tipoSeguridadEx.equalsIgnoreCase("1"))) {
      return null;
    }
    return tipoSeguridadEx;
  }
  
  public String getEventLabel(String evento) {
		this.getContainer().runScriptlet("require 'dcmt_history'");
		Object greeter = this.getContainer().runScriptlet("DcmtHistory.new");
		String eventLabel = this.getContainer().callMethod(greeter,
				"label_event", evento, String.class);
		return eventLabel;
	}

}