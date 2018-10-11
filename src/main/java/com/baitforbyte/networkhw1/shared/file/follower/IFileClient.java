package com.baitforbyte.networkhw1.shared.file.follower;

import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionException;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;

import java.io.IOException;

public interface IFileClient {
    /**
     * Tries to receive file from the socket, returns null or a File Transmission Model
     *
     * @return Read File Transmission Model, or null
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot read file from the socket
     */
    FileTransmissionModel tryReceiveFile() throws IOException;

    /**
     * Writes the given model to the socket
     *
     * @param model File data to write
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot write file to the socket
     */
    void sendFile(FileTransmissionModel model) throws IOException;

    /**
     * Gets FileTransmissionModel from given path and filename
     *
     * @param directory File Directory
     * @param filename  File Path
     * @return Read file data
     * @throws FileTransmissionException if cannot read the file data
     */
    FileTransmissionModel getModelFromPath(String directory, String filename) throws IOException;

    /**
     * Write the FileTransmissionModel to the given directory
     *
     * @param directory File Directory
     * @param model     File Model containing bytes and filename
     * @throws FileTransmissionException if cannot read the file data
     * @throws NullPointerException      if the model is null
     */
    public void writeModelToPath(String directory, FileTransmissionModel model) throws IOException;
}
