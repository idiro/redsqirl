package com.redsqirl.keymanager.ciphers;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import org.apache.log4j.Logger;

public abstract class KeyCipher {

	private static Logger logger = Logger.getLogger(KeyCipher.class);
	protected static String cipher = "FDx3KruzJlYRy2VLeqd0f6j4hknwUBbH1tSMmgAQoW8ZXCpGT7NO5sv9ciPaEI";
	private String macString = "";

	public String getMACAddress() {
		macString = null;
		try{
			byte[] mac = null;
			Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
			while (networks.hasMoreElements()) {
				try{
					NetworkInterface network = networks.nextElement();
					mac = network.getHardwareAddress();
					if(mac != null){
						break;
					}
				} catch (SocketException e){
					logger.error(e,e);
				}
			}

			StringBuilder sbMac = new StringBuilder();
			for (int i = 0; i < mac.length; ++i) {
				sbMac.append(String.format("%02X", mac[i]));
			}
			macString = sbMac.toString();

		}catch (Exception e){
			logger.error(e,e);
		}

		return macString;
	}

	public String randomizeString(String s) {
		Random rnd = new Random();
		for (int i = 0; i < 10000 * s.length(); ++i) {
			int pa = rnd.nextInt(s.length());
			char a = s.charAt(pa);
			int pb = rnd.nextInt(s.length());
			char b = s.charAt(pb);

			if (pa > pb) {
				s = s.replace(a, b);
				s = s.replaceFirst("" + b, "" + a);
			} else {
				s = s.replace(b, a);
				s = s.replaceFirst("" + a, "" + b);
			}
		}
		return s;
	}

	public char transform(int indent) {
		return cipher.charAt(indent);
	}


}
