package com.ggar.gdrive.javafx;

import com.ggar.gdrive.framework.model.GoogleDriveService;
import com.ggar.gdrive.framework.model.Project;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Application extends javafx.application.Application {

    public static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE, DriveScopes.DRIVE_METADATA);
    public static final String APPLICATION_NAME = "OpenDirectoryDownloader";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static HttpTransport HTTP_TRANSPORT;
    private static Project project;
    public final String pass = "dff60ebc093d5d12d90968cee4d55167";

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public Application(String projectFolder) {
        Path path = Paths.get(projectFolder);

        Project project = null;
        try {
            project = new Project(path.getFileName().toString());
            List<Path> jsons = Files.walk(path)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json"))
                    .collect(Collectors.toList());
            jsons.stream();
            for (Path json : jsons) {
                String identifier = FilenameUtils.removeExtension(json.getFileName().toString());
                project.register(identifier, new GoogleDriveService(identifier, json.toString(), HTTP_TRANSPORT, JSON_FACTORY, APPLICATION_NAME));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Application.project = project;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public static void main(String[] args) throws Exception {
        //App app = new App(args[0]);

    }

    public static Project project() {
        return project;
    }
}
