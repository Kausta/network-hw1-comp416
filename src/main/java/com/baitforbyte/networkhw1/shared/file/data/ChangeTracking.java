package com.baitforbyte.networkhw1.shared.file.data;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.util.ApplicationMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class ChangeTracking {


    private ChangeTracking() {
    }

    /**
     * Gets local files in the designated folder
     *
     * @return Hashmap of files, hashes and last changed times
     * @throws IOException              When a file reading exception occurs
     * @throws NoSuchAlgorithmException When hash function is not found, should not occur with the algorithms we use
     */
    public static HashMap<String, FileData> getLocalFiles(String directory) throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = new HashMap<>();
        FileTransmissionModel[] fileModels = FileUtils.getAllFilesInDirectory(directory);

        for (FileTransmissionModel file : fileModels) {
            if (!file.getFilename().endsWith(".veryspeciallog")) {
                files.put(file.getFilename(), new FileData(file.getHash(), file.getLastModifiedTimestamp()));
            }
        }
        return files;
    }

    // TODO: write docstring
    public static Set<String> getFilesToDelete(String directory) throws NoSuchAlgorithmException, IOException {
        Set<String> previousFiles = FileUtils.readLog(directory, Constants.PREV_FILES_LOG_NAME);
        for (String file : getLocalFileNames(directory)) {
            previousFiles.remove(file);
        }
        return previousFiles;
    }

    public static Set<String> getLocalFileNames(String directory) throws IOException, NoSuchAlgorithmException {
        Set<String> files = new HashSet<String>();
        FileTransmissionModel[] fileModels = FileUtils.getAllFilesInDirectory(directory);

        for (FileTransmissionModel file : fileModels) {
            if (!file.getFilename().endsWith(".veryspeciallog")) {
                files.add(file.getFilename());
            }
        }
        return files;
    }

    // TODO: write docstring
    public static Set<String> getChangedFiles(String directory) throws NoSuchAlgorithmException, IOException {
        Set<String> prevFiles = FileUtils.readLog(directory, Constants.CHANGE_FILES_LOG_NAME);
        HashMap<String, FileData> locals = getLocalFiles(directory);
        Set<String> changedFiles = new HashSet<>();
        for (String file : prevFiles) {
            String[] data = file.split("-");
            if (locals.containsKey(data[0])) {
                String localHash = locals.get(data[0]).getHash();
                String oldHash = data[1];
                if (!oldHash.equals(localHash)) {
                    changedFiles.add(data[0]);
                }
            }
        }
        return changedFiles;
    }


    // TODO: write docstring
    public static Set<String> getAddedFiles(String directory) throws NoSuchAlgorithmException, IOException {
        Set<String> previousFiles = FileUtils.readLog(directory, Constants.PREV_FILES_LOG_NAME);
        Set<String> files = getLocalFileNames(directory);
        for (String file : previousFiles) {
            files.remove(file);
        }
        return files;
    }

    public static void createLogFiles(String directory, ApplicationMode mode) throws IOException {
        String[] names = mode == ApplicationMode.MASTER ? Constants.SERVER_FILES : Constants.CLIENT_FILES;
        for (String name : names) {
            File logFile = new File(directory, name);
            if (!logFile.exists()) {
                if (!logFile.createNewFile()) {
                    System.out.println("Cannot create log file " + name);
                }
                try {
                    Path path = Paths.get(directory, name);
                    Boolean hidden = (Boolean) Files.getAttribute(path, "dos:hidden", LinkOption.NOFOLLOW_LINKS);
                    if (hidden != null && !hidden) {
                        Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
    }

}
