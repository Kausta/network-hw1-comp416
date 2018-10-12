package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.shared.util.DirectoryUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import com.google.api.client.http.AbstractInputStreamContent;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream; // ###
import com.google.api.client.http.FileContent;

public class DriveConnection {

  // TODO: try-catch blokları şeklinde yazıp exception mı fırlatılmalı acaba
  // TODO: Google Doc'lar exception yazmam gerekiyor
  // FilePath


  private final String APPLICATION_NAME = "DriveCloud - Bait for Byte";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  
  // TODO: Token ve credential path'ini degistirebiliyor muyuz?
  private static final String TOKENS_DIRECTORY_PATH = "tokens";
  private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  private static final String APPLICATION_FOLDER_PATH = "/home/eusbolh/Desktop/";
  private static Drive service;

  /**
   * Global constant of the scope used by application
   * 
   * We are using "DRIVE" scope, because it gives the permission access
   * of all of the user's files with the write and delete options.
   * 
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

  private static Map<String, String> fileIdMap = new HashMap<String, String>();

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

  public static List<File> getFileList() throws IOException {
    FileList response = service.files().list()
          .setPageSize(1000)
          .setFields("nextPageToken, files(id, name, modifiedTime)")
          .execute();
    List<File> fileList = response.getFiles();
    for(File file: fileList) {
      fileIdMap.put(file.getName(), file.getId());
    }
    return fileList;
  }

  public static void uploadFile(final String fileName) throws IOException {
    java.io.File uploadFile = new java.io.File(getFilePath(fileName));
    AbstractInputStreamContent uploadContent = new FileContent(fileExtensionMap.get(getFileExtension(fileName)), uploadFile);
    File fileMetadata = new File();
    fileMetadata.setName(fileName);
    fileMetadata.setParents(null);
    File file = service.files().create(fileMetadata, uploadContent)
          .setFields("id, webContentLink, webViewLink").execute();
    System.out.println(fileName + " is uploaded!");
    System.out.println("WebContentLink: " + file.getWebContentLink() );
    System.out.println("WebViewLink: " + file.getWebViewLink() );
  }

  public void downloadFile(final String fileName) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String fileId = getFileId(fileName);
    service.files().get(fileId)
          .executeMediaAndDownloadTo(outputStream);
    // TODO: Download message yazılmalı.
    FileOutputStream fos = new FileOutputStream(getFilePath(fileName));
    fos.write(outputStream.toByteArray());
    fos.close();
  }

  /**
   * Returns the file path for the file which will be uploaded.
   * @param fileName Name of the file
   * @return File path of the file
   */
  private static String getFilePath(final String fileName) {
    return APPLICATION_FOLDER_PATH + fileName;
  }

  private static String getFileExtension(final String fileName) {
    for(int i = fileName.length()-1; i >= 0; i--) {
      if (fileName.charAt(i) == '.') {
        return fileName.substring(i+1);
      }
    }
    return fileName;
  }

  private static String getFileId(final String fileName) throws IOException{
    if (fileIdMap.isEmpty())
      getFileList();
    return fileIdMap.get(fileName);
  }

  /**
   * IMPORTANT NOTE: This method is copied from Google Drive API documentation
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

}