package idiro.workflow.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;

public class RestrictedRMIRegistry implements RMIServerSocketFactory {

    public ServerSocket createServerSocket(int port) throws IOException {
        // Tricky bit; make a server socket with bound address
        return new ServerSocket(port, 0, InetAddress.getLocalHost());
    }
    
}
