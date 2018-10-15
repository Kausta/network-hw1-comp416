package com.baitforbyte.networkhw1.master;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.file.data.ChangeTracking;
import com.baitforbyte.networkhw1.shared.file.data.Constants;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.file.master.IFileServerThread;

class ServerThread extends Thread {


    private final IFileServerThread fsThread;
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String line = "";
    private String directory;

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s, IFileServerThread fsThread, String directory) {
        this.s = s;
        this.fsThread = fsThread;
        this.directory = directory;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run() {
        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

        } catch (IOException e) {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try {
            while (line.compareTo("QUIT") != 0) {
                line = is.readLine();
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent : " + line);
                if (line.startsWith("SENDFILE")) {
                    FileTransmissionModel f = fsThread.getModelFromPath(directory, line.substring(8));
                    fsThread.sendFile(f);
                    sendToClient(f.getHash());
                } else if (line.startsWith("CORRECT")) {
                    sendToClient("CORRECT");
                } else if (line.startsWith("SENDING")) {
                    sendToClient("SEND");
                    FileTransmissionModel f = fsThread.tryReceiveFile();
                    sendToClient(f.getHash());
                    String answer = is.readLine();
                    if (answer.equals("CORRECT")) {
                        sendToClient("CORRECT");
                        fsThread.writeModelToPath(directory, f);
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
                    Set<String> filesToDelete = ChangeTracking.getFilesToDelete(directory);
                    sendToClient("SENDING");
                    String response = "";
                    for (String file : filesToDelete) {
                        while (!response.equals("DELETED")){
                            sendToClient(file);
                            response = is.readLine();                            
                        }
                        
                    }
                }
                FileUtils.saveLog(ChangeTracking.getLocalFileNames(directory), directory, Constants.PREV_FILES_LOG_NAME);
                Set<String> localHashes = new HashSet<>();
                for (String file : ChangeTracking.getLocalFileNames(directory)) {
                    localHashes.add(file + "-" + getLocalFiles().get(file).getHash());
                }
                FileUtils.saveLog(localHashes, directory, Constants.CHANGE_FILES_LOG_NAME);
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
                if (fsThread != null) {
                    fsThread.interrupt();
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
        HashMap<String, FileData> files = new HashMap<>();
        FileTransmissionModel[] fileModels = FileUtils.getAllFilesInDirectory(directory);

        for (FileTransmissionModel file : fileModels) {
            files.put(file.getFilename(), new FileData(file.getHash(), file.getLastModifiedTimestamp()));
        }
        return files;
    }

    // TODO: write docstring
    private void sendToClient(String s) {
        System.out.println("Send " + s);
        os.write(s + "\n");
        os.flush();
    }


    

}
