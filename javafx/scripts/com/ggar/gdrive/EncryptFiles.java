package com.ggar.gdrive;

import com.ggar.gdrive.framework.file.FileFacade;
import com.ggar.gdrive.framework.model.Project;
import com.ggar.gdrive.framework.security.AESCrypto;
import com.ggar.gdrive.framework.security.CryptoException;
import com.ggar.gdrive.framework.util.ExecutorWrapper;
import com.ggar.gdrive.javafx.Application;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class EncryptFiles {

    private static final String ALGORITHM = "blowfish";

    public static byte[] encrypt(String key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");
        SecretKeySpec _key = new SecretKeySpec(key.getBytes(),"Blowfish");
        byte[] iv = Hex.decodeHex("1234567812345678");
        cipher.init(Cipher.ENCRYPT_MODE, _key, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    public static void main(String[] args) throws IOException, CryptoException {
        String fileFolder = "D:/a";
        String jsonfolder = "D:/jetbrains/gdrive/javafx/src/main/resources/projects/cbstsrv/";
        Application app = new Application(jsonfolder);
        Project project = app.project();

        List<String> filenames = Files.walk(Paths.get(fileFolder))
                .filter(Files::isRegularFile)
                .filter(e -> e.toString().endsWith(".mp4"))
                .filter(e -> !e.toString().endsWith(".a.mp4"))
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

        while (it.hasNext()) {
            Thread thread = new Thread2(fileFolder, it.next(), app.pass);
            thread.start();
        }
    }
}

class Thread2 extends Thread {
    private final String folder;
    private final String filename;
    private final String password;

    public Thread2(String folder, String filename, String password) {
        this.folder = folder;
        this.filename = filename;
        this.password = password;
    }

    @Override
    public void start() {
        super.start();

        ExecutorWrapper.execute(() -> {
            System.out.printf("Encrypting %s.%s\n", filename, "mp4");

            File input = new File(String.format("%s/%s.%s", folder, filename, "mp4"));
            File output = new File(String.format("%s/%s.%s", folder, filename, "a.mp4"));

            FileFacade.transform(input.toPath(), output.toPath(), bytes -> ExecutorWrapper.execute(() -> {
                return EncryptFiles.encrypt(password, bytes);
            }));
            FileFacade.delete(input.toPath());

            System.out.printf("Encrypted %s.%s\n", filename, "mp4");
            return null;
        });
    }

    @Override
    public void run() {
        super.run();


    }
}
