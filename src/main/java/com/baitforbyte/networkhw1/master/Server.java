package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.shared.base.BaseServer;
import com.baitforbyte.networkhw1.shared.base.ConnectionException;
import com.baitforbyte.networkhw1.shared.file.master.FileServerThread;
import com.baitforbyte.networkhw1.shared.file.master.IFileServer;
import com.baitforbyte.networkhw1.shared.util.DirectoryUtils;

import java.io.IOException;
import java.net.Socket;


public class Server extends BaseServer {
    private IFileServer fileServer;
    private String directory;

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port Server port
     */
    public Server(int port, IFileServer fileServer) throws IOException {
        super(port);
        this.fileServer = fileServer;
        this.directory = DirectoryUtils.getDirectoryInDesktop("CloudDrive");
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

        if (!fsSocket.getInetAddress().equals(s.getInetAddress())) {
            // TODO: Solve this issue
            // TODO: Detect which file server thread is which file server's
            throw new ConnectionException("Different clients connected to server and file server, error");
        }

        ServerThread st = new ServerThread(s, fsThread, directory);
        st.start();
    }

}

