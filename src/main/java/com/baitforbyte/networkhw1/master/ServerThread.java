package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.ApplicationConfiguration;
import com.baitforbyte.networkhw1.shared.file.data.ChangeTracking;
import com.baitforbyte.networkhw1.shared.file.data.Constants;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.file.master.IFileServer;
import com.baitforbyte.networkhw1.shared.file.master.IFileServerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class ServerThread extends Thread {
    private static AtomicInteger localClientNumber = new AtomicInteger(1);

    private final HashSet<String> deletedFiles;
    private final IFileServer fsServer;
    private final int filePort;
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String clientIdentifier;
    private String line = "";
    private String directory;

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s, IFileServer fsServer, int filePort, String directory) {
        this.s = s;
        this.fsServer = fsServer;
        this.filePort = filePort;
        this.directory = directory;
        deletedFiles = new HashSet<>();
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run() {
        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

            String connectedLine = is.readLine();
            if (!connectedLine.startsWith("CONNECTED")) {
                throw new RuntimeException("Cannot join, incorrect client");
            }
            sendToClient("" + filePort);
            clientIdentifier = s.getInetAddress().getHostAddress() + "@" + s.getPort();
            sendToClient(clientIdentifier);

            ApplicationConfiguration instance = ApplicationConfiguration.getInstance();
            String folderName = instance.getFolderName();
            if (instance.isLocalhostAddress(s)) {
                int clientId = localClientNumber.getAndIncrement();
                folderName += clientId;
            }
            sendToClient(folderName);
        } catch (IOException e) {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try {
            while (line.compareTo("QUIT") != 0) {
                line = is.readLine();
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent : " + line);
                if (line.startsWith("SENDFILE")) {
                    FileTransmissionModel f = getFsThread().getModelFromPath(directory, line.substring(8));
                    sendToClient(f.getHash());
                    getFsThread().sendFile(f);
                } else if (line.startsWith("CONSISTENCY_CHECK_PASSED")) {
                    sendToClient("CONSISTENCY_CHECK_PASSED");
                } else if (line.startsWith("SENDING")) {
                    sendToClient("SEND");
                    FileTransmissionModel f = getFsThread().tryReceiveFile();
                    sendToClient(f.getHash());
                    String answer = is.readLine();
                    if (answer.equals("CONSISTENCY_CHECK_PASSED")) {
                        sendToClient("CONSISTENCY_CHECK_PASSED");
                        getFsThread().writeModelToPath(directory, f);
                    }
                } else if (line.startsWith("HASH")) {
                    HashMap<String, FileData> files = getLocalFiles();
                    sendToClient("" + files.size());
                    for (String fileName : files.keySet()) {
                        FileData file = files.get(fileName);

                        sendToClient(fileName);
                        sendToClient(file.getHash());
                        sendToClient("" + file.getLastChangeTime());
                    }
                } else if (line.startsWith("DELETE")) {
                    sendToClient("SEND");
                    String fileName = is.readLine();
                    FileUtils.deleteFile(directory, fileName);
                    sendToClient("DELETED");
                } else if (line.startsWith("REMOVE")) {
                    Set<String> filesToDelete = deletedFiles;
                    sendToClient("SENDING");
                    String response = "";
                    for (String file : filesToDelete) {
                        while (!response.equals("DELETED")) {
                            sendToClient(file);
                            response = is.readLine();
                        }
                        filesToDelete.remove(file);
                        response = "";
                    }
                    sendToClient("DONE");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server Thread. Run. IO Error/ Client " + getName() + " terminated abruptly");
        } catch (NullPointerException e) {
            e.getMessage();
            System.err.println("Server Thread. Run.Client " + getName() + " Closed");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Closing the connection");
                if (is != null) {
                    is.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (os != null) {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.err.println("Socket Closed");
                }
                if (getFsThread() != null) {
                    getFsThread().interrupt();
                }

            } catch (IOException ie) {
                System.err.println("Socket Close Error");
            }
        }//end finally


    }

    /**
     * Gets local files in the designated folder
     *
     * @return Hashmap of files, hashes and last changed times
     * @throws IOException              When a file reading exception occurs
     * @throws NoSuchAlgorithmException When hash function is not found, should not occur with the algorithms we use
     */
    private HashMap<String, FileData> getLocalFiles() throws IOException, NoSuchAlgorithmException {
        return ChangeTracking.getLocalFiles(directory);
    }

    /**
     * The function to send a string to the client
     * @param s the string to be sent
     */
    private void sendToClient(String s) {
        System.out.println("Send " + s);
        os.write(s + "\n");
        os.flush();
    }

    private IFileServerThread getFsThread() {
        return fsServer.getFSThread(clientIdentifier);
    }


    public HashSet<String> getDeletedFiles() {
        return deletedFiles;
    }
}
