package com.llamas.networkhw2.master;

import com.llamas.networkhw2.shared.base.BaseServer;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer extends BaseServer {

  public TCPServer(int port) throws IOException, GeneralSecurityException {
    super(port);
    System.out.println("TCP server is up and running on port " + port);
    System.out.println("--------------------");
    while(true) {
      listenAndAccept();
    }
  }

  public void listenAndAccept() throws IOException {
    Socket s = getServerSocket().accept();
    TCPServerThread st = new TCPServerThread(this, s);
    st.start();
    threads.add(st);
  }

  private List<TCPServerThread> threads = new ArrayList<>();

}
