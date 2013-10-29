package idiro.workflow.server;

import java.rmi.*;

import org.junit.Test;



public class RMIobjects {
//	@Test
	public static void main(String[] args) throws Exception {
		String host;
		if (args.length == 0)
			host = "localhost:2001";
		else
			host = args[0];
		String[] names = Naming.list("//" + host + "/");
		System.out.println(host);
		for (int i = 0; i < names.length; i++)
			System.out.println("object : "+names[i]);
	} // public static void main()
} // public class RegList()

