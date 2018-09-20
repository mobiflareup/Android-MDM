package com.browser.gingerbox;


import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class StringCryptor
{
	static String IV = "AAAAAAAAAAAAAAAA";

	public static String encryptionKey = "0123456789abcdef";
	
	 
	public static String encrypt(String plainText, String encryptionKey) throws Exception 
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    byte[] encVal = cipher.doFinal(plainText.getBytes());
	    String encryptedValue = Base64.encodeToString( encVal, Base64.DEFAULT );
	    return encryptedValue;
	}
	 
	public static String decrypt(String cipherText, String encryptionKey) throws Exception
	{
		cipherText = java.net.URLDecoder.decode(cipherText, "UTF-8");
	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
	    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    byte[] decordedValue = Base64.decode( cipherText, Base64.DEFAULT );
	    byte[] decValue = cipher.doFinal(decordedValue);
	    String decryptedValue = new String(decValue);
	    return decryptedValue;
	}
}
