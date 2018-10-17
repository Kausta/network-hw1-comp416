package com.baitforbyte.networkhw1.shared.file.master;

import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionException;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.follower.FileClient;

import java.io.IOException;
import java.net.Socket;

/**
 * Server thread handling file transfers
 * Directly uses the methods from the FileClient, so this is only a thin wrapper around it preventing opening a new Socket
 * and using the given socket
 * It also extends Thread, and used as File Server Connection Threads, and hence it contains a FileClient instead of extending it
 */
public class FileServerThread extends IFileServerThread {
    /**
     * Connection socket
     */
    private final Socket s;
    /**
     * File server instance
     */
    private final FileServer server;
    /**
     * Internal file client, this class uses File Client directly instead of implementing the same things
     */
    private final FileClient client;
    /**
     * Unique (ip+port) identifier for the client
     */
    private final String identifier;

    /**
     * Initiate the file server thread with the given socket and file server
     *
     * It only requires file server instance to add the client together with its identifier or remove it
     * @param s Socket that was opened by file server
     * @param server File server
     * @throws IOException If there were a problem getting the identifier
     */
    public FileServerThread(Socket s, FileServer server) throws IOException {
        this.s = s;
        this.server = server;
        this.client = new FileClient(s);
        client.connect();
        this.identifier = client.getIdentifier();
        server.addClient(identifier, this);
    }

    /**
     * Called on thread interruption
     */
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

    /**
     * Tries to receive file from the socket, returns null or a File Transmission Model
     *
     * @return Read File Transmission Model, or null
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot read file from the socket
     */
    @Override
    public FileTransmissionModel tryReceiveFile() throws IOException {
        return client.tryReceiveFile();
    }

    /**
     * Writes the given model to the socket
     *
     * @param model File data to write
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot write file to the socket
     * @throws NullPointerException      Throws NullPointerException if the model is null
     */
    @Override
    public void sendFile(FileTransmissionModel model) throws IOException {
        client.sendFile(model);
    }

    /**
     * Gets FileTransmissionModel from given path and filename
     *
     * @param directory File Directory
     * @param filename  File Path
     * @return Read file data
     * @throws FileTransmissionException if cannot read the file data
     */
    @Override
    public FileTransmissionModel getModelFromPath(String directory, String filename) throws IOException {
        return client.getModelFromPath(directory, filename);
    }

    /**
     * Write the FileTransmissionModel to the given directory
     *
     * @param directory File Directory
     * @param model     File Model containing bytes and filename
     * @throws FileTransmissionException if cannot read the file data
     * @throws NullPointerException      if the model is null
     */
    @Override
    public void writeModelToPath(String directory, FileTransmissionModel model) throws IOException {
        client.writeModelToPath(directory, model);
    }
}
