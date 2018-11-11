package com.llamas.networkhw2.shared.file.master;

import com.llamas.networkhw2.shared.base.BaseServer;
import com.llamas.networkhw2.shared.base.ConnectionException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class FileServer extends BaseServer implements IFileServer {
    private HashMap<String, FileServerThread> fsRegistry = new HashMap<>();

    /**
     * Initiates a file server socket on the input port
     *
     * @param port Server port
     */
    public FileServer(int port) throws ConnectionException {
        super(port);
    }

    @Override
    public FileServerThread listenAndAccept() throws IOException {
        Socket s = this.getServerSocket().accept();
        System.out.println("A file connection was established with a client on the address of " + s.getRemoteSocketAddress());
        FileServerThread fsThread = new FileServerThread(s, this);
        fsThread.start();
        return fsThread;
    }

    @Override
    public FileServerThread getFSThread(String identifier) {
        return fsRegistry.get(identifier);
    }

    public void addClient(String identifier, FileServerThread fsThread) {
        fsRegistry.put(identifier, fsThread);
    }

    public void removeClient(String identifier) {
        fsRegistry.remove(identifier);
    }
}
