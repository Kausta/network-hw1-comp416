package com.llamas.networkhw2.follower;

import com.llamas.networkhw2.shared.base.BaseClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class TCPConnectionToServer extends BaseClient {

  protected BufferedReader is;
  protected PrintWriter os;
  private String line = new String();

  public TCPConnectionToServer(String address, int port) throws IOException {
    super(address, port);
  }

  public void connect() throws IOException {
    super.connect();
    is = new BufferedReader(new InputStreamReader(super.getSocket().getInputStream()));
    os = new PrintWriter(super.getSocket().getOutputStream());
    listenCommandAndSend();
  }

  public String sendForAnswer(String message) throws IOException {
    String response = new String();
    os.println(message);
    os.flush();
    response = is.readLine();
    return response;
  }

  public void disconnect() throws IOException {
    super.disconnect();
    System.out.println("Disconnected!");
  }

  private void listenCommandAndSend() throws IOException {
    while(line.compareTo("QUIT!") != 0) {
      System.out.println("--------------------");
      System.out.print("Enter your message: ");
      Scanner sc = new Scanner(System.in);
      line = sc.nextLine();
      os.println(line);
      os.flush();
      String response = is.readLine();
      System.out.println("Server " + super.getSocket().getRemoteSocketAddress() + " sent : " + response);
    }
    System.out.println("Quit command is received. Disconnecting..");
    disconnect();
  }

}
