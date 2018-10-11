package com.baitforbyte.networkhw1.follower;

import com.baitforbyte.networkhw1.shared.base.BaseClient;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionException;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.file.follower.IFileClient;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer extends BaseClient {
    private static final String GET_HASH_MESSAGE = "HASH";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected BufferedReader is;
    protected PrintWriter os;
    private IFileClient client;
    private File directory;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port, IFileClient fileClient, String directoryName) {
        super(address, port);
        this.client = fileClient;
        this.directory = new File(directoryName);
    }

    public void startWorking() throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = getHash();
        compareHash(files);

    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    @Override
    public void connect() throws IOException {
        super.connect();
        is = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        os = new PrintWriter(getSocket().getOutputStream());
        scheduler.scheduleAtFixedRate(() -> {
            try {
                startWorking();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);

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
        String number = sendForAnswer(GET_HASH_MESSAGE);

        int numberOfLines = Integer.parseInt(number) * 3;
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
     * compares hashes of local and remote files then calls send and recieve
     * functions
     *
     * @param files hashmap of filenames, hashes and dates
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws FileTransmissionException
     */
    public void compareHash(HashMap<String, FileData> files) throws NoSuchAlgorithmException, IOException, FileTransmissionException {
        ArrayList<String> filesToSend = new ArrayList<String>();
        ArrayList<String> filesToRequest = new ArrayList<String>();
        HashMap<String, FileData> localFiles = getLocalFiles();
        for (String fileName : files.keySet()) {
            if (localFiles.containsKey(fileName)) {
                FileData local = localFiles.get(fileName);
                FileData remote = files.get(fileName);
                if (local.getHash().equals(remote.getHash())) {
                    continue;
                } else {
                    long dateDiff = local.getLastChangeTime() - remote.getLastChangeTime();
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
        for (String fileName : filesToRequest) {
            String response = "";
            FileTransmissionModel f = null;
            while(!response.equals("CORRECT")){
                String hash = sendForAnswer("SEND" + fileName);
                f = client.tryReceiveFile();
                if (f != null && hash.equals(f.getHash())){
                    response = sendForAnswer("CORRECT");
                }
            }   
            client.writeModelToPath(directory.toPath().toString(), f);
        }
        
        for (String fileName : filesToSend) {
            String response = "";
            FileTransmissionModel f = client.getModelFromPath(directory.toPath().toString(), fileName);
            while(!response.equals("CORRECT")){
                response = sendForAnswer("SENDING");
                if(response.equals("SEND")){
                    client.sendFile(f);
                    response = is.readLine();
                    if (Objects.equals(response, "CORRECT")){
                        os.write("CORRECT");
                    }
                }
            }
        }
    }

    /**
     * Gets local files in the designated folder
     *
     * @return Hashmap of files, hashes and last changed times
     * @throws IOException              When a file reading exception occurs
     * @throws NoSuchAlgorithmException When hash function is not found, should not occur with the algorithms we use
     */
    private HashMap<String, FileData> getLocalFiles() throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = new HashMap<>();
        FileTransmissionModel[] fileModels = FileUtils.getAllFilesInDirectory(directory);

        for (FileTransmissionModel file : fileModels) {
            files.put(file.getFilename(), new FileData(file.getHash(), file.getLastModifiedTimestamp()));
        }
        return files;
    }


}


