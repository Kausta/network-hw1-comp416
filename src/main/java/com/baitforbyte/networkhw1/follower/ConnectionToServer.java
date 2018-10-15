package com.baitforbyte.networkhw1.follower;

import com.baitforbyte.networkhw1.shared.base.BaseClient;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionException;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.file.follower.IFileClient;
import com.baitforbyte.networkhw1.shared.util.DirectoryUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer extends BaseClient {
    private final String GET_HASH_MESSAGE = "HASH";
    private final int LOOP_TIME = 30; 
    private final int LOOP_DELAY = 0; 
    private final TimeUnit LOOP_UNIT = TimeUnit.SECONDS; 

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    protected BufferedReader is;
    protected PrintWriter os;
    private IFileClient client;
    private String directory;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port, IFileClient fileClient) {
        super(address, port);
        this.client = fileClient;
        this.directory = DirectoryUtils.getDirectoryInDesktop("CloudDrive1");
    }

    // TODO: write docstring
    public void tasks() throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = getHashesFromServer();
        Set<String> localFileNames = compareHash(files);
        FileUtils.saveLog(localFileNames, "filename"); // TODO: filename
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    @Override
    public void connect() throws IOException {
        super.connect();
        is = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        os = new PrintWriter(getSocket().getOutputStream());
        startWorkingLoop();
    }

    // TODO: write docstring
    private void startWorkingLoop(){
        scheduler.scheduleAtFixedRate(() -> {
            try {
                tasks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, LOOP_DELAY, LOOP_TIME, LOOP_UNIT);
    }

    /**
     * sends the message String to the server and retrives the answer
     *
     * @param message input message string to the server
     * @return the received server answer
     */
    public String sendForAnswer(String message) throws IOException {
        String response;
            /*
            Sends the message to the server via PrintWriter
             */
        os.println(message);
        os.flush();
            /*
            Reads a line from the server via Buffer Reader
             */
        response = is.readLine();
        int counter = 0;
        while (response == null) {
            if (counter > 5) {
                throw new RuntimeException("Connection closed");
            }
            System.out.println("Waiting for connection");
            counter++;
            response = is.readLine();
        }
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
    public HashMap<String, FileData> getHashesFromServer() throws IOException {
        HashMap<String, FileData> files = new HashMap<>();
        String number = sendForAnswer(GET_HASH_MESSAGE);

        int numberOfLines = Integer.parseInt(number);
        for (int i = 0; i < numberOfLines; i++) {
            String fileName = is.readLine();
            String hash = is.readLine();
            String date = is.readLine();
            System.out.println("Server sent " + fileName + " : " + hash + " : " + date);
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
    public Set<String> compareHash(HashMap<String, FileData> files) throws NoSuchAlgorithmException, IOException, FileTransmissionException {
        ArrayList<String> filesToSend = new ArrayList<String>();
        ArrayList<String> filesToRequest = new ArrayList<String>();
        HashMap<String, FileData> localFiles = getLocalFiles();
        Set<String> localFileNames = getLocalFiles().keySet();
        Set<String> filesToDelete = getFilesToDelete("fileName", localFileNames); // TODO: filename

        for (String fileName : files.keySet()) {
            if (localFiles.containsKey(fileName)) {
                FileData local = localFiles.get(fileName);
                FileData remote = files.get(fileName);
                if (!local.getHash().equals(remote.getHash())) {
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

        deleteFilesAtServer(filesToDelete);

        requestFilesToDeleteFromServer();

        requestFilesFromServer(filesToRequest);
        
        sendFilesToServer(filesToSend);       
        
        return localFileNames;
    }

    // TODO: write docstring
    private void deleteFilesAtServer(Set<String> filesToDelete) throws IOException {
        for (String file : filesToDelete) {
            String response = sendForAnswer("DELETE");
            if(response.equals("SEND")){
                response = sendForAnswer(file);
                while(!response.equals("DELETED")){
                    response = sendForAnswer(file);
                }
            }
        }
    }

    // TODO: write docstring
    private void requestFilesFromServer(ArrayList<String> filesToRequest) throws IOException, NoSuchAlgorithmException {
        for (String fileName : filesToRequest) {
            String response = "";
            FileTransmissionModel f = null;
            while (!response.equals("CORRECT")) {
                String hash = sendForAnswer("SENDFILE" + fileName);
                f = client.tryReceiveFile();
                if (f != null && hash.equals(f.getHash())) {
                    response = sendForAnswer("CORRECT");
                }
            }
            client.writeModelToPath(directory, f);
        }
    }

    // TODO: write docstring
    private void sendFilesToServer(ArrayList<String> filesToSend) throws IOException, NoSuchAlgorithmException {
        for (String fileName : filesToSend) {
            String response = "";
            System.out.println("Sending " + fileName);
            FileTransmissionModel f = client.getModelFromPath(directory, fileName);
            while (!response.equals("CORRECT")) {
                response = sendForAnswer("SENDING");
                if (!response.equals("SEND")) {
                    continue;
                }
                client.sendFile(f);
                String hash = is.readLine();
                if (hash.equals(f.getHash())) {
                    response = sendForAnswer("CORRECT");
                } else {
                    sendForAnswer("ERROR");
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

    // TODO: write docstring
    private Set<String> getFilesToDelete(String fileName, Set<String> files){
        Set<String> previousFiles = FileUtils.readLog(fileName);
        for (String file : files) {
            previousFiles.remove(file);
        }
        return previousFiles;
    }

    // TODO: write docstring
    private void requestFilesToDeleteFromServer() throws IOException {
        String response = sendForAnswer("REMOVE");
        if(response.equals("SENDING")){
            response = is.readLine();
            while(!response.equals("DONE")){                
                Files.delete(Paths.get(response));
                response = sendForAnswer("DELETED");
            }
        }
    }


}


