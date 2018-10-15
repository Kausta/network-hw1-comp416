package com.baitforbyte.networkhw1.shared.file.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
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
            Object object = is.readObject();
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

    // TODO: write docstring
    private static Path getPath(String directory, String filename) {
        return FileSystems.getDefault().getPath(directory, filename);
    }

    // TODO: write docstring
    public static List<String> readPreviousFiles(String fileName){
        List<String> files = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))){
            files = stream.collect(Collectors.toList());
        }catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    // TODO: write docstring
    public static void savePreviousFiles(Set<String> files, String fileName){
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
            for (String file : files) {
                writer.write(file + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: write docstring
    public static void deleteFile(String fileName){
        try {
            Files.delete(Paths.get(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
