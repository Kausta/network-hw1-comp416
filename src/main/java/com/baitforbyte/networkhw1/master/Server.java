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
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
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

    // TODO: Maybe should be in server thread ?
    /**
     * Gets local files in the designated folder
     *
     * @return Hashmap of files, hashes and last changed times
     * @throws IOException When a file reading exception occurs
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

