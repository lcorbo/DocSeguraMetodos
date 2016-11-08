package ar.com.lpa.documentum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.Key;
import java.util.Date;
import java.util.Properties;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import sun.misc.BASE64Decoder;

public class LPAServiceConfigurator
{
  protected boolean debug;
  protected String log;
  protected String hostDB;
  protected String DB;
  protected String userDB;
  protected String passDB;
  protected String instanceDB;
  protected String passDoc;
  protected String hostSMTP;
  protected String from;
  protected String docbase;
  protected String pathBPM;
  protected String bookmark;
  protected String clase;
  protected String usuarioEspera;
  protected int diasNotificacionesTareas;

  public LPAServiceConfigurator()
  {
    try
    {
      Properties props = new Properties();
      File clase = new File(LPAServiceConfigurator.class.getResource(
        "LPAServiceConfigurator.class").toString());
      File carpeta = clase.getParentFile();
      String path = carpeta.getPath();
      path = path.substring(6);
      path = path.replace("%20", " ");
      FileInputStream Prop = new FileInputStream(path + File.separator + 
        "BagoJMS.properties");
      props.load(Prop);
      setDebug(props.getProperty("debug"));
      setLog(props.getProperty("log"));
      setHostDB(props.getProperty("hostDB"));
      setDB(props.getProperty("DB"));
      setUserDB(props.getProperty("userDB"));
      setPassDB(desencriptar(props.getProperty("passDB")));
      setInstanceDB(props.getProperty("instanceDB"));
      setPassDoc(desencriptar(props.getProperty("passDoc")));
      setHostSMTP(props.getProperty("hostSMTP"));
      setFrom(props.getProperty("from"));
      setDocbase(props.getProperty("docbase"));
      setPathBPM(props.getProperty("pathBPM"));
      setBookmark(props.getProperty("bookmark"));
      setClase(props.getProperty("clase"));
      setUsuarioEspera(props.getProperty("usuarioEspera"));
      setDiasNotificacionesTareas(props.getProperty("diasNotificacionesTareas"));
      Prop.close();
    } catch (Exception e) {
      log("LPAServiceConfigurator - readProperties: " + 
        e.getMessage());
    }
  }

  public String getPassDoc() {
    return this.passDoc;
  }

  private void setPassDoc(String passDoc) {
    this.passDoc = passDoc;
  }

  public String getHostDB() {
    return this.hostDB;
  }

  private void setHostDB(String hostDB) {
    this.hostDB = hostDB;
  }

  public String getDB() {
    return this.DB;
  }

  private void setDB(String dB) {
    this.DB = dB;
  }

  public String getUserDB() {
    return this.userDB;
  }

  private void setUserDB(String userDB) {
    this.userDB = userDB;
  }

  public String getPassDB() {
    return this.passDB;
  }

  private void setPassDB(String passDB) {
    this.passDB = passDB;
  }

  public String getInstanceDB() {
    return this.instanceDB;
  }

  private void setInstanceDB(String instanceDB) {
    this.instanceDB = instanceDB;
  }

  public String getDocbase() {
    return this.docbase;
  }

  private void setDebug(String debug) {
    if ((debug.equals("true")) || (debug.equals("TRUE")))
      this.debug = true;
    else
      this.debug = false;
  }

  private void setLog(String Log)
  {
    this.log = Log;
  }

  public boolean getDebug()
  {
    return this.debug;
  }

  public String getLog() {
    return this.log;
  }

  public String getHostSMTP() {
    return this.hostSMTP;
  }

  private void setHostSMTP(String hostSMTP) {
    this.hostSMTP = hostSMTP;
  }

  public String getFrom() {
    return this.from;
  }

  private void setFrom(String from) {
    this.from = from;
  }

  private void setDocbase(String docbase) {
    this.docbase = docbase;
  }

  public String getPathBPM() {
    return this.pathBPM;
  }

  private void setPathBPM(String pathBPM) {
    this.pathBPM = pathBPM;
  }

  public String getBookmark() {
    return this.bookmark;
  }

  private void setBookmark(String bookmark) {
    this.bookmark = bookmark;
  }

  public String getClase() {
    return this.clase;
  }

  private void setClase(String clase) {
    this.clase = clase;
  }

  public String getUsuarioEspera() {
    return this.usuarioEspera;
  }

  private void setUsuarioEspera(String usuarioEspera) {
    this.usuarioEspera = usuarioEspera;
  }

  public int getDiasNotificacionesTareas() {
    return this.diasNotificacionesTareas;
  }

  private void setDiasNotificacionesTareas(String diasNotificacionesTareas) {
    this.diasNotificacionesTareas = Integer.parseInt(diasNotificacionesTareas);
  }

  private static String desencriptar(String str)
  {
    byte[] secret = { 1, 2, 3, 4, 5, 6, 7, 8 };
    try {
      Key key = null;
      SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
      key = skf.generateSecret(new DESKeySpec(secret));
      SecretKey clave = (SecretKey)key;
      String Algoritmo = "DES";

      byte[] dec = new BASE64Decoder().decodeBuffer(str);
      Cipher descifrar = Cipher.getInstance(Algoritmo);
      descifrar.init(2, clave);
      byte[] cadenaByte = descifrar.doFinal(dec);

      return new String(cadenaByte, "UTF8");
    } catch (BadPaddingException localBadPaddingException) {
    } catch (IllegalBlockSizeException localIllegalBlockSizeException) {
    } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
    } catch (Exception localException) {
    }
    return null;
  }

  private void log(String message) {
    try {
      File clase = new File(LPAServiceConfigurator.class.getResource(
        "LPAServiceConfigurator.class").toString());
      File carpeta = clase.getParentFile();
      String path = carpeta.getPath();
      path = path.substring(6);
      path = path.replace("%20", " ");
      FileWriter file = new FileWriter(path + File.separator + 
        "BagoJMS.log", true);
      BufferedWriter w = new BufferedWriter(file);
      w.write(new Date() + ": ");
      w.write(message);
      w.newLine();
      w.close();
    }
    catch (Exception localException)
    {
    }
  }
}