package com.redsqirl.workflow.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
/**
 * Class to create server sockets
 * @author keith
 *
 */
public class RestrictedRMIRegistry implements RMIServerSocketFactory {
	/**
	 * Create a server socket 
	 * @param port
	 * @throws IOException
	 */
    public ServerSocket createServerSocket(int port) throws IOException {
        // Tricky bit; make a server socket with bound address
        return new ServerSocket(port, 0, InetAddress.getLocalHost());
    }
    
}
