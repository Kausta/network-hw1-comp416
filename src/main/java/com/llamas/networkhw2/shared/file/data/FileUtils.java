package com.llamas.networkhw2.shared.file.data;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for reading and writing file data to files or network streams
 */
public final class FileUtils {
    private FileUtils() {
    }

    /**
     * Get file transmission models for the files in the directory
     *
     * @param directoryPath File directory
     * @return All the files in the directory
     * @throws NullPointerException if directory is null, not found or not a directory
     */
    public static FileTransmissionModel[] getAllFilesInDirectory(String directoryPath) throws FileTransmissionException {
        File directory = new File(directoryPath);
        // Directory should not be null
        Objects.requireNonNull(directory);
        if (!directory.isDirectory()) {
            throw new FileTransmissionException("Not a directory");
        }

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
    public static void writeToStream(ObjectOutputStream os, FileTransmissionModel model) throws FileTransmissionException {
        try {
            os.writeObject(model);
            os.flush();
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
    public static FileTransmissionModel readFromStream(ObjectInputStream is) throws FileTransmissionException {
        try {
            System.out.println("Reading from stream");
            Object object = is.readObject();
            if (object == null) {
                System.out.println("Got null closed");
                return null;
            }
            if (object instanceof FileTransmissionModel) {
                System.out.println("Got correct");
                return (FileTransmissionModel) object;
            }
            System.out.println("Oh no");
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

    /**
     * gets the path of a file
     *
     * @param directory directory of the file
     * @param filename  name of the file
     * @return the path of the file
     */
    public static Path getPath(String directory, String filename) {
        return FileSystems.getDefault().getPath(directory, filename);
    }

    /**
     * Reads the log file
     *
     * @param directory directory of the file
     * @param fileName  name of the file
     * @return a set which contains every line of the log file
     */
    public static Set<String> readLog(String directory, String fileName) {
        Set<String> files = new HashSet<>();
        try (Stream<String> stream = Files.lines(getPath(directory, fileName))) {
            files = stream.collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    /**
     * write to a log file
     *
     * @param files     the strings to be written to the log line by line
     * @param directory directory of the file
     * @param fileName  name of the file
     */
    public static void saveLog(Set<String> files, String directory, String fileName) {
        try (BufferedWriter writer = Files.newBufferedWriter(getPath(directory, fileName))) {
            for (String file : files) {
                writer.write(file + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete a file
     *
     * @param directory directory of the file
     * @param fileName  name of the file
     */
    public static void deleteFile(String directory, String fileName) {
        try {
            Files.delete(getPath(directory, fileName));
        } catch (Exception e) {
            System.out.println("File already deleted");
            // e.printStackTrace();
        }
    }
}
