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
    FileTransmissionModel tryReceiveFile() throws IOException, FileTransmissionException;

    /**
     * Writes the given model to the socket
     *
     * @param model File data to write
     * @throws IOException               Throws IOException if not connected
     * @throws FileTransmissionException Throws FileTransferException if cannot write file to the socket
     */
    void writeFile(FileTransmissionModel model) throws IOException, FileTransmissionException;

    /**
     * Gets FileTransmissionModel from given path and filename
     *
     * @param directory File Directory
     * @param filename  File Path
     * @return Read file data
     * @throws FileTransmissionException if cannot read the file data
     */
    FileTransmissionModel getModelFromPath(String directory, String filename) throws FileTransmissionException;
}
