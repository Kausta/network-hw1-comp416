package com.llamas.networkhw2.shared.file.master;

import java.io.IOException;

public interface IFileServer {
    FileServerThread listenAndAccept() throws IOException;

    FileServerThread getFSThread(String identifier);
}
