package com.llamas.networkhw2.shared.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class BaseClient {
    protected String serverAddress;
    protected int serverPort;
    protected Socket s = null;
    protected BufferedReader is;
    protected PrintWriter os;

    /**
     * Open a socket for client at address:port
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public BaseClient(String address, int port) {
        serverAddress = address;
        serverPort = port;
    }    

    public void connect() throws IOException{
        if (s != null) {
            return;
        }
        s = createSocket();
        intializeStreams();
        handshake();
        listenCommandAndSend();
         
    }

    public abstract void handshake() throws IOException; 

    public abstract Socket createSocket() throws IOException;


    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect() throws IOException {
        try {
            is.close();
            os.close();
            s.close();
            System.out.println("BaseClient: Connection closed");
        } catch (IOException e) {
            throw new IOException("Cannot close connection: " + e.getMessage(), e);
        }
        System.out.println("Disconnected!");
    }

    public void intializeStreams() throws IOException {
        try {
            is=new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            os= new PrintWriter(getSocket().getOutputStream());            
        } catch (IOException e) {
            throw new IOException("Cannot open connection: " + e.getMessage(), e);
        }
    }

    protected void listenCommandAndSend() throws IOException {
        Scanner sc = new Scanner(System.in);
        String line = "";
        try {
            while(line.compareTo("QUIT!") != 0) {
                System.out.println("--------------------");
                System.out.print("Enter your message: ");
                line = sc.nextLine();
                String ans = sendForAnswer(line);
                if(line.startsWith("submit")){
                    while(!ans.equals("OK")){
                        ans = sendForAnswer(line);
                    }
                }
                System.out.println("Server " + getSocket().getRemoteSocketAddress() + " sent : " + ans);
              }
              System.out.println("Quit command is received. Disconnecting..");
              sc.close();
              disconnect();
        } catch (IOException e) {
            throw new IOException("Error occured: " + e.getMessage(), e);
        }
        
      }
      
    public String sendForAnswer(String message) throws IOException {
        message = message.trim();
        String response = new String();
        os.println(message);
        os.flush();      
        response = is.readLine();
        return response;
    }


    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public Socket getSocket() {
        return s;
    }
}


