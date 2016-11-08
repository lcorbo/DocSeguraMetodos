package ar.com.lpa.documentum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.*;
import sun.misc.*;

public class LPAProperties {
	
	private Properties props;
	 private static final byte[] keyValue = 
	 new byte[] {1, 2, 3, 4, 5, 6, 7, 8 };
	 private static final String ALGO = "DES";
	
	
	public LPAProperties() throws IOException{
		props = new Properties();
	      File clase = new File("c:\\LPA.properties");
	      File carpeta = clase.getParentFile();
	      FileInputStream Prop = new FileInputStream(clase);
	      props.load(Prop);
	}
	
	public String get(String key)
	{
		return props.getProperty(key);
	}
	
	public String getEncrypted(String key) throws Exception
	{
		return desencriptar(props.getProperty(key));
	}
	
	private static String encriptar(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    private static String desencriptar(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String encriptado = "sigar2011";
        try {
        	System.out.println(encriptar(encriptado));
        } catch (Exception ex1)
        {
            System.out.print(ex1.toString());
        }
   }


	}


