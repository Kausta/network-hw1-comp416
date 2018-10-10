package com.baitforbyte.networkhw1.shared.file.follower;

import com.baitforbyte.networkhw1.shared.base.BaseClient;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionException;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * Client handling file transfers
 */
public class FileClient extends BaseClient implements IFileClient {
    private InputStream is;
    private OutputStream os;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public FileClient(String address, int port) {
        super(address, port);
    }

    /**
     * Use the given socket as client socket
     *
     * @param s Socket to use as connection socket
     */
    public FileClient(Socket s) {
        super(s);
    }

    @Override
    public void connect() throws IOException {
        super.connect();
        Socket socket = getSocket();
        is = socket.getInputStream();
        os = socket.getOutputStream();
    }

    /**
     * Tries to receive file from the socket, returns null or a File Transmission Model
     *
     * @return Read File Transmission Model, or null
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot read file from the socket
     */
    @Override
    public FileTransmissionModel tryReceiveFile() throws IOException, FileTransmissionException {
        validateConnection();

        return FileUtils.readFromStream(this.is);
    }

    /**
     * Writes the given model to the socket
     *
     * @param model File data to write
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot write file to the socket
     */
    @Override
    public void writeFile(FileTransmissionModel model) throws IOException, FileTransmissionException {
        Objects.requireNonNull(model);
        validateConnection();

        FileUtils.writeToStream(this.os, model);
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
    public FileTransmissionModel getModelFromPath(String directory, String filename) throws FileTransmissionException {
        return FileUtils.readAllBytes(directory, filename);
    }

    @Override
    public void disconnect() throws IOException {
        try {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        } finally {
            super.disconnect();
        }
    }

    private void validateConnection() throws IOException {
        if (is == null || os == null) {
            throw new IOException("File client cannot be used before connecting first");
        }
    }
}
