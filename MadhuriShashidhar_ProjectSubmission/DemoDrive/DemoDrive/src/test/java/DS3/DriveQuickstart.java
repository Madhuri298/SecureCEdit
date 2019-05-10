package DS3;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DriveQuickstart
{

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Directory to store user credentials for this application.
    private static final java.io.File CREDENTIALS_FOLDER = new java.io.File(System.getProperty("c:\\Code\\"), "credentials");

    private static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";

    // Global instance of the scopes required by this quickstart.If modifying these scopes, delete your previously saved credentials/ folder.
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException
    {
        java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);
        if (!clientSecretFilePath.exists())
        {
            throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }

        // Load client secrets.
        InputStream in = new FileInputStream(clientSecretFilePath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(CREDENTIALS_FOLDER))
        .setAccessType("offline").build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    //to create credentials folder
    private static void createCredentials() throws IOException, GeneralSecurityException
    {
    	System.out.println("CREDENTIALS_FOLDER: " + CREDENTIALS_FOLDER.getAbsolutePath());

        //1: Create CREDENTIALS_FOLDER
        if (!CREDENTIALS_FOLDER.exists())
        {
            CREDENTIALS_FOLDER.mkdirs();
            System.out.println("Created Folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
            System.out.println("Copy file " + CLIENT_SECRET_FILE_NAME + " into folder above.. and rerun this class!!");
            return;
        }
    }
    
    //to create service
    public static Drive createService() throws IOException, GeneralSecurityException
    {
        //2: Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        //3: Read client_secret.json file & create Credential object.
        Credential credential = getCredentials(HTTP_TRANSPORT);

        //5: Create Google Drive Service.
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
        return service;

    }
    
    //To upload a file from file system to Google Drive and print the File ID
    public static void saveFile(String filename) throws IOException, GeneralSecurityException
    {
    	createCredentials();
    	Drive service = createService();
    	
        File fileMetadata = new File();
        fileMetadata.setName(filename + ".txt");
        java.io.File filePath = new java.io.File("C:\\Users\\Madhuri\\Documents\\Project\\Files\\" + filename + ".txt");
        FileContent mediaContent = new FileContent("text/plain", filePath);
        File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
        System.out.println("File successfully uploaded to drive");
        System.out.println("File ID: " + file.getId());
    }
    
    //to share file 
    /*public static void shareFile(String email) throws IOException, GeneralSecurityException
    {
    	Drive service = createService();
    	FileList result = service.files().list().setPageSize(1).setFields("nextPageToken, files(id, name)").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty())
        {
        	System.out.println("No files found.");
        }
        else
        {
        	File file = files.get(0);
        	System.out.printf("The file is: %s and the File ID is: %s", file.getName(), file.getId());
        	file.getPermissions();
        }
    }*/
    
    public static Permission createPermissionForEmail(String email) throws IOException, GeneralSecurityException {
        // user - group - domain - anyone
        String permissionType = "user"; 
        // organizer - owner - writer - commenter - reader
        String permissionRole = "reader";
 
        Permission newPermission = new Permission();
        newPermission.setType(permissionType);
        newPermission.setRole(permissionRole);
        newPermission.setEmailAddress(email);
 
        Drive service = createService();
        FileList result = service.files().list().setPageSize(1).setFields("nextPageToken, files(id, name)").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty())
        {
        	System.out.println("No files found.");
        	return null;
        }
        else
        {
        	File file = files.get(0);
        	System.out.printf("The file is: %s and the File ID is: %s\n", file.getName(), file.getId());
        	System.out.printf("File successfully shared with: %s", email);
        	return service.permissions().create(file.getId().toString(), newPermission).execute();
        }
    }
    public static String openFile(String filename) throws IOException, GeneralSecurityException 
    {
    	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
        String res = "";
        FileList result = service.files().list().setPageSize(1).setFields("nextPageToken, files(id, name)").execute();
        List<File> files = result.getFiles();
        if(files == null || files.isEmpty())
        {
            System.out.println("No files found.");
            return null;
        } 
        else
        {
            System.out.println("File successfully downloaded...opening the file");
            for (File file : files)
            {
            	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            	String fileId = file.getId();
            	service.files().get(fileId).executeMediaAndDownloadTo(outStream);
                res = outStream.toString();
            }
            return res;
        }
    }

    
}