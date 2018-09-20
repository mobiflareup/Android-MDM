package com.mobiocean.util;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
*Test HandSet Model     = s01
*ADT Pakage Version     = 21.0.1.201212060302
*Eclipse Platform       = 4.2.1.v20120814
*Date					= April 29,2013
*Functionality			= Encrytion and Decryption
*						  				
 *Android version		= 2.3.6 [Gingerbread (API level 10)]
 */
public class StringCryptor
{
	protected static String IV = "AAAAAAAAAAAAAAAA";
	public static String encryptionKey = CallHelper.Ds.structPC.stEDPassword;
// For Encryption
	public static String encrypt(String plainText, String encryptionKey) throws Exception 
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	    byte[] encVal = cipher.doFinal(plainText.getBytes());
	    String encryptedValue = Base64.encodeToString( encVal, Base64.DEFAULT );
	    return encryptedValue;
	}
//For Decryption 
	public static String decrypt(String cipherText, String encryptionKey) throws Exception
	{
		if(cipherText!=null) {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
			byte[] decordedValue = Base64.decode(cipherText, Base64.DEFAULT);
			byte[] decValue = cipher.doFinal(decordedValue);
			return new String(decValue);
		}
		return null;
	}
}
