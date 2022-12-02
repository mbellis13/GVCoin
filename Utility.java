import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility Methods
 * @author mellis
 *
 */
public class Utility 
{
	
	private static Base64.Encoder encoder = Base64.getEncoder();
	//private static Base64.Decoder decoder = Base64.getDecoder();
	public static double mining_reward = 50;
	
	
	/**
	 * convert byte array to hex string
	 * @param hash
	 * @return
	 */
	public static String toHexString(byte[] hash)
	{
		BigInteger number = new BigInteger(1,hash);
		StringBuilder hexString = new StringBuilder(number.toString(16));
		while(hexString.length() < 32)
			hexString.insert(0, '0');
		return hexString.toString();
	}
	
	/**
	 * calculates SHA hash as byte array
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] getSHA(String input) throws NoSuchAlgorithmException
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * calculates hash of string and returns as hex string
	 * @param s
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String hash(String s) throws NoSuchAlgorithmException
	{
		return Utility.toHexString(Utility.getSHA(s));
	}
	
	
	/**
	 * converts hex string to byte array
	 * @param s
	 * @return
	 */
	public static byte[] toByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len/2];
		for(int i = 0; i < len; i+=2)
		{
			data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1),16));
		}
		return data;
	}
	
	
	/**
	 * verifies signature is valid
	 * @param key
	 * @param data
	 * @param signedData
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public static boolean verifySignature(PublicKey key,String data, String signedData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException
	{
		Signature signer = Signature.getInstance("SHA1WithECDSA");
		signer.initVerify(key);
		signer.update(data.getBytes("UTF8"));
		return(signer.verify(toByteArray(signedData)));	
	}
	
	/**
	 * returns PublicKey object from string representation
	 * @param encodedKey
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static PublicKey retrievePublicKey(String encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException
	{	
		//Base64.Encoder encoder = Base64.getEncoder();
		Base64.Decoder decoder = Base64.getDecoder();
		KeyFactory factory = KeyFactory.getInstance("EC");
		
		
		//System.out.println(factory.generatePublic(new X509EncodedKeySpec(decoder.decode(encodedKey))));
		PublicKey key = factory.generatePublic(new X509EncodedKeySpec(decoder.decode(encodedKey)));
		//System.out.println(4);
		return key;
	}
	
	
	/**
	 * returns PrivateKey object from string representation
	 * @param encodedKey
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 */
	public static PrivateKey retrievePrivateKey(String encodedKey) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException
	{	
		//Base64.Encoder encoder = Base64.getEncoder();
		Base64.Decoder decoder = Base64.getDecoder();
		KeyFactory factory = KeyFactory.getInstance("EC");
		
		
		//System.out.println(factory.generatePublic(new X509EncodedKeySpec(decoder.decode(encodedKey))));
		PrivateKey key = factory.generatePrivate(new PKCS8EncodedKeySpec(decoder.decode(encodedKey)));
		//System.out.println(4);
		return key;
	}
	
	/**
	 * signs given string with given key
	 * @param dataHash
	 * @param privateKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws UnsupportedEncodingException
	 */
	public static String sign(String dataHash, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException
	{
		Signature signer = Signature.getInstance("SHA1WithECDSA");
		signer.initSign(privateKey);
		signer.update(dataHash.getBytes("UTF8"));
		return Utility.toHexString(signer.sign());
	}
	
	
	/**
	 * converts PublicKey object to String representation
	 * @param key
	 * @return
	 */
	public static String publicKeyToAddress(PublicKey key)
	{
		return encoder.encodeToString(key.getEncoded());
	}
	
	/**
	 * converts PrivateKey object to String representation
	 * @param key
	 * @return
	 */
	public static String privateKeyToAddress(PrivateKey key)
	{
		return encoder.encodeToString(key.getEncoded());
	}
}
