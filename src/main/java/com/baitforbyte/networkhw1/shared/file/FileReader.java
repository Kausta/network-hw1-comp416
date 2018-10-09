package com.baitforbyte.networkhw1.shared.file;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public final class FileReader {
    private FileReader() {}

    public static FileTransmissionModel readAllBytes(String filename) throws IOException {
        byte[] bytes = Files.readAllBytes(FileSystems.getDefault().getPath(filename));
        return new FileTransmissionModel(bytes.length, bytes);
    }
 }
