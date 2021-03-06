package com.baitforbyte.networkhw1.follower;

import com.baitforbyte.networkhw1.shared.base.BaseClient;
import com.baitforbyte.networkhw1.shared.file.data.*;
import com.baitforbyte.networkhw1.shared.file.follower.FileClient;
import com.baitforbyte.networkhw1.shared.util.ApplicationMode;
import com.baitforbyte.networkhw1.shared.util.DirectoryUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConnectionToServer extends BaseClient {
    private final String GET_HASH_MESSAGE = "HASH";
    private final int LOOP_TIME = 30;
    // Start after 3 seconds waiting for file client to connect ?
    // TODO: Maybe not required
    private final int LOOP_DELAY = 3;
    private final TimeUnit LOOP_UNIT = TimeUnit.SECONDS;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected BufferedReader is;
    protected PrintWriter os;
    private FileClient client = null;
    private String directory;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port) {
        super(address, port);
    }

    /**
     * The tasks that are run in the scheduler
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void tasks() throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = getHashesFromServer();
        Set<String> localFileNames = compareHash(files);
        FileUtils.saveLog(localFileNames, directory, Constants.PREV_FILES_LOG_NAME);
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    @Override
    public void connect() throws IOException {
        super.connect();
        is = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        os = new PrintWriter(getSocket().getOutputStream());

        String portLine = sendForAnswer("CONNECTED");

        int filePort;
        try {
            filePort = Integer.parseInt(portLine);
        } catch (NumberFormatException ex) {
            throw new IOException("Cannot parse port number, incorrect server!\nMessage: " + ex.getMessage(), ex);
        }

        String identifier = is.readLine();
        client = new FileClient(getServerAddress(), filePort, identifier);
        client.connect();

        String localDirectoryName = is.readLine();
        directory = DirectoryUtils.getDirectoryInDesktop(localDirectoryName, ApplicationMode.FOLLOWER);

        startWorkingLoop();
    }

    /**
     * the scheduler which works for the LOOP_TIME final integer
     */
    private void startWorkingLoop() {
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
        System.out.println("[CLIENT] Sending: " + message);
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
        System.out.println("[CLIENT] Response: " + response);
        return response;
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    @Override
    public void disconnect() throws IOException {
        if (client != null) {
            client.disconnect();
        }

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
        ArrayList<String> filesToSend = new ArrayList<>();
        ArrayList<String> filesToRequest = new ArrayList<>();
        HashMap<String, FileData> localFiles = ChangeTracking.getLocalFiles(directory);
        Set<String> filesToDelete = ChangeTracking.getFilesToDelete(directory, Constants.PREV_FILES_LOG_NAME);

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

        List<String> removedFiles = requestFilesToDeleteFromServer();

        requestFilesFromServer(filesToRequest.stream()
                .filter(x -> !removedFiles.contains(x))
                .filter(x -> !filesToDelete.contains(x))
                .collect(Collectors.toList()));

        filesToSend.addAll(localFiles.keySet()
                .stream()
                .filter(x -> !x.endsWith(".veryspeciallog"))
                .filter(x -> !removedFiles.contains(x))
                .collect(Collectors.toList()));
        sendFilesToServer(filesToSend.stream()
                .filter(x -> !removedFiles.contains(x))
                .filter(x -> !filesToDelete.contains(x))
                .collect(Collectors.toList()));

        return ChangeTracking.getLocalFiles(directory).keySet();
    }

    /**
     * The conversation function with the server for communicating the deleted files in the follower
     *
     * @param filesToDelete the set of the names of the deleted files
     * @throws IOException
     */
    private void deleteFilesAtServer(Set<String> filesToDelete) throws IOException {
        for (String file : filesToDelete) {
            String response = sendForAnswer("DELETE");
            if (response.equals("SEND")) {
                response = sendForAnswer(file);
                while (!response.equals("DELETED")) {
                    response = sendForAnswer(file);
                }
            }
        }
    }

    /**
     * The conversation function with the server for communicating the files that exists in the server but not in the follower
     *
     * @param filesToRequest set of the names of the files for the server to send
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void requestFilesFromServer(List<String> filesToRequest) throws IOException, NoSuchAlgorithmException {
        for (String fileName : filesToRequest) {
            String response = "";
            FileTransmissionModel f = null;
            while (!response.equals("CONSISTENCY_CHECK_PASSED")) {
                String hash = sendForAnswer("SENDFILE" + fileName);
                f = client.tryReceiveFile();
                if (f != null && hash.equals(f.getHash())) {
                    response = sendForAnswer("CONSISTENCY_CHECK_PASSED");
                }
            }
            client.writeModelToPath(directory, f);
        }
    }

    /**
     * The conversation function with the server for communicating the files that exists in the follower but not in the server
     *
     * @param filesToSend the arraylist of the filenames of the files that are needed to be sent
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private void sendFilesToServer(List<String> filesToSend) throws IOException, NoSuchAlgorithmException {
        for (String fileName : filesToSend) {
            String response = "";
            System.out.println("Sending " + fileName);
            FileTransmissionModel f = client.getModelFromPath(directory, fileName);
            while (!response.equals("CONSISTENCY_CHECK_PASSED")) {
                response = sendForAnswer("SENDING");
                if (!response.equals("SEND")) {
                    continue;
                }
                client.sendFile(f);
                String hash = is.readLine();
                if (hash.equals(f.getHash())) {
                    response = sendForAnswer("CONSISTENCY_CHECK_PASSED");
                } else {
                    sendForAnswer("ERROR");
                }
            }
        }
    }

    /**
     * The conversation function with the server for communicating the files that are deleted in the server
     *
     * @return the list of the removed files
     * @throws IOException
     */
    public List<String> requestFilesToDeleteFromServer() throws IOException {
        List<String> removedFiles = new ArrayList<>();
        String response = sendForAnswer("REMOVE");
        if (response.equals("SENDING")) {
            response = is.readLine();
            System.out.println("[CLIENT] Response: " + response);
            while (!response.equals("DONE")) {
                removedFiles.add(response);
                FileUtils.deleteFile(directory, response);
                response = sendForAnswer("DELETED");
            }
        }
        return removedFiles;
    }


}


