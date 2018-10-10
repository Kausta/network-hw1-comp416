package com.baitforbyte.networkhw1.shared.file.master;

import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionException;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.follower.FileClient;

import java.io.IOException;
import java.net.Socket;

/**
 * Server thread handling file transfers
 */
public class FileServerThread extends IFileServerThread {
    private final Socket s;
    private final FileClient client;

    public FileServerThread(Socket s) {
        this.s = s;
        this.client = new FileClient(s);
    }

    public Socket getSocket() {
        return s;
    }

    @Override
    public void interrupt() {
        System.out.println("Closing file client connection");
        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();
    }

    @Override
    public FileTransmissionModel tryReceiveFile() throws IOException, FileTransmissionException {
        return client.tryReceiveFile();
    }

    @Override
    public void writeFile(FileTransmissionModel model) throws IOException, FileTransmissionException {
        client.writeFile(model);
    }

    @Override
    public FileTransmissionModel getModelFromPath(String directory, String filename) throws FileTransmissionException {
        return client.getModelFromPath(directory, filename);
    }
}
