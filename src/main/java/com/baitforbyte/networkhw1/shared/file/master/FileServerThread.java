package com.baitforbyte.networkhw1.shared.file.master;

import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.follower.FileClient;

import java.io.IOException;
import java.net.Socket;

/**
 * Server thread handling file transfers
 */
public class FileServerThread extends IFileServerThread {
    private final Socket s;
    private final FileServer server;
    private final FileClient client;
    private final String identifier;

    public FileServerThread(Socket s, FileServer server) throws IOException {
        this.s = s;
        this.server = server;
        this.client = new FileClient(s);
        client.connect();
        this.identifier = client.getIdentifier();
        server.addClient(identifier, this);
    }

    public Socket getSocket() {
        return s;
    }

    @Override
    public void interrupt() {
        System.out.println("Closing file client connection");
        try {
            client.disconnect();
            server.removeClient(identifier);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }

    @Override
    public FileTransmissionModel tryReceiveFile() throws IOException {
        return client.tryReceiveFile();
    }

    @Override
    public void sendFile(FileTransmissionModel model) throws IOException {
        client.sendFile(model);
    }

    @Override
    public FileTransmissionModel getModelFromPath(String directory, String filename) throws IOException {
        return client.getModelFromPath(directory, filename);
    }

    @Override
    public void writeModelToPath(String directory, FileTransmissionModel model) throws IOException {
        client.writeModelToPath(directory, model);
    }
}
