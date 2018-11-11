package com.llamas.networkhw2.follower;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLConnectionToServer {
    /*
    Name of key store file
     */
    private final String KEY_STORE_NAME =  "clientkeystore";
    /*
    Password to the key store file
     */
    private final String KEY_STORE_PASSWORD = "storepass";
    private SSLSocketFactory sslSocketFactory;
    private SSLSocket sslSocket;
    private BufferedReader is;
    private PrintWriter os;

    protected String serverAddress;
    protected int serverPort;

    public SSLConnectionToServer(String address, int port) {
        serverAddress = address;
        serverPort = port;
        /*
        Loads the keystore's address of client
         */
        System.setProperty("javax.net.ssl.trustStore", KEY_STORE_NAME);

        /*
        Loads the keystore's password of client
         */
        System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);
    }

    public void connect() throws IOException {
        try
            {
                sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);
                sslSocket.startHandshake();
                is=new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                os= new PrintWriter(sslSocket.getOutputStream());
                System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
            }
        catch (Exception e)
            {
                e.printStackTrace();
            }
    }
    
    public String sendForAnswer(String message) throws IOException {
        String response = new String();
        os.println(message);
        os.flush();
        response = is.readLine();
        return response;
    }

    public void disconnect() throws IOException {
        is.close();
        os.close();
        sslSocket.close();
    }
}


