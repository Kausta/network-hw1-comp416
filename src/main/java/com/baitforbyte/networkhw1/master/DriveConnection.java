package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.ApplicationConfiguration;
import com.baitforbyte.networkhw1.shared.util.DirectoryUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.collect.ImmutableMap;

import java.io.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class DriveConnection {

    // TODO: Google Doc'lar için file extensionları çoğalt
    // TODO: Hash'ler uyuşmuyor
    // TODO: Update function geliştirilebilir?
    // TODO: Update function text
    // FilePath


    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // TODO: Token ve credential path'ini degistirebiliyor muyuz?
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String APPLICATION_FOLDER_PATH = DirectoryUtils.getDirectoryInDesktop("DriveCloud");
    /**
     * Global constant of the scope used by application
     * <p>
     * We are using "DRIVE" scope, because it gives the permission access
     * of all of the user's files with the write and delete options.
     * <p>
     * IMPORTANT NOTE: If scope is modified, previously saved tokens/ folder must be deleted.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final Map<String, String> fileExtensionMap = ImmutableMap.<String, String>builder()
            .put("html", "text/html")
            .put("txt", "text/plain")
            .put("pdf", "application/pdf")
            .put("doc", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .put("csv", "text/csv")
            .put("tsv", "text/tab-separated-values")
            .put("jpeg", "image/jpeg")
            .put("png", "image/png")
            .put("svg", "image/svg+xml")
            .put("ppt", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            .put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
            .put("json", "application/vnd.google-apps.script+json")
            .build();
    private static final Map<String, String> fileMimeTypeMapDownload = ImmutableMap.<String, String>builder()
            .put("application/vnd.google-apps.document", "application/vnd.oasis.opendocument.text")
            .put("application/vnd.google-apps.spreadsheet", "application/x-vnd.oasis.opendocument.spreadsheet")
            .put("application/vnd.google-apps.presentation", "application/vnd.oasis.opendocument.presentation")
            .build();
    private static final Map<String, String> fileExtensionMapForGoogleDocs = ImmutableMap.<String, String>builder()
            .put("application/vnd.google-apps.document", ".docx")
            .put("application/vnd.google-apps.spreadsheet", ".xlsx")
            .put("application/vnd.google-apps.presentation", ".pptx")
            .build();
    private static Drive service;
    private static Map<String, String> fileIdMap = new HashMap<String, String>();
    private static Map<String, String> fileMimeMap = new HashMap<String, String>();
    private static Map<String, DateTime> changeMap = new HashMap<String, DateTime>();
    private static Map<String, DateTime> tmpChangeMap = new HashMap<String, DateTime>();
    private static Set<String> changeLog = new HashSet<String>();
    private final String APPLICATION_NAME = "DriveCloud - Bait for Byte";
    private String folderID;
    private String rootFolderID;
    private String pageToken;
    private Boolean changed = false;

    /**
     * Class constructor for DriveConnection class
     * It initialize the Drive object called service to operate all functionality of the application
     */
    public DriveConnection() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(this.APPLICATION_NAME)
                .build();
    }

    /**
     * IMPORTANT NOTE: This method is copied from Google Drive API documentation
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveConnection.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public void checkFolderIsExist() throws IOException {
        FileList response = service.files().list()
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name, parents, mimeType)")
                .execute();
        List<File> fileList = response.getFiles();
        Boolean found = false;
        for (File file : fileList) {
            if (file.getName().equals("DriveCloud")
                    && file.getMimeType().equals("application/vnd.google-apps.folder")) {
                found = true;
                folderID = file.getId();
                for (String p : file.getParents()) {
                    rootFolderID = p;
                }
                break;
            }
        }
        System.out.println("================================\n");
        if (found) {
            System.out.println("DriveCloud folder is exist, so the new one is not gonna be created");
        } else {
            System.out.println("DriveCloud folder is not exist, so the new one is being created");
            File fileMetadata = new File();
            fileMetadata.setName("DriveCloud");
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            folderID = file.getId();
            FileList res = service.files().list()
                    .setPageSize(1000)
                    .setFields("nextPageToken, files(id, name, parents, mimeType)")
                    .execute();
            List<File> fList = res.getFiles();
            for (File f : fList) {
                if (f.getName().equals("DriveCloud")
                        && f.getMimeType().equals("application/vnd.google-apps.folder")) {
                    for (String p : f.getParents()) {
                        rootFolderID = p;
                    }
                    break;
                }
            }
            System.out.println("DriveCloud folder is created with the ID " + folderID);
        }
        System.out.println("\n================================");
    }

    public Boolean checkFileInFolder(String parentID) throws IOException {
        // System.out.println(parentID + " " + folderID);
        if (parentID.equals(folderID)) {
            return true;
        } else
            return false;
    /*
    else if (parentID.equals(rootFolderID)) {
      return false;
    }
    else {
      String newParentID = rootFolderID;
      FileList res = service.files().list()
      .setPageSize(1000)
      .setFields("nextPageToken, files(id, parents, mimeType)")
      .execute();
      List<File> fList = res.getFiles();
      for(File f: fList) {
        if (f.getParents() == null)
          return false;
        if (f.getId().equals(parentID)
              && f.getMimeType().equals("application/vnd.google-apps.folder")) {
          for(String p: f.getParents()) {
            newParentID = p;
          }
          break;
        }
      }
      return checkFileInFolder(newParentID);
    }*/
    }

    public void initializeChangeMap() throws IOException {
        FileList response = service.files().list()
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name, parents, trashed, modifiedTime)")
                .execute();
        List<File> fileList = response.getFiles();
        for (File file : fileList) {
            String parentID = file.getId();
            if (file.getParents() != null && !file.getTrashed()) {
                for (String p : file.getParents()) {
                    parentID = p;
                }
                if (checkFileInFolder(parentID)) {
                    changeMap.put(file.getName(), file.getModifiedTime());
                }
            }
        }
    }

    public void deleteLocalFile(String fileName) {
        java.io.File file = new java.io.File(getFilePath(fileName));
        file.delete();
        System.out.println("Local file " + fileName + " has been deleted.\n");
    }

    public void addChangeLog(String fileName) {
        changeLog.add(fileName);
    }

    public void detectChanges() throws IOException {
        FileList response = service.files().list()
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name, parents, trashed, modifiedTime)")
                .execute();
        List<File> fileList = response.getFiles();
        for (File file : fileList) {
            String parentID = file.getId();
            if (file.getParents() != null && !file.getTrashed()) {
                for (String p : file.getParents()) {
                    parentID = p;
                }
                if (checkFileInFolder(parentID)) {
                    tmpChangeMap.put(file.getName(), file.getModifiedTime());
                }
            }
        }
        for (String s : changeMap.keySet()) {
            if (!changeLog.contains(s)) {
                if (tmpChangeMap.get(s) == null) {
                    System.out.println("Change detected!");
                    System.out.println("File " + s + " has been deleted from Google Drive.");
                    changed = true;
                    deleteLocalFile(s);
                } else {
                    if (!tmpChangeMap.get(s).equals(changeMap.get(s))) {
                        System.out.println("Change detected!");
                        System.out.println("File " + s + " has been modified in Google Drive.");
                        System.out.println("Local file will be updated.");
                        downloadFile(s);
                        System.out.println("File " + s + " has been updated at local folder.\n");
                        changed = true;
                    }
                }
            }
        }
        for (String s : tmpChangeMap.keySet()) {
            if (!changeLog.contains(s)) {
                if (changeMap.get(s) == null) {
                    System.out.println("Change detected!");
                    System.out.println("File " + s + " has been added to Google Drive");
                    System.out.println("It will be downloaded to local folder.");
                    downloadFile(s);
                    System.out.println("File " + s + " has been downloaded to local folder.\n");
                    changed = true;
                }
            }
        }
        changeMap.clear();
        for (String s : tmpChangeMap.keySet()) {
            changeMap.put(s, tmpChangeMap.get(s));
        }
        tmpChangeMap.clear();
        changeLog.clear();
    }

    public Boolean isChanged() {
        return this.changed;
    }

    public void setChanged(Boolean b) {
        this.changed = b;
    }

    public List<File> getFileList() throws IOException {
        // setPageSize's default value is 100, max value is 1000
        // Its value must be 1000 in production
        FileList response = service.files().list()
                .setPageSize(100)
                .setFields("nextPageToken, files(id, name, parents, trashed, mimeType, modifiedTime, md5Checksum)")
                .execute();
        List<File> fileList = response.getFiles();
        // trash'deki şeyleri de alalım mı buraya
        for (File file : fileList) {
            String parentID = file.getId();
            if (file.getParents() != null && !file.getTrashed()) {
                // System.out.println(file.getName());
                for (String p : file.getParents()) {
                    parentID = p;
                }
                if (checkFileInFolder(parentID)) {
                    fileIdMap.put(file.getName(), file.getId());
                    fileMimeMap.put(file.getName(), file.getMimeType());
                    tmpChangeMap.put(file.getName(), file.getModifiedTime());
                }
            }
        }
        return fileList;
    }

    public HashMap<String, FileData> getFileDataMap() throws IOException, NoSuchAlgorithmException {
        FileList response = service.files().list()
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name, parents, mimeType, modifiedTime, md5Checksum)")
                .execute();
        HashMap<String, FileData> fileMap = new HashMap<String, FileData>();
        // trash'deki şeyleri de alalım mı buraya
        for (File file : response.getFiles()) {
            String parentID = file.getId();
            if (file.getParents() != null) {
                for (String p : file.getParents()) {
                    parentID = p;
                }
                if (checkFileInFolder(parentID)) {
                    DateTime tmpDateTime = file.getModifiedTime();
                    System.out.println("=======");
                    final MessageDigest md = MessageDigest.getInstance(ApplicationConfiguration.getInstance().getFileHashType());
                    System.out.println(file.getMd5Checksum());
                    byte[] b = file.getMd5Checksum().getBytes();
                    System.out.println(Base64.getEncoder().encodeToString(b));
                    FileData tmpFile = new FileData(Base64.getEncoder().encodeToString(b), tmpDateTime.getValue());
                    fileMap.put(file.getName(), tmpFile);
                }
            }
        }
        return fileMap;
    }

    public void uploadFile(final String fileName) throws IOException {
        java.io.File uploadFile = new java.io.File(getFilePath(fileName));
        AbstractInputStreamContent uploadContent = new FileContent(fileExtensionMap.get(getFileExtension(fileName)), uploadFile);
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderID));
        File file = service.files().create(fileMetadata, uploadContent)
                .setFields("id, webContentLink, webViewLink").execute();
        //System.out.println(fileName + " is uploaded!");
        //System.out.println("WebContentLink: " + file.getWebContentLink());
        //System.out.println("WebViewLink: " + file.getWebViewLink());
    }

    public void downloadFile(final String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileId = getFileId(fileName);
        // TODO: Download message yazılmalı.
        String mimeType = fileMimeMap.get(fileName);
        if (mimeType.equals("application/vnd.google-apps.folder")) {
            System.out.println("You are trying to download a folder, it is not allowed!");
        } else {
            if (mimeType.contains("google-apps")) {
                service.files().export(fileId, fileMimeTypeMapDownload.get(mimeType))
                        .executeMediaAndDownloadTo(outputStream);
            } else {
                service.files().get(fileId)
                        .executeMediaAndDownloadTo(outputStream);
            }
            String filePath = getFilePath(fileName);
            if (mimeType.contains("google-apps")) {
                filePath += fileExtensionMapForGoogleDocs.get(mimeType);
            }
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(outputStream.toByteArray());
            fos.close();
        }
    }

    public void updateFile(final String fileName) throws IOException {
        deleteFile(fileName);
        uploadFile(fileName);
    }

    public void deleteFile(final String fileName) throws IOException {
        service.files().delete(getFileId(fileName)).execute();
    }

    /**
     * Returns the file path for the file which will be uploaded.
     *
     * @param fileName Name of the file
     * @return File path of the file
     */
    private String getFilePath(final String fileName) {
        return Paths.get(APPLICATION_FOLDER_PATH, fileName).toString();
    }

    private String getFileExtension(final String fileName) {
        for (int i = fileName.length() - 1; i >= 0; i--) {
            if (fileName.charAt(i) == '.') {
                return fileName.substring(i + 1);
            }
        }
        return fileName;
    }

    private String getFileId(final String fileName) throws IOException {
        if (fileIdMap.isEmpty())
            getFileList();
        return fileIdMap.get(fileName);
    }

}
