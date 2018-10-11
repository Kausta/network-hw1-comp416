package com.baitforbyte.networkhw1.master;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.base.BaseServer;
import com.baitforbyte.networkhw1.shared.base.ConnectionException;
import com.baitforbyte.networkhw1.shared.file.master.FileServerThread;
import com.baitforbyte.networkhw1.shared.file.master.IFileServer;


public class Server extends BaseServer {
    private IFileServer fileServer;
    private File directory;

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port Server port
     */
    public Server(int port, IFileServer fileServer, String directoryPath) throws IOException {
        super(port);
        this.fileServer = fileServer;
        directory = new File(directoryPath);
    }

    /**
     * Listens to the line and starts a connection on receiving a request from the client
     * The connection is started and initiated as a ServerThread object
     */
    public void listenAndAccept() throws IOException {
        Socket s = getServerSocket().accept();
        System.out.println("A connection was established with a client on the address of " + s.getRemoteSocketAddress());

        // Get file server thread for this connection
        FileServerThread fsThread = fileServer.listenAndAccept();
        Socket fsSocket = fsThread.getSocket();

        if(!fsSocket.getInetAddress().equals(s.getInetAddress())) {
            // TODO: Solve this issue
            // TODO: Detect which file server thread is which file server's
            throw new ConnectionException("Different clients connected to server and file server, error");
        }

        ServerThread st = new ServerThread(s, fsThread);
        st.start();
    }

    /**
     * Gets local files in the designated folder
     * @return Hashmap of files, hashes and last changed times 
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public HashMap<String, FileData> getLocalFiles() throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = new HashMap<String, FileData>();
        for (File file : directory.listFiles()) {
            byte[] data = Files.readAllBytes(file.toPath());
            String name = file.getName();
            String hash = getHash(data);
            long time = file.lastModified();
            FileData fileData = new FileData(hash, time);
            files.put(name, fileData);
        }
        return files;
    }

    /**
     * Gets the hash of a file
     * @param file byte array of the file
     * @return hash as string
     * @throws NoSuchAlgorithmException
     */
    public String getHash(byte[] file) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest();
        return new String(Base64.getEncoder().encode(hash));
    }
}

