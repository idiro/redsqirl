package com.redsqirl.keymanager.ciphers;

import java.util.Random;

public class Encrypter extends KeyCipher{
	
	public String encryptKey(Types type , String appName ,int nbUser){
		Random rnd = new Random();
		int[] indent = new int[4];
		char[] indCh = new char[4];
		boolean ok = false;
		cipher = type.toString().equals(Types.redsqirl) ? redSqirlCypher : nutCrackerCypher;
		String app = type.name();
		String mac = getMACAddress();
		
		int actualnbUser = getNumberOfusers();
		if(actualnbUser > nbUser){
			System.exit(1);
		}
		
		while(!ok){
			ok = true;
			for(int i = 0; i< 4;++i){
				indent[i] = rnd.nextInt(cipher.length()-1)+1;
				indCh[i] = transform(indent[i]);
				ok &= indent[i] % 62 != 0;
			}
			if(ok){
				ok = indent[0]+indent[2]-indent[3] % 62 != 0 && 
						indent[2]+indent[3]-indent[0] % 62 != 0 &&
						indent[0]+indent[3]-indent[2] % 62 != 0 &&
						indent[0]+indent[2]-indent[1] % 62 != 0 &&
						indent[0]+indent[3]-indent[1] % 62 != 0;
			}
		}
		//System.out.println(indent[0]+" , "+indent[1]+" , "+indent[2]+" , "+indent[3]);
		String trAppStr = indentStr(app,indent[0]+indent[2]-indent[3]);
		String trAppStr2 = indentStr(app,indent[2]+indent[3]-indent[0]);
		String trMac = indentStr(mac,indent[0]+indent[3]-indent[2]);
		String trUserNb = new String();
		trUserNb += indentStr(""+cipher.charAt((int)(nbUser/62)),indent[0]+indent[2]-indent[1]);
		trUserNb += indentStr(""+cipher.charAt(nbUser - ((int)(nbUser/62))*62),indent[0]+indent[3]-indent[1]);
		
		return  new String (indCh)+
				trAppStr.substring(0,3)+trAppStr2.substring(trAppStr.length()-3)+
				trMac.substring(trMac.length()-8)+
				trUserNb;
	}
	
	

}
