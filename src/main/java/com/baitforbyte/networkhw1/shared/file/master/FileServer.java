package com.baitforbyte.networkhw1.shared.file.master;

import com.baitforbyte.networkhw1.shared.base.BaseServer;
import com.baitforbyte.networkhw1.shared.base.ConnectionException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * Class for handling file server connections and starting threads for them
 *
 * Also holds the identifier, file server thread relation
 */
public class FileServer extends BaseServer implements IFileServer {
    /**
     * Identifier <-> File Server Thread map
     */
    private HashMap<String, FileServerThread> fsRegistry = new HashMap<>();

    /**
     * Initiates a file server socket on the input port
     *
     * @param port Server port
     */
    public FileServer(int port) throws ConnectionException {
        super(port);
    }

    /**
     * Listens to new socket connections, accepts them and start them
     * @return The newly opened socket connection
     * @throws IOException if there were an error opening the connection
     */
    @Override
    public FileServerThread listenAndAccept() throws IOException {
        Socket s = this.getServerSocket().accept();
        System.out.println("A file connection was established with a client on the address of " + s.getRemoteSocketAddress());
        FileServerThread fsThread = new FileServerThread(s, this);
        fsThread.start();
        return fsThread;
    }

    /**
     * Get the file server thread with the given identifier
     * @param identifier Identifier for the file server thread
     * @return File server thread
     */
    @Override
    public FileServerThread getFSThread(String identifier) {
        return fsRegistry.get(identifier);
    }

    /**
     * Registers a file client with the given identifier
     * @param identifier Identifier for the client
     * @param fsThread Thread for the client
     */
    public void addClient(String identifier, FileServerThread fsThread) {
        fsRegistry.put(identifier, fsThread);
    }

    /**
     * Removes the file client with the given identifier
     * @param identifier Identifier for the client
     */
    public void removeClient(String identifier) {
        fsRegistry.remove(identifier);

    }
}
