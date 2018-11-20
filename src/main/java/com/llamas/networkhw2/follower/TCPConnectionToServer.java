package com.llamas.networkhw2.follower;

import com.llamas.networkhw2.shared.base.BaseClient;

import java.io.IOException;
import java.net.Socket;

public class TCPConnectionToServer extends BaseClient { 

  public TCPConnectionToServer(String address, int port) throws IOException {
    super(address, port);
  }

  @Override
  public Socket createSocket() throws IOException {
    try {
      return new Socket(serverAddress, serverPort);
    } catch (IOException e) {
        throw new IOException("Error: no server has been found on " + serverAddress + "/" + serverPort, e);
    }
}

  @Override
  public void handshake() {

  }

  

}
