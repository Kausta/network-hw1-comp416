package com.llamas.networkhw2.master;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLServer extends Thread {

  private final String SERVER_KEYSTORE_FILE = "llamaprotocol.jks";
  private final String SERVER_KEYSTORE_PASSWORD = "LlamaProtocol";
  private final String SERVER_KEY_PASSWORD = "LlamaProtocol";
  private SSLServerSocket sslServerSocket;
  private SSLServerSocketFactory sslServerSocketFactory;
  // private ServerControlPanel serverControlPanel;

  public SSLServer(int port) throws IOException, GeneralSecurityException {
    // serverControlPanel = new ServerControlPanel("hello server!");

    /*
     * Instance of SSL protocol with TLS variance
     */
    SSLContext sc = SSLContext.getInstance("TLS");

    /*
     * Key management of the server
     */
    char ksPass[] = SERVER_KEYSTORE_PASSWORD.toCharArray();
    System.out.println(System.getProperty("user.dir"));
    System.out.println(new FileInputStream(SERVER_KEYSTORE_FILE));
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(new FileInputStream(SERVER_KEYSTORE_FILE), ksPass);
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, SERVER_KEY_PASSWORD.toCharArray());
    sc.init(kmf.getKeyManagers(), null, null);

    /*
     * SSL socket factory which creates SSLSockets
     */
    sslServerSocketFactory = sc.getServerSocketFactory();
    sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);

    System.out.println("SSL server is up and running on port " + port);
    while (true) {
      listenAndAccept();
    }
  }

  /**
   * Listens to the line and starts a connection on receiving a request from the
   * client The connection is started and initiated as a ServerThread object
   */
  public void listenAndAccept() throws IOException {
    SSLSocket s;
      try
      {
          s = (SSLSocket) sslServerSocket.accept();
          System.out.println("An SSL connection was established with a client on the address of " + s.getRemoteSocketAddress());
          SSLServerThread st = new SSLServerThread(s);
          st.start();

      }

      catch (Exception e)
      {
          e.printStackTrace();
          System.out.println("Server Class.Connection establishment error inside listen and accept function");
      }
  }

  private List<SSLServerThread> threads = new ArrayList<>();
}
