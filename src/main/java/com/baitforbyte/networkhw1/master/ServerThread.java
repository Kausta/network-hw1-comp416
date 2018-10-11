package com.baitforbyte.networkhw1.master;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.file.master.IFileServerThread;

class ServerThread extends Thread {
    private final IFileServerThread fsThread;
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String line = "";
    private File directory;

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s, IFileServerThread fsThread) {
        this.s = s;
        this.fsThread = fsThread;
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
            line = is.readLine();
            while (line.compareTo("QUIT") != 0) {

                System.out.println("Client " + s.getRemoteSocketAddress() + " sent : " + line);
                line = is.readLine();
                if(line.startsWith("SEND")){
                    FileTransmissionModel f = fsThread.getModelFromPath(directory.toPath().toString(), line.substring(4));
                    fsThread.sendFile(f);
                }else if(line.startsWith("CORRECT")){
                    os.write("CORRECT");
                }else if(line.startsWith("SENDING")){
                    os.write("SEND");
                }else if(line.startsWith("HASH")){
                    HashMap<String, FileData> files = getLocalFiles();
                    os.write(files.size());
                }
            }
        } catch (IOException e) {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        } catch (NullPointerException e) {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed");
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
}
