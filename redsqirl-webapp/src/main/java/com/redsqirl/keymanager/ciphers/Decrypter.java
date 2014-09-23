package com.redsqirl.keymanager.ciphers;

import org.apache.log4j.Logger;

public class Decrypter extends KeyCipher {
	private Logger logger = Logger.getLogger(getClass());
	public String[] decrypt(Types keyType , String key){
		
		cipher = keyType.name().equalsIgnoreCase(Types.redsqirl.name()) ? redSqirlCypher : nutCrackerCypher;
		
		String[]ans = new String[]{};
		int[] indent = new int[4];
		for(int i = 0; i< 4;++i){
			indent[i] = transformBack(key.charAt(i));
		}
		String app = indentStr(key.substring(4,7),indent[3]-indent[0]-indent[2]);
		String app2 = indentStr(key.substring(7,10),indent[0]-indent[2]-indent[3]);
		
		String mac = indentStr(key.substring(10,18),indent[2]-indent[0]-indent[3]);
		String userNbObf = indentStr(key.substring(18,19),indent[1]-indent[0]-indent[2])+
							indentStr(key.substring(19,20),indent[1]-indent[0]-indent[3]);
		Integer userNb = Integer.valueOf(cipher.indexOf(userNbObf.charAt(0))*62+ cipher.indexOf(userNbObf.charAt(1)));
		logger.info(app+app2+" , "+mac+" , "+userNb);
		return ans;
		
	}
	
	public int transformBack(char t){
		return cipher.indexOf(t);
	}

}
