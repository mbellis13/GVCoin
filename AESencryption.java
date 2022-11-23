import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;


import javax.crypto.Cipher;

import javax.crypto.spec.SecretKeySpec;

public class AESencryption 
{
	private SecretKeySpec secretKey;
	private byte[] key;
	
	public AESencryption(String mykey)
	{
		MessageDigest sha = null;
		try
		{
			key = mykey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
			key = sha.digest(key);
			key = Arrays.copyOf(key, 16);
			secretKey = new SecretKeySpec(key,"AES");
		}
		catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String encrypt(String strToEncrypt)
	{
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
			
		
		} catch (Exception e) {
			System.out.println("AES encrytion error");
			e.printStackTrace();
		}
		return null;
	}
	
	public String decrypt(String strToDecrypt)
	{
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		}catch(Exception e) {
			System.out.println("AES decrypt error");
			e.printStackTrace();
		}
		return null;
	}
}
