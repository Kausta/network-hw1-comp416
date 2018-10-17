package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.ApplicationConfiguration;
import com.baitforbyte.networkhw1.shared.file.data.Constants;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.util.ApplicationMode;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DriveConnection {

    /**
     * It is a constant which is used while creating a Drive object (which provides all connection between GoogleDrive and master)
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    /**
     * Path constants for tokens, credentials and application folder (which is DriveCloud folder in Desktop)
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String APPLICATION_FOLDER_PATH = DirectoryUtils.getDirectoryInDesktop("DriveCloud", ApplicationMode.MASTER);
    
    /**
     * Global constant of the scope used by application
     * We are using "DRIVE" scope, because it gives the permission access
     * of all of the user's files with the write and delete options.
     * IMPORTANT NOTE: If scope is modified, previously saved tokens/ folder must be deleted.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    /**
     * HashMap for easily accessing when Google Drive wants to MimeType of the file
     */
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

    /**
     * Google Drive uses a very different Mime Type hierarchy on Google Drive's own file types
     * It provides a Mime type while listing the file and wants a different MimeType while downloading that file
     * So, we use a HashMap for easily accessing when Google Drive wants that MimeTypes 
     */
    private static final Map<String, String> fileMimeTypeMapDownload = ImmutableMap.<String, String>builder()
            .put("application/vnd.google-apps.document", "application/vnd.oasis.opendocument.text")
            .put("application/vnd.google-apps.spreadsheet", "application/x-vnd.oasis.opendocument.spreadsheet")
            .put("application/vnd.google-apps.presentation", "application/vnd.oasis.opendocument.presentation")
            .build();

    /**
     * Google Drive's own file types don't have a file extension, so for Windows users, we need to provide
     * a file extension while downlaoding those files to local folder. For this reason, we use this
     * HashMap for easily accessing some file extension-mime type pairs
     */
    private static final Map<String, String> fileExtensionMapForGoogleDocs = ImmutableMap.<String, String>builder()
            .put("application/vnd.google-apps.document", ".docx")
            .put("application/vnd.google-apps.spreadsheet", ".xlsx")
            .put("application/vnd.google-apps.presentation", ".pptx")
            .build();

    /**
     * Drive object which basically provides all the connection between Google Drive and application
     */
    private static Drive service;

    /**
     * Files' id and Mime type maps container
     * These are filled while getting list of the files in proper functions
     */
    private static Map<String, String> fileIdMap = new HashMap<String, String>();
    private static Map<String, String> fileMimeMap = new HashMap<String, String>();

    /**
     * Two container used while detecting the changes on Google Drive
     * One of them contains the files on last cycles,
     * other one contains the files on current cycles on Google Drive
     * End of the cycles, pairs in tmpChangeMap are transferred to changeMap
     */
    private static Map<String, DateTime> changeMap = new HashMap<String, DateTime>();
    private static Map<String, DateTime> tmpChangeMap = new HashMap<String, DateTime>();

    /**
     * Set used for tracking the changes local to Google Drive
     */
    private static Set<String> changeLog = new HashSet<String>();

    /**
     * Application name constant which is used while initializing the Drive object 'service'
     */
    private final String APPLICATION_NAME = "DriveCloud - Bait for Byte";

    /**
     * Variables used for containing the DriveCloud folder ID and the root folder ID in Google Drive account
     */
    private String folderID;
    private String rootFolderID;

    /**
     * Variable used for detecting whether there was a change on last cycle
     */
    private Boolean changed = false;

    /**
     * Class constructor for DriveConnection class
     * It initialize the Drive object called service to operate all functionality of the application
     * @throws IOException if GoogleNetHttpTransport can't provide a proper NetHttpTransport
     * @throws GeneralSecurityException if GoogleNetHttpTransport can't provide a proper NetHttpTransport
     */
    public DriveConnection() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(this.APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object.
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

    /**
     * Checks whether DriveCloud folder is exist on Google Drive
     * First, it gets the list of file in Google Drive account (folders are considered as files in Google Drive)
     * Then, necessary filtering is done and check the DriveCloud folder is exists on Google Drive
     * If doesn't exist, then create a folder named "DriveCloud"
     * @throws IOException If service return a null object.
     */
    public void checkFolderIsExist() throws IOException {
        FileList response = service.files().list()
                .setPageSize(1000)
                .setFields("nextPageToken, files(id, name, trashed, parents, mimeType)")
                .execute();
        List<File> fileList = response.getFiles();
        Boolean found = false;
        for (File file : fileList) {
            if (file.getName().equals("DriveCloud")
                    && file.getMimeType().equals("application/vnd.google-apps.folder")
                    && !file.getTrashed()) {
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

    /**
     * Check whether the file with the given Parent ID is in DriveCloud folder
     * @param parentID Parent ID of the file which is getting checked whether it is inside the DriveCloud folder or not
     * @return A boolean whether the file with the given Parent ID is in DriveCloud folder
     */
    public Boolean checkFileInFolder(String parentID) {
        if (parentID.equals(folderID)) {
            return true;
        } else
            return false;
    }

    /**
     * Initializing changeMap at the start of the application to keep track of files on Google Drive
     * @throws IOException when service returns a null object
     */
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

    /**
     * Delete the given file in local
     * @param fileName filename of the file which will be deleted
     */
    public void deleteLocalFile(String fileName) {
        java.io.File file = new java.io.File(getFilePath(fileName));
        file.delete();
        System.out.println("Local file " + fileName + " has been deleted.\n");
    }

    /**
     * It is a part of the program to keep track of the changes
     * Adding given file to changeLog
     * @param fileName name of the file which will be added to changeLog
     */
    public void addChangeLog(String fileName) {
        changeLog.add(fileName);
    }

    /**
     * Detect changes in Google Drive files(add, delete, update) and synchronize them with the local files
     * First, it takes the current file list and filter them depends on whether they are in DriveCloud folder
     * Then, it puts the proper files into tmpChangeMap
     * Finally, it compares changeMap (files at one cycles before) and tmpChangeMap (current files) and syncronize Google Drive with local folder.
     * @throws IOException if service returns a null object
     */
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
            if(!changeLog.contains(s)) {
                if (tmpChangeMap.get(s) == null) {
                    System.out.println("Change detected!");
                    System.out.println("File " + s + " has been deleted from Google Drive.");
                    changed = true;
                    deleteLocalFile(s);
                } else {
                    if (!tmpChangeMap.get(s).equals(changeMap.get(s))) {
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
        }
        for (String s : tmpChangeMap.keySet()) {
            if(!changeLog.contains(s)) {
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
        String directory = DirectoryUtils.getDirectoryInDesktop(ApplicationConfiguration.getInstance().getFolderName(), ApplicationMode.MASTER);
        Set<String> files = new HashSet<String>();
        for (String s: changeMap.keySet()) {
            DateTime d = changeMap.get(s);
            files.add(s + "<><>" + d + "\r\n");
        }
        FileUtils.saveLog(files, directory, Constants.DRIVE_CHANGE_LOG_NAME);

        updateChangeMap();
        tmpChangeMap.clear();
        changeLog.clear();    
    }

    /**
     * Update changeMap with the data coming from Google Drive API
     * First, it clears the changeMap and gets the file list from Google Drive.
     * Then, filter the files depends on whether they are in DriveCloud folder.
     * Finally, it adds the proper files to changeMap.
     * @throws IOException if service returns a null object
     */
    public void updateChangeMap() throws IOException {
        changeMap.clear();
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

    /**
     * Read the local Google Drive Change Log and update the changeMap
     * This method is only used at the start to check whether
     * there was a change while the master is closed.
     * @throws IOException if there is an error while reading the local change-log file
     */
    public void readChangeMap() throws IOException {
        changeMap.clear();
        String directory = DirectoryUtils.getDirectoryInDesktop(ApplicationConfiguration.getInstance().getFolderName(), ApplicationMode.MASTER);
        Map<String, DateTime> stream = Files.lines(FileUtils.getPath(directory, Constants.DRIVE_CHANGE_LOG_NAME))
            .filter(x -> !x.isEmpty())
            .map(line -> line.split("<><>"))
            .collect(Collectors.toMap(arr -> arr[0], arr->new DateTime(arr[1])));
        for(String s: stream.keySet()) {
            changeMap.put(s, stream.get(s));
        }
    }

    /**
     * Returns the value of the boolean 'changed'
     * This method is used for detecting whether any change happens in a cycle.
     * @return the value of the boolean 'changed'
     */
    public Boolean isChanged() {
        return this.changed;
    }

    /**
     * Setter method of the 'changed' boolean
     * @param b the value which will replace the current value of the variable
     */
    public void setChanged(Boolean b) {
        this.changed = b;
    }

    /**
     * Returns the current file list in the DriveCloud folder on Google Drive
     * @return a list of file in the DriveCloud
     * @throws IOException if service returns null
     */
    public List<File> getFileList() throws IOException {
        // setPageSize's default value is 100, max value is 1000
        // Its value must be 1000 in production
        FileList response = service.files().list()
                .setPageSize(100)
                .setFields("nextPageToken, files(id, name, parents, trashed, mimeType, modifiedTime, md5Checksum)")
                .execute();
        List<File> fileList = response.getFiles();
        for (File file : fileList) {
            String parentID = file.getId();
            if (file.getParents() != null && !file.getTrashed()) {
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

    /**
     * Upload the given file to Google Drive
     * @param fileName name of the file which will be uploaded to Google Drive
     * @throws IOException if service returns a null object
     */
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

    /**
     * Download the given file from Google Drive
     * There are two general type of files in Google Drive: Google's files(Google Doc, Spreadsheet etc.) and others
     * These two types are downloaded in seperate ways. So, first we needed to check the type of the file.
     * Then, we were able to download the file in correct way.
     * @param fileName name of the file which will be downloaded from Google Drive
     * @throws IOException if service returns a null object
     */
    public void downloadFile(final String fileName) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String fileId = getFileId(fileName);
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

    /**
     * Update the given file in Google Drive
     * Because of the fact that Google Drive's update() API is pretty bad,
     * this method first delete the file from Google Drive,
     * then upload the file again.
     * @param fileName name of the file which will be updated on Google Drive
     * @throws IOException if service returns a null object in used methods
     */
    public void updateFile(final String fileName) throws IOException {
        deleteFile(fileName);
        uploadFile(fileName);
    }

    /**
     * Delete the given file from Google Drive
     * @param fileName name of the file which will be deleted from Google Drive
     * @throws IOException if service returns a null object
     */
    public void deleteFile(final String fileName) throws IOException {
        service.files().delete(getFileId(fileName)).execute();
    }

    /**
     * Returns the file path for the file.
     * @param fileName Name of the file
     * @return File path of the file
     */
    private String getFilePath(final String fileName) {
        return Paths.get(APPLICATION_FOLDER_PATH, fileName).toString();
    }

    /**
     * Returns the file extension of the given file
     * While uploading file, we need to provide its Mime type.
     * For this reason, we keep a hashmap of these mime type - file extension pairs
     * and get the proper one for this purpose.
     * @param fileName name of the file whose file extension will be returned
     * @return file extension of the given file
     */
    private String getFileExtension(final String fileName) {
        for (int i = fileName.length() - 1; i >= 0; i--) {
            if (fileName.charAt(i) == '.') {
                return fileName.substring(i + 1);
            }
        }
        return fileName;
    }

    /**
     * Returns the Google Drive fileId of the given file
     * Google Drive requires the fileId of the file in some methods
     * @param fileName name of the file whose fileID will be returned
     * @return the GoogleDrive fileID of the given file
     * @throws IOException if service returns null object in getFileList() function
     */
    private String getFileId(final String fileName) throws IOException {
        if (fileIdMap.isEmpty())
            getFileList();
        return fileIdMap.get(fileName);
    }

}
