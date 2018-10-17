package com.baitforbyte.networkhw1.shared.file.master;

import java.io.IOException;

public interface IFileServer {
    /**
     * Listens to new socket connections, accepts them and start them
     *
     * @return The newly opened socket connection
     * @throws IOException if there were an error opening the connection
     */
    FileServerThread listenAndAccept() throws IOException;

    /**
     * Get the file server thread with the given identifier
     *
     * @param identifier Identifier for the file server thread
     * @return File server thread
     */
    FileServerThread getFSThread(String identifier);
}
