package com.baitforbyte.networkhw1.master;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.baitforbyte.networkhw1.follower.FileData;
import com.baitforbyte.networkhw1.shared.ApplicationConfiguration;
import com.baitforbyte.networkhw1.shared.base.BaseServer;
import com.baitforbyte.networkhw1.shared.file.data.ChangeTracking;
import com.baitforbyte.networkhw1.shared.file.data.Constants;
import com.baitforbyte.networkhw1.shared.file.data.FileTransmissionModel;
import com.baitforbyte.networkhw1.shared.file.data.FileUtils;
import com.baitforbyte.networkhw1.shared.file.master.IFileServer;
import com.baitforbyte.networkhw1.shared.util.DirectoryUtils;


public class Server extends BaseServer {
    private static final int PERIOD = 45;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int filePort;

    private IFileServer fileServer;
    private String directory;
    private DriveConnection drive;
    public  int i = 0;

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     *
     * @param port Server port
     */
    public Server(int port, IFileServer fileServer, int filePort) throws IOException, GeneralSecurityException {
        super(port);
        this.fileServer = fileServer;
        this.directory = DirectoryUtils.getDirectoryInDesktop(ApplicationConfiguration.getInstance().getFolderName());
        this.filePort = filePort;
        drive = new DriveConnection();
        drive.checkFolderIsExist();
        drive.initializeChangeMap();
        // drive.getFileList();
        /*for(File file: drive.getFileList()){
            System.out.println(file);
        }*/
        /*HashMap<String, FileData> tmpFileDataMap = drive.getFileDataMap();
        for(String e: tmpFileDataMap.keySet()) {
            FileData tmpFileData = tmpFileDataMap.get(e);
            System.out.println(e);
            System.out.println(tmpFileData.getHash());
        }*/
        //drive.deleteFile("perfection.txt");
        // drive.updateFile("perfection.txt");
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Don't forget to call getPageToken() before scheduler
                System.out.println("");
                drive.getFileList();
                Set<String> changedSet = ChangeTracking.getChangedFiles(directory);
                Set<String> createdSet = ChangeTracking.getAddedFiles(directory);
                Set<String> deletedSet = ChangeTracking.getFilesToDelete(directory);
                drive.detectChanges();
                for(String s: changedSet) {
                    drive.setChanged(true);
                    System.out.println("Change detected!");
                    System.out.println("Local file \"" + s + "\" is changed. File will be updated on cloud!");
                    drive.updateFile(s);
                    drive.addChangeLog(s);
                    System.out.println("\"" + s + "\" is updated on cloud!\n");
                }
                for(String s: createdSet) {
                    drive.setChanged(true);
                    System.out.println("Change detected!");
                    System.out.println("Local file \"" + s + "\" is created. File will be added to cloud!");
                    drive.uploadFile(s);
                    drive.addChangeLog(s);
                    System.out.println("\"" + s + "\" is added to cloud!\n");
                }
                for(String s: deletedSet) {
                    drive.setChanged(true);
                    System.out.println("Change detected!");
                    System.out.println("Local file \"" + s + "\" is deleted. File will be deleted on cloud!");
                    drive.deleteFile(s);
                    drive.addChangeLog(s);
                    System.out.println("\"" + s + "\" is deleted from cloud!\n");
                }
                if(!drive.isChanged()) {
                    System.out.println("No change is detected in this cycle!\n");
                    System.out.println("================================");
                }
                else {
                    System.out.println("================================");
                }
                drive.setChanged(false);
                // dont erase
                syncLogs();
                // dont erase

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, PERIOD, TimeUnit.SECONDS);

        // ornekler

    }

    private void syncLogs() throws NoSuchAlgorithmException, IOException {
        FileUtils.saveLog(ChangeTracking.getLocalFileNames(directory), directory, Constants.PREV_FILES_LOG_NAME);
        Set<String> localHashes = new HashSet<>();
        for (String file : ChangeTracking.getLocalFileNames(directory)) {
            localHashes.add(file + "-" + getLocalFiles().get(file).getHash());
        }
        FileUtils.saveLog(localHashes, directory, Constants.CHANGE_FILES_LOG_NAME);
    }

    public void startWorking() throws IOException, NoSuchAlgorithmException {
        compareHash(drive.getFileDataMap());
    }

    public void compareHash(HashMap<String, FileData> files) throws IOException, NoSuchAlgorithmException {
        System.out.println("In Compare Hash");
        List<String> filesToSend = new ArrayList<String>();
        List<String> filesToRequest = new ArrayList<String>();
        HashMap<String, FileData> localFiles = getLocalFiles();
        for (String fileName : files.keySet()) {
            if (localFiles.containsKey(fileName)) {
                FileData local = localFiles.get(fileName);
                FileData cloud = files.get(fileName);
                System.out.println("Local: " + local.getHash() + " - Cloud: " + cloud.getHash());
                if (!local.getHash().equals(cloud.getHash())) {
                    long dateDiff = local.getLastChangeTime() - cloud.getLastChangeTime();
                    if (dateDiff > 0) {
                        filesToSend.add(fileName);
                    } else if (dateDiff < 0) {
                        filesToRequest.add(fileName);
                    }
                }
                localFiles.remove(fileName);
            } else {
                filesToRequest.add(fileName);
            }
        }
        System.out.println(Arrays.deepToString(filesToRequest.toArray()));
        filesToSend.addAll(localFiles.keySet());
        System.out.println(Arrays.deepToString(filesToSend.toArray()));
        receiveFiles(filesToRequest);
        sendFiles(filesToSend);
    }

    public void sendFiles(List<String> files) throws IOException {
        for (String file : files) {
            drive.uploadFile(file);
        }
    }

    public void receiveFiles(List<String> files) throws IOException {
        for (String file : files) {
            drive.downloadFile(file);
        }
    }

    private HashMap<String, FileData> getLocalFiles() throws IOException, NoSuchAlgorithmException {
        HashMap<String, FileData> files = new HashMap<>();
        FileTransmissionModel[] fileModels = FileUtils.getAllFilesInDirectory(directory);

        for (FileTransmissionModel file : fileModels) {
            files.put(file.getFilename(), new FileData(file.getHash(), file.getLastModifiedTimestamp()));
        }
        return files;
    }

    // public void deleteFiles() {}

    /**
     * Listens to the line and starts a connection on receiving a request from the client
     * The connection is started and initiated as a ServerThread object
     */
    public void listenAndAccept() throws IOException {
        Socket s = getServerSocket().accept();
        System.out.println("A connection was established with a client on the address of " + s.getRemoteSocketAddress());

        ServerThread st = new ServerThread(s, fileServer, filePort, directory);
        st.start();
    }

}

