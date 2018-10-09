package com.baitforbyte.networkhw1.shared.file;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * Utility class for reading and writing file data to files or network streams
 */
public final class FileUtils {
    private FileUtils() {
    }

    /**
     * Reads all bytes from the given file and records it in a FileTransmissionModel
     * @param directory File directory
     * @param filename File name
     * @return Bytes of the read file together with length and file name
     * @throws FileTransmissionException Thrown when an IOException occurs
     */
    public static FileTransmissionModel readAllBytes(String directory, String filename) throws FileTransmissionException {
        try {
            byte[] bytes = Files.readAllBytes(FileSystems.getDefault().getPath(directory, filename));
            return new FileTransmissionModel(filename, bytes.length, bytes);
        } catch (IOException ex) {
            throw new FileTransmissionException("Error occurred while reading file " + filename + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Write a FileTransmissionModel to an output stream
     * @param os Output stream
     * @param model File Data
     * @throws FileTransmissionException Thrown when an IOException occurs
     */
    public static void writeToStream(OutputStream os, FileTransmissionModel model) throws FileTransmissionException {
        try (ObjectOutputStream stream = new ObjectOutputStream(os)) {
            stream.writeObject(model);
        } catch (IOException ex) {
            throw new FileTransmissionException("Error occurred while writing file to the stream: " + ex.getMessage(), ex);
        }
    }

    /**
     * Tries to read a FileTransmissionModel from the given stream
     * @param is Stream to read from
     * @return Read file data
     * @throws FileTransmissionException Thrown when an IOException occurs or the read class is not FileTransmissionModel
     */
    public static FileTransmissionModel readFromStream(InputStream is) throws FileTransmissionException {
        try (ObjectInputStream stream = new ObjectInputStream(is)) {
            Object object = stream.readObject();
            if(object == null) {
                return null;
            }
            if(object instanceof FileTransmissionModel) {
                return (FileTransmissionModel) object;
            }
            throw new ClassNotFoundException("Unexpected class: " + object.getClass().getName());
        } catch (ClassNotFoundException ex) {
            throw new FileTransmissionException("Incorrect class found in the stream, expected FileTransmissionModel: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new FileTransmissionException("Error occurred while reading file from the stream: " + ex.getMessage(), ex);
        }
    }
}
