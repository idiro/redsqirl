package com.redsqirl.keymanager.ciphers;

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

import com.jcraft.jsch.Logger;

public abstract class KeyCipher {

	protected static String cipher = "FDx3KruzJlYRy2VLeqd0f6j4hknwUBbH1tSMmgAQoW8ZXCpGT7NO5sv9ciPaEI";
	private String macString = "";

	public String getMACAddress() {


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
