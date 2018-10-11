package com.baitforbyte.networkhw1.shared.file.master;

import java.io.IOException;

public interface IFileServer {
    public FileServerThread listenAndAccept() throws IOException;
}
