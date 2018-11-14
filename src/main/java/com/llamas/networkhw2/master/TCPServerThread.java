package com.llamas.networkhw2.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPServerThread extends Thread {

  private final String SERVER_ACK_MESSAGE = "server_ack";
  private TCPServer server;
  private Socket s;
  private BufferedReader is;
  private PrintWriter os;
  private String line = new String();

  public TCPServerThread(TCPServer server, Socket s) {
    this.server = server;
    this.s = s;
  }

  /**
   * The server thread, echos the client until it receives the QUIT string from the client
   */
  public void run() {
    try
    {
      is = new BufferedReader(new InputStreamReader(s.getInputStream()));
      os = new PrintWriter(s.getOutputStream());
    }
    catch (IOException e)
    {
      System.out.println("Server Thread. Run. IO error in server thread");
    }

    try
    {
      while (line.compareTo("QUIT!") != 0) {
        line = is.readLine();
        os.println(SERVER_ACK_MESSAGE);
        os.flush();
        System.out.println("Client " + s.getRemoteSocketAddress() + " sent : " + line);
      }
    }
    catch (IOException e)
    {
      line = this.getClass().toString(); //reused String line for getting thread name
      System.out.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
    }
    catch (NullPointerException e)
    {
      line = this.getClass().toString(); //reused String line for getting thread name
      System.out.println("Server Thread. Run.Client " + line + " Closed");
    } finally
    {
      try
      {
        System.out.println("Closing the connection");
        if (is != null)
        {
          is.close();
          System.out.println(" Socket Input Stream Closed");
        }

        if (os != null)
        {
          os.close();
          System.out.println("Socket Out Closed");
        }
        if (s != null)
        {
          s.close();
          System.out.println("Socket Closed");
        }

      }
      catch (IOException ie)
      {
        System.out.println("Socket Close Error");
      }
    }//end finally
  }
}
