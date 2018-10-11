package com.baitforbyte.networkhw1.shared.file.data;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utility class for reading and writing file data to files or network streams
 */
public final class FileUtils {
    private FileUtils() {
    }

    /**
     * Get file transmission models for the files in the directory
     *
     * @param directory File directory
     * @return All the files in the directory
     * @throws NullPointerException if directory is null, not found or not a directory
     */
    public static FileTransmissionModel[] getAllFilesInDirectory(File directory) throws FileTransmissionException {
        // Directory should not be null
        Objects.requireNonNull(directory);
        final String directoryPath = directory.getPath();

        final File[] files = directory.listFiles();
        // Files should not be null
        Objects.requireNonNull(files);

        final FileTransmissionModel[] models = new FileTransmissionModel[files.length];
        for (int i = 0; i < files.length; i++) {
            models[i] = readAllBytes(directoryPath, files[i].getName());
        }
        return models;
    }

    /**
     * Reads all bytes from the given file and records it in a FileTransmissionModel
     *
     * @param directory File directory
     * @param filename  File name
     * @return Bytes of the read file together with length and file name
     * @throws FileTransmissionException Thrown when an IOException occurs
     */
    public static FileTransmissionModel readAllBytes(String directory, String filename) throws FileTransmissionException {
        try {
            final Path path = getPath(directory, filename);
            byte[] bytes = Files.readAllBytes(path);
            long timestamp = Files.getLastModifiedTime(path).toMillis();
            return new FileTransmissionModel(filename, bytes.length, bytes, timestamp);
        } catch (IOException ex) {
            throw new FileTransmissionException("Error occurred while reading file " + filename + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * Write a FileTransmissionModel to an output stream
     *
     * @param os    Output stream
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
     *
     * @param is Stream to read from
     * @return Read file data
     * @throws FileTransmissionException Thrown when an IOException occurs or the read class is not FileTransmissionModel
     */
    public static FileTransmissionModel readFromStream(InputStream is) throws FileTransmissionException {
        try (ObjectInputStream stream = new ObjectInputStream(is)) {
            Object object = stream.readObject();
            if (object == null) {
                return null;
            }
            if (object instanceof FileTransmissionModel) {
                return (FileTransmissionModel) object;
            }
            throw new ClassNotFoundException("Unexpected class: " + object.getClass().getName());
        } catch (ClassNotFoundException ex) {
            throw new FileTransmissionException("Incorrect class found in the stream, expected FileTransmissionModel: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new FileTransmissionException("Error occurred while reading file from the stream: " + ex.getMessage(), ex);
        }
    }

    /**
     * Writes given model in the directory
     *
     * @param directory Directory write set the file in
     * @param model     File transmission model
     * @throws NullPointerException if the given model is null
     */
    public static void writeAllBytes(String directory, FileTransmissionModel model) throws FileTransmissionException {
        Objects.requireNonNull(model);

        Path path = getPath(directory, model.getFilename());
        try {
            Files.write(path,
                    model.getContent(),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new FileTransmissionException("Cannot save the file!", e);
        }
    }

    private static Path getPath(String directory, String filename) {
        return FileSystems.getDefault().getPath(directory, filename);
    }
}
