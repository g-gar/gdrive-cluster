package com.ggar.gdrive.framework.model;

import com.ggar.gdrive.framework.util.ExecutorWrapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import org.apache.commons.io.FilenameUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

public class GoogleDriveService {
    private final Drive service;
    private final String identifier;
    private About about;

    public GoogleDriveService(String identifier, String config, HttpTransport httpTransport, JsonFactory jsonFactory, String applicationName) throws IOException {
        this.identifier = identifier;
        GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(config), httpTransport, jsonFactory)
                .createScoped(DriveScopes.all());
        this.service = new Drive
                .Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
        this.about = reloadMetadata();
    }

    public About getAbout() {
        return this.about;
    }

    public Drive getService() {
        return this.service;
    }

    public List<File> ls() {
        return ExecutorWrapper.execute(() -> this.service
            .files()
            .list()
            .setFields("files(*)")
            .execute()
            .getFiles()
        );
    }

    public File getFile(String fileId) {
        return ExecutorWrapper.execute(() -> {
           return ls().stream()
               .filter(e -> e.getId().equals(fileId))
               .findAny()
               .orElse(null);
        });
    }

    public File persist(java.io.File file) {
        ExecutorWrapper.execute(() -> GoogleDriveService.this.reloadMetadata());
        File result = ExecutorWrapper.execute(() -> service.files()
            .create(
                new File().setName(FilenameUtils.getName(file.getName())),
                new FileContent(Files.probeContentType(file.toPath()), file)
            )
            .setFields("*")
            .execute()
        );
        ExecutorWrapper.execute(() -> GoogleDriveService.this.reloadMetadata());
        return result;
    }

    public void export(String fileId, OutputStream stream) {
        ExecutorWrapper.execute(() -> {
            File file = getFile(fileId);
            service.files().get(fileId).executeMediaAndDownloadTo(stream);
            return null;
        });
    }

    public Boolean alreadyPersisted(java.io.File file) {
        return this.ls().stream()
            .filter(e -> e.getName().equals(file.getName()))
            .findAny()
            .orElse(null) != null;
    }

    public Boolean canPersist(java.io.File file) {
        return ExecutorWrapper.execute(() -> {
            this.reloadMetadata();
            return getRemainingQuota() >= Long.valueOf(file.length());
        });
    }

    public Long getRemainingQuota() {
        About.StorageQuota quota = this.about.getStorageQuota();
        return quota.getLimit() - quota.getUsage();
    }

    public About reloadMetadata() throws IOException {
        this.about = this.service.about().get().set("fields", "*").execute();
        return this.about;
    }

    public boolean deleteFile(String fileId) {
        return ExecutorWrapper.execute(() -> {
            service.files().delete(fileId).execute();
            return this.getFile(fileId) == null;
        });
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
