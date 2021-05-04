package com.azure.keyvault.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.RandomSaltGenerator;

/**
 * This class contains utility to encrypt yaml/properties values to pass encrypted
 * values for Jasypt 
 * @author Sandeep kumar
 *
 */
public class EncryptionDecryptionUtils {
	
	
	public static void main(String...args) {
		String key = "myapp123";
		setKeyForJasyptEncryptionDecryption(key, "PBEWithMD5AndDES", 1000, "SunJCE", "base64");
		
		String secretValutToEncrypt = "p@ssw0rd"; //Example value to showcase, use own values to encrypt
		System.out.println(encryptForJasypt(secretValutToEncrypt));
	}
	
	
	private static final Logger logger = Logger.getLogger(EncryptionDecryptionUtils.class.getName());
	
    public static void setKeyForJasyptEncryptionDecryption(String key, String algorithm, int keyObtentionIterations, String providerName, String stringOutputType) {
    	standardPBEStringEncryptor = new StandardPBEStringEncryptor();
    	standardPBEStringEncryptor.setAlgorithm(algorithm);
    	standardPBEStringEncryptor.setPassword(key);
    	standardPBEStringEncryptor.setKeyObtentionIterations(keyObtentionIterations);
    	standardPBEStringEncryptor.setProviderName(providerName);
    	standardPBEStringEncryptor.setSaltGenerator(new RandomSaltGenerator());
    	standardPBEStringEncryptor.setStringOutputType(stringOutputType);
    }
    
    public static String encryptForJasypt(String input) {
    	return standardPBEStringEncryptor.encrypt(input);
    }

    public static String decryptForJasypt(String input) {
    	return standardPBEStringEncryptor.decrypt(input);
    }
	
    public String encrypt(final String message) {
    	try {
			return encoder.encodeToString(encryptor.doFinal(message.getBytes("UTF-8")));
		} catch (IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "Exception while encryption", e);
		}
    	return null;
    }

    public String decrypt(final String encryptedMessage) {
    	try {
			return new String(decryptor.doFinal(decoder.decode(encryptedMessage)));
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			logger.log(Level.SEVERE, "Exception while decryption", e);
		}
    	return null;
    }
    
    private static Cipher encryptor = null;
    private static Cipher decryptor = null;
    private static Base64.Decoder decoder = null;
    private static Base64.Encoder encoder = null;
    private static StandardPBEStringEncryptor standardPBEStringEncryptor;

    public static void setKeyAES(String myKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException, NoSuchPaddingException {
    	decoder = Base64.getDecoder();
    	encoder = Base64.getEncoder();
    	
		byte[] key = myKey.getBytes("UTF-8");

		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		key = sha.digest(key);
		key = Arrays.copyOf(key, 32);
		
		SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

		encryptor = Cipher.getInstance("AES/ECB/PKCS5Padding");
		encryptor.init(Cipher.ENCRYPT_MODE, secretKey);
		
		decryptor = Cipher.getInstance("AES/ECB/PKCS5Padding");
		decryptor.init(Cipher.DECRYPT_MODE, secretKey);
	}
}
