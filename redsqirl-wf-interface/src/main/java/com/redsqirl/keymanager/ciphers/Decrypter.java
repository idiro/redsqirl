package com.redsqirl.keymanager.ciphers;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.utils.PMLanguageManager;

public class Decrypter extends KeyCipher {
	// Software Keys
	private String appName, appName2, macAddr, nbCluster, dateStr;

	public static String encrypt = "encrypt", decrypt = "decrypt", software = "software", module = "module", name = "title", license = "license",
		modLicense = "modLicense", version = "version", mac = "mac", clusterNb = "clusterNb", userName = "user", date = "date";

	private Map<String, String> ans;
	
	public int numberCluster;

	// Module Keys
	private String key, key2, module1, module2, user, user2;
	private int clusterNbInt, dateInt;

	private static Logger logger = Logger.getLogger(Decrypter.class);

	public void decrypt(String key) {
		ans = new HashMap<String, String>();
		int[] indent = new int[4];
		int[] vals = null;
		while (vals == null) {
			try {
				for (int i = 0; i < 4; ++i) {
					indent[i] = transformBack(key.charAt(i));
				}
				vals = intCombinations(indent, 5, 3);
			} catch (Exception e) {
				vals = null;
				e.printStackTrace();
			}
		}

		appName = indentStr(cipher, key.substring(4, 7), -vals[0]);
		appName2 = indentStr(cipher, key.substring(7, 10), -vals[1]);

		macAddr = indentStr(cipher, key.substring(10, 18), -vals[2]);
		
		nbCluster = indentStr(cipher, key.substring(18, 19), -vals[3])	+ indentStr(cipher, key.substring(19, 20), -vals[4]);
		clusterNbInt = Integer.valueOf(cipher.indexOf(nbCluster.charAt(0)) * 62 + cipher.indexOf(nbCluster.charAt(1))).intValue();

		dateStr = indentStr(cipher, key.substring(20, 21), -vals[2]) + indentStr(cipher, key.substring(21, 22), -vals[3]);
		dateInt = Integer.valueOf(cipher.indexOf(dateStr.charAt(0)) * 62 + cipher.indexOf(dateStr.charAt(1))).intValue();
		 
		
		ans.put(name + "1", appName);
		ans.put(name + "2", appName2);
		ans.put(mac, macAddr);
		ans.put(clusterNb, String.valueOf(clusterNbInt));
		ans.put(date, String.valueOf(dateInt));
		
		setNumberCluster(clusterNbInt);
	}

	public int transformBack(char t) {
		return cipher.indexOf(t);
	}

	public void decrypt_key_module(String keyModule) {

		//logger.info("keyModule " + keyModule);

		if(keyModule.length() != 24){
			return;
		}
		ans = new HashMap<String, String>();

		int[] indent = new int[4];

		int[] vals = null;
		while (vals == null) {
			try {
				for (int i = 0; i < 4; ++i) {
					indent[i] = transformBack(keyModule.charAt(i));
				}

				vals = intCombinations(indent, 6, 2);
			} catch (Exception e) {
				vals = null;
				e.printStackTrace();
			}
		}

		key = indentStr(cipher, keyModule.substring(4, 8), -vals[0]);
		key2 = indentStr(cipher, keyModule.substring(8, 11), -vals[1]);

		module1 = indentStr(cipher, keyModule.substring(11, 14), -vals[2]);
		module2 = indentStr(cipher, keyModule.substring(14, 17), -vals[3]);

		user = indentStr(cipher, keyModule.substring(17, 21), -vals[4]);
		user2 = indentStr(cipher, keyModule.substring(21), -vals[5]);

		ans.put(license + "1", key);
		ans.put(license + "2", key2);
		ans.put(name + "1", module1);
		ans.put(name + "2", module2);
		ans.put(userName + "1", user);
		ans.put(userName + "2", user2);

	}

	public int[] intCombinations(int[] seed, int sizeArray, int startIndex)
			throws Exception {
		int[] ans = new int[sizeArray];
		boolean end = false;

		if (startIndex + sizeArray > seed.length * (seed.length - 1)) {
			throw new Exception("Start index too big: " + sizeArray
					+ ". Should be less than than "
					+ (seed.length * (seed.length - 1) - sizeArray - 1));
		}

		int seedIndex = 0;
		int combIndex = 0;

		while (seedIndex < seed.length && !end) {
			int subIndex = 0;
			while (subIndex < seed.length && !end) {
				if (combIndex >= startIndex) {
					int res = -seed[subIndex];
					for (int i = 0; i < seed.length; ++i) {
						if (subIndex != i && seedIndex != i) {
							res += seed[i];
						}
					}
					ans[combIndex - startIndex] = res;
					end = combIndex - startIndex + 1 == ans.length;
				}
				++combIndex;
				++subIndex;
			}
			++seedIndex;
		}
		for (int j = 0; j < ans.length; ++j) {
			end &= checkValidInt(ans[j]);
		}
		if (!end) {
			return null;
		}

		return ans;
	}

	public boolean checkValidInt(int indent) {
		return indent % 62 != 0;
	}

	public static String indentStr(String cipher, String str, int indent) {
		String ans = "";

		for (int i = 0; i < str.length(); ++i) {
			int tChIdx = cipher.indexOf(str.charAt(i)) + indent;
			if (tChIdx > 0) {
				ans += cipher.charAt(tChIdx % cipher.length());
			} else {
				while (tChIdx < 0) {
					tChIdx += cipher.length();
				}
				ans += cipher.charAt(tChIdx);
			}
		}
		return ans;
	}

	public boolean validateAllValuesSoft(Map<String, String> keysoft) {
		boolean valid = true;

		try {

			//logger.info("name 1 " + ans.get(name + "1"));
			//logger.info("key name sub 3 " + keysoft.get(name).substring(0, 3));
			valid &= ans.get(name + "1").equals(keysoft.get(name).substring(0, 3));
			//logger.info("valid name 1 " + valid);

			//logger.info("name 2 " + ans.get(name + "2"));
			//logger.info("key name lenght-3 " + keysoft.get(name).substring(keysoft.get(name).length() - 3));
			valid &= ans.get(name + "2").equals(keysoft.get(name).substring(keysoft.get(name).length() - 3));
			//logger.info("valid name 2 " + valid);

			//logger.info("mac " + ans.get(mac));
			//logger.info("mac lenght-8 " + keysoft.get(mac).substring(keysoft.get(mac).length() - 8));
			
			//valid &= ans.get(mac).equalsIgnoreCase(keysoft.get(mac).substring(keysoft.get(mac).length() - 8));
			
			valid &= validateMacAddress(ans.get(mac));
			
			//logger.info("valid mac " + valid);

			//valid &= Integer.valueOf(ans.get(clusterNb)).intValue() > Integer.valueOf(keysoft.get(clusterNb)).intValue();
			
			DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formatter.parse(("2015/06/01")));
			calendar.add(Calendar.DATE, Integer.parseInt(ans.get(date)));
			valid &= new Date(keysoft.get(date)).before(calendar.getTime());
			//logger.info("valid date " + valid);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return valid;
	}
	
	private boolean validateMacAddress(String macTocheck) throws ParseException {
		
		byte[] mac = null;
		try {
			
			logger.info("validateMacAddress " + macTocheck);
			
			Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
			while (networks.hasMoreElements()) {
				NetworkInterface network = networks.nextElement();
				mac = network.getHardwareAddress();
				if(mac != null){
					
					StringBuilder sbMac = new StringBuilder();
					for (int i = 0; i < mac.length; ++i) {
						sbMac.append(String.format("%02X", mac[i]));
					}
					
					//logger.info("Mac " + sbMac.toString());
					
					logger.info("MacAddress " + sbMac.substring(sbMac.length() - 8));
					
					if(macTocheck.equals(sbMac.substring(sbMac.length() - 8))){
						return true;
					}
				}
				
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean validateExpiredKey(Map<String, String> keysoft) throws ParseException {
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(formatter.parse(("2015/06/01")));
	    calendar.add(Calendar.DATE, Integer.parseInt(ans.get(date)));
	    
	    //logger.info("ans value " + ans.get(date));
	    //logger.info("date1 " + calendar.getTime());
	    //logger.info("date2 " + keysoft.get(date));
	    
		return new Date(keysoft.get(date)).before(calendar.getTime());
	}

	public String validateAllValuesModule(Map<String, String> keyModule) {
		StringBuffer error = new StringBuffer();
		try {

			boolean sys = Boolean.valueOf(keyModule.get("system")).booleanValue();
			String sysVal = "";
			if (sys) {
				sysVal = "s";
			} else {
				sysVal = "u";
			}

			if(ans != null){
				// License
				if(ans.get(license + "1") != null){
					//logger.info("license 1 " + ans.get(license + "1"));
					//logger.info("license sub 8 12 " + keyModule.get(license).substring(8, 12));
					if(!ans.get(license + "1").equals(keyModule.get(license).substring(8, 12))){
						error.append(PMLanguageManager.getText("error_module_key_license", new String[] { keyModule.get(name) }));
					}
				}

				if(ans.get(license + "2") != null){
					//logger.info("license 2 " + ans.get(license + "2"));
					//logger.info("license lenght-3 " + keyModule.get(license).substring(keyModule.get(license).length() - 3));
					if(!ans.get(license + "2").equals(keyModule.get(license).substring(keyModule.get(license).length() - 3))){
						error.append(PMLanguageManager.getText("error_module_key_license", new String[] { keyModule.get(name) }));
					}
				}

				if(ans.get(userName + "1") != null){
					if(!ans.get(userName + "1").substring(0, 1).equals(sysVal) ||
							(!sys && !ans.get(userName + "1").equals(keyModule.get(userName).substring(0, 4))) ||
							(!sys && !ans.get(userName + "2").equals(keyModule.get(userName).substring(keyModule.get(userName).length() - 3)))){
						error.append(PMLanguageManager.getText("error_module_key_username", new String[] { keyModule.get(name) }));
					}
				}

				// name
				if(ans.get(name + "1") != null){
					//logger.info("name 1 '" + ans.get(name + "1")+"'");
					//logger.info("name sub 3 '" + keyModule.get(name).substring(0, 3)+"'");
					if(!ans.get(name + "1").equals(keyModule.get(name).substring(0, 3))){
						error.append(PMLanguageManager.getText("error_module_key_name", new String[] { keyModule.get(name) }));
					}
				}

				if(ans.get(name + "2") != null){
					//logger.info("name 2 " + ans.get(name + "2"));
					//logger.info("name lenght-3 " + keyModule.get(name).substring(keyModule.get(name).length() - 3));
					if(!ans.get(name + "2").equals(keyModule.get(name).substring(keyModule.get(name).length() - 3))){
						error.append(PMLanguageManager.getText("error_module_key_name", new String[] { keyModule.get(name) }));
					}
				}

			}else{
				error.append(PMLanguageManager.getText("error_module_key_license", new String[] { keyModule.get(name) }));
			}

			//logger.info("error " + error.toString());

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

		return error.length() == 0 ? null : error.toString();
	}

	public int getNumberCluster() {
		return numberCluster;
	}

	public void setNumberCluster(int numberCluster) {
		this.numberCluster = numberCluster;
	}

}