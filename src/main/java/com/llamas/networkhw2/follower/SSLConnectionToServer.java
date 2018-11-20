package com.llamas.networkhw2.follower;

import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.llamas.networkhw2.shared.base.BaseClient;

public class SSLConnectionToServer extends BaseClient {
    /*
    Name of key store file
     */
    private final String KEY_STORE_NAME =  "client.jks";
    /*
    Password to the key store file
     */
    private final String KEY_STORE_PASSWORD = "123456";
    private SocketFactory sslSocketFactory;

    public SSLConnectionToServer(String address, int port) {
        super(address, port);
        /*
        Loads the keystore's address of client
         */
        System.setProperty("javax.net.ssl.trustStore", KEY_STORE_NAME);

        /*
        Loads the keystore's password of client
         */
        System.setProperty("javax.net.ssl.trustStorePassword", KEY_STORE_PASSWORD);
        sslSocketFactory = SSLSocketFactory.getDefault();
    }

    public Socket createSocket() throws IOException {
        try {
            return sslSocketFactory.createSocket(serverAddress, serverPort);
        }
        catch (Exception e) {
            throw new IOException("Error: no server has been found on " + serverAddress + "/" + serverPort, e);
        }
    }

    @Override
    public void handshake() throws IOException {
        try {
            ((SSLSocket) s).startHandshake();
        } catch (IOException e) {
            throw new IOException("Cannot create SSL Socket: " + e.getMessage(), e);
        }
    }
}


