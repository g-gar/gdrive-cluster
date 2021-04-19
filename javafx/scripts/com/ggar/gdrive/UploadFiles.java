package com.ggar.gdrive;

import com.ggar.gdrive.framework.model.GoogleDriveService;
import com.ggar.gdrive.framework.model.Project;
import com.ggar.gdrive.framework.security.AESCrypto;
import com.ggar.gdrive.framework.security.CryptoException;
import com.ggar.gdrive.javafx.Application;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class UploadFiles {
    public static void main(String[] args) throws IOException, CryptoException {
        String fileFolder = "D:/a";
        String jsonfolder = "D:/jetbrains/gdrive/javafx/src/main/resources/projects/cbstsrv/";
        Application app = new Application(jsonfolder);
        Project project = app.project();

        GoogleDriveService service;
        for (String s : project.keys()) {
            service = project.get(s);
            System.out.printf("%s: %s\n", s, service.getRemainingQuota());
        }

        List<String> filenames = Files.walk(Paths.get(fileFolder))
                .filter(Files::isRegularFile)
                .filter(e -> e.toString().endsWith(".mp4"))
                .filter(e -> e.toString().endsWith(".a.mp4"))
                .map(e -> e.toFile())
                .sorted((a,b) -> a.length() > b.length() ? 1 : -1)
                .map(e -> e.toPath())
                .map(e -> e.getFileName().toString())
                .map(e -> {
                    while (FilenameUtils.getExtension(e).trim().length() > 0) {
                        e = FilenameUtils.removeExtension(e);
                    }
                    return e;
                })
                .collect(Collectors.toList());

        Iterator<String> it = filenames.iterator();
        String current = null;
        while (it.hasNext()) {
            current = it.next();
            System.out.printf("Uploading %s.%s\n", current, "mp4");

            File input = new File(String.format("%s/%s.%s", fileFolder, current, "a.mp4"));

            app.project().persist(input);
            Files.delete(input.toPath());

            System.out.printf("Uploaded %s.%s\n", current, "a.mp4");
        }
    }
}
