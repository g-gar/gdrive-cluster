package com.ggar.gdrive.framework.model;

import com.ggar.gdrive.framework.container.AbstractContainer;
import com.ggar.gdrive.framework.util.ExecutorWrapper;
import com.google.api.services.drive.model.File;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Project extends AbstractContainer<String, GoogleDriveService> {

    private final String name;

    public Project(String name) {
        super();
        this.name = name;
    }

    public File persist(java.io.File file) {
        return !alreadyPersisted(file) ? ExecutorWrapper.execute(() -> {
            File result = null;
            Iterator<String> iterator = super.container.keySet().iterator();
            GoogleDriveService service = null;
            while (result == null && iterator.hasNext()) {
                service = super.container.get(iterator.next());
                if (!service.alreadyPersisted(file) && service.canPersist(file)) {
                    System.out.printf("[%s] persisting %s (%s bytes)\n", service.getIdentifier(), file.getName(), file.length());
                    result = service.persist(file);
                    System.out.printf("[%s] persisted %s [%s left]\n", service.getIdentifier(), file.getName(), service.getRemainingQuota());
                }
            }
            return result;
        }) : null;
    }

    public boolean canPersist(java.io.File file) {
        return ExecutorWrapper.execute(() -> {
            boolean result = false;
            Iterator<String> iterator = super.container.keySet().iterator();
            GoogleDriveService service = null;
            while (!result && iterator.hasNext()) {
                service = get(iterator.next());
                result |= service.canPersist(file);
            }
            return result;
        });
    }

    public void export(String fileId, OutputStream stream) {
        ExecutorWrapper.execute(() -> {
            Iterator<GoogleDriveService> it = this.container.values().iterator();
            GoogleDriveService service = null;
            while (it.hasNext() && ( service = it.next() ).getFile(fileId) != null) {
                service.export(fileId, stream);
            }
            return null;
        });
    }

    public Boolean alreadyPersisted(java.io.File file) {
        return ExecutorWrapper.execute(() -> {
            Boolean result = false;
            Iterator<GoogleDriveService> it = this.container.values().iterator();
            while (!result && it.hasNext()) {
                result |= it.next().alreadyPersisted(file);
            }
            return result;
        });
    }

    public Set<File> getFiles() {
        Set<File> files = new HashSet<>();

        GoogleDriveService service;
        for (String key : super.keys()) {
            service = super.get(key);
            for (File file : service.ls()) {
                files.add(file);
            }
        }

        return files;
    }

    public File getFile(String id) {
        return ExecutorWrapper.execute(() -> {
            File result = null;
            Iterator<GoogleDriveService> it = this.container.values().iterator();
            GoogleDriveService service = null;
            while (it.hasNext() && result == null) {
                service = it.next();
                result = service.getFile(id);
            }
            return result;
        });
    }

    public boolean deleteFile(String fileId) {
        return ExecutorWrapper.execute(() -> {
            boolean deleted = false;
            Iterator<GoogleDriveService> it = this.container.values().iterator();
            GoogleDriveService service = null;
            while (it.hasNext() && !deleted && (service = it.next()).getFile(fileId) != null) {
                deleted = deleted | service.deleteFile(fileId);
            }
            return deleted;
        });
    }

    public String getName() {
        return this.name;
    }
}
