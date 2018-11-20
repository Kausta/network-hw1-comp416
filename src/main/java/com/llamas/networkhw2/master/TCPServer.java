package com.llamas.networkhw2.master;

import com.llamas.networkhw2.shared.base.BaseServer;

import java.io.IOException;
import java.net.Socket;

public class TCPServer extends BaseServer {

  public TCPServer(int port) throws IOException {
    super(port, "A TCP connection was established with a client on the address of ", "TCP");
    finishInitializing(port);
  }

  /**
   * Listens to the line and starts a connection on receiving a request from the
   * client The connection is started and initiated as a ServerThread object
   */
  public void listenAndAccept() throws IOException{
    Socket s;
     try
     {
         s = serverSocket.accept();
         ServerThread st = new ServerThread(s);
         st.start();
         System.out.println(sucessfullConnectionMessage + s.getRemoteSocketAddress());
 
     }
 
     catch (Exception e)
     {
         e.printStackTrace();
         System.out.println("Server Class.Connection establishment error inside listen and accept function");
     }
     }



}
