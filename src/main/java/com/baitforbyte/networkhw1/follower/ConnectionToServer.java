package com.baitforbyte.networkhw1.follower;

import com.baitforbyte.networkhw1.shared.base.BaseClient;
import com.baitforbyte.networkhw1.shared.file.follower.IFileClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer extends BaseClient {
    private static final String GET_HASH_MESSAGE = "Send hashes";

    protected BufferedReader is;
    protected PrintWriter os;
    private IFileClient client;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port, IFileClient fileClient) {
        super(address, port);
        this.client = fileClient;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    @Override
    public void connect() throws IOException {
        super.connect();
        is = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        os = new PrintWriter(getSocket().getOutputStream());
    }

    /**
     * sends the message String to the server and retrives the answer
     *
     * @param message input message string to the server
     * @return the received server answer
     */
    public String sendForAnswer(String message) throws IOException {
        String response = "";
            /*
            Sends the message to the server via PrintWriter
             */
        os.println(message);
        os.flush();
            /*
            Reads a line from the server via Buffer Reader
             */
        response = is.readLine();
        return response;
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    @Override
    public void disconnect() throws IOException {
        is.close();
        os.close();
        System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");

        super.disconnect();
    }

    /**
     * Requests and gets the hashes from the client
     *
     * @return the recieved filename, hash and last change times
     */
    public HashMap<String, FileData> getHash() throws IOException {
        HashMap<String, FileData> files = new HashMap<String, FileData>();
        os.println(GET_HASH_MESSAGE);
        os.flush();

        int numberOfLines = Integer.parseInt(is.readLine()) * 3;
        for (int i = 0; i < numberOfLines; i++) {
            String fileName = is.readLine();
            String hash = is.readLine();
            String date = is.readLine();
            FileData data = new FileData(hash, Long.parseLong(date));
            files.put(fileName, data);
        }

        return files;
    }

    /**
     * compares hashes of local and remote files then calls send and recieve functions
     *
     * @param files hashmap of filenames, hashes and dates
     */
    public void compareHash(HashMap<String, FileData> files) {
        ArrayList<String> filesToSend = new ArrayList<String>();
        ArrayList<String> filesToRequest = new ArrayList<String>();
        HashMap<String, FileData> localFiles = getLocalFiles();
        for (String fileName : files.keySet()) {
            if (localFiles.containsKey(fileName)) {
                FileData local = localFiles.get(fileName);
                FileData remote = files.get(fileName);
                if (local.hash.equals(remote.hash)) {
                    continue;
                } else {
                    long dateDiff = local.lastChangeTime - remote.lastChangeTime;
                    if (dateDiff > 0) {
                        filesToSend.add(fileName);
                    } else if (dateDiff < 0) {
                        filesToRequest.add(fileName);
                    }
                }
                localFiles.remove(fileName);
            } else {
                filesToRequest.add(fileName);
            }
        }
        for (String fileName : localFiles.keySet()) {
            filesToSend.add(fileName);
        }
        // TODO: request files
        // TODO: send files
    }

    public HashMap<String, FileData> getLocalFiles() {
        return null; // TODO: implement
    }


}


