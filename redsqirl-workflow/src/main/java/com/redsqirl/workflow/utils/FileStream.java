package com.redsqirl.workflow.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.io.IOUtils;

public class FileStream {

	//Arbitrarily selected 8-byte salt sequence:
	private static final byte[] salt = {
		(byte) 0x53, (byte) 0x7a, (byte) 0x9f, (byte) 0xc7,
		(byte) 0x5b, (byte) 0xd7, (byte) 0x25, (byte) 0x1d 
	};
	private static final String passphrase = "The quick brown fox jumped over the lazy brown dog";
	private static final int iterations = 2000;
	private static final int keyLength = 256;

	public static void encryptFile(File in,File out) throws Exception{
		//Write the encrypted data to a new file:
        FileOutputStream outStream = new FileOutputStream(out);
        outStream.write(encryptFile(in));
        outStream.close();
	}
	
	public static void decryptFile(File in,File out) throws Exception{
		//Write the encrypted data to a new file:
        FileOutputStream outStream = new FileOutputStream(out);
        outStream.write(decryptFile(in));
        outStream.close();
	}

	public static byte[] encryptFile(File in) throws Exception{
		int blockSize = 8;
		//Figure out how many bytes are padded
		int paddedCount = blockSize - ((int)in.length()  % blockSize );
		
		//Figure out full size including padding
		int padded = (int)in.length() + paddedCount;

		byte[] decData = new byte[padded];
		FileInputStream inStream = new FileInputStream(in);
		inStream.read(decData);
		inStream.close();

		//Write out padding bytes as per PKCS5 algorithm
		for( int i = (int)in.length(); i < padded; ++i ) {
			decData[i] = (byte)paddedCount;
		}
		
		return encrypt(decData);
	}

	public static byte[] decryptFile(File in) throws Exception{
        //Decrypt the file data:
		byte[] encData;

        //Read in the file:
        FileInputStream inStream = new FileInputStream(in);
        encData = new byte[(int)in.length()];
        inStream.read(encData);
        inStream.close();
        
        byte[] decData = decrypt(encData);

        //Figure out how much padding to remove
        int padCount = (int)decData[decData.length - 1];

        //Naive check, will fail if plaintext file actually contained
        //this at the end
        //For robust check, check that padCount bytes at the end have same value
        if( padCount >= 1 && padCount <= 8 ) {
            decData = Arrays.copyOfRange( decData , 0, decData.length - padCount);
        }
        
        //Write the decrypted data to a new file:
		return decData;
	}

	private static byte[] encrypt(byte[] plaintext) throws Exception {
		SecretKey key = generateKey();
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
		return cipher.doFinal(plaintext);
	}

	private static byte[] decrypt(byte[] ciphertext) throws Exception {
		SecretKey key = generateKey();
		PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);
		Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
		cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
		return cipher.doFinal(ciphertext);
	}

	private static SecretKey generateKey() throws Exception {
		PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray(), salt, iterations, keyLength);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		return keyFactory.generateSecret(keySpec);
	}

}
