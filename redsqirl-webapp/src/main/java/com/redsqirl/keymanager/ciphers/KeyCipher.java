package com.redsqirl.keymanager.ciphers;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

public abstract class KeyCipher {

	static String redSqirlCypher = "FDx3KruzJlYRy2VLeqd0f6j4hknwUBbH1tSMmgAQoW8ZXCpGT7NO5sv9ciPaEI";
	static String nutCrackerCypher = "FDx3KruzJlYRy2VLeqd0f6j4hknwUBbH1tSMmgAQoW8ZXCpGT7NO5sv9ciPaEI";

	protected String cipher = "";



	public String getMACAddress() {
		
		String macString= "";
		
		byte[] mac = null;
		try {
			Enumeration<NetworkInterface> networks = NetworkInterface
					.getNetworkInterfaces();
			while (networks.hasMoreElements() && mac == null) {
				NetworkInterface network = networks.nextElement();
				mac = network.getHardwareAddress();
			}

		} catch (SocketException e) {

			e.printStackTrace();

		}

		StringBuilder sbMac = new StringBuilder();
		for (int i = 0; i < mac.length; ++i) {
			sbMac.append(String.format("%02X", mac[i]));
		}
		macString = sbMac.toString();
		
		return macString;
	}
	
	public String randomizeString(String s){
		Random rnd = new Random();
		for(int i = 0; i < 10000*s.length();++i){
			int pa = rnd.nextInt(s.length());
			char a = s.charAt(pa);
			int pb = rnd.nextInt(s.length());
			char b = s.charAt(pb);
			
			if(pa > pb){
				s = s.replace(a, b);
				s = s.replaceFirst(""+b, ""+a);
			}else{
				s = s.replace(b, a);
				s = s.replaceFirst(""+a, ""+b);
			}
		}
		return s;
	}
	
	public char transform(int indent){
		return cipher.charAt(indent);
	}
	
	public static String indentStr(String str, int indent){
		String ans = "";
		for(int i=0; i < str.length();++i){
			int tChIdx = redSqirlCypher.indexOf(str.charAt(i))+indent;
			if(tChIdx < -2*redSqirlCypher.length()){
				ans += redSqirlCypher.charAt(3*redSqirlCypher.length()+tChIdx);
			}else if(tChIdx < -redSqirlCypher.length()){
				ans += redSqirlCypher.charAt(2*redSqirlCypher.length()+tChIdx);
			}else if(tChIdx < 0){
				ans += redSqirlCypher.charAt(redSqirlCypher.length()+tChIdx);
			}else if(tChIdx >= 2* redSqirlCypher.length()){
				ans += redSqirlCypher.charAt(tChIdx - 2*redSqirlCypher.length());
			}else if(tChIdx >= redSqirlCypher.length()){
				ans += redSqirlCypher.charAt(tChIdx - redSqirlCypher.length());
			}else{
				ans += redSqirlCypher.charAt(tChIdx);
			}
		}
		return ans;
	}
	
	
	public int getNumberOfusers(){
		int users = 0;
		
		File folder = new File("/home/");
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			System.out.println(f.getPath());
		}
		
		return users;
	}

	public boolean checkNumberOfUsers(int userInputNumber) {
		boolean atLeast = true;
		
		int users = getNumberOfusers();
		
		atLeast = users > userInputNumber ? false : true;

		return atLeast;

	}

}
