package com.llamas.networkhw2.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.llamas.networkhw2.database.SQLiteDatabase;

public class ServerThread extends Thread {
    private Socket s;
    private BufferedReader is;
    private PrintWriter os;
    private SQLiteDatabase db;

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s){
        this.s = s;
        String ip = s.getInetAddress().toString().replace(".", "").replace("/", "");
        System.out.println(ip);
        db = new SQLiteDatabase("data");
    }

    public void run() {
        String line = "";
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
            String[] message = line.split(" ");
            for (int i = 0; i < message.length; i++){
              message[i] = message[i].replace(",", "");
            }
            System.out.println("Client " + s.getRemoteSocketAddress() + " sent : " + line);
            switch (message[0].toLowerCase()) {
              case "submit":
                db.create(message[1], message[2]);
                sendMessage("OK");
                break;
              
              case "get":
                String ans = db.retrieve(message[1]);
                if(ans == null){
                  sendMessage("No stored value for " + message[1]);
                }else{
                  sendMessage(ans);
                }
                break;
            
              default:
                sendMessage("No such command");
                break;
            }            
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

      private void sendMessage(String message){
        message = message.trim();
        os.println(message);
        os.flush();
      }
}