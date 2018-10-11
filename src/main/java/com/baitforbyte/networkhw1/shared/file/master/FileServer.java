package com.baitforbyte.networkhw1.shared.file.master;

import com.baitforbyte.networkhw1.shared.base.BaseServer;
import com.baitforbyte.networkhw1.shared.base.ConnectionException;

import java.io.IOException;
import java.net.Socket;

public class FileServer extends BaseServer implements IFileServer {
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
        FileServerThread fsThread = new FileServerThread(s);
        fsThread.start();
        return fsThread;
    }
}
