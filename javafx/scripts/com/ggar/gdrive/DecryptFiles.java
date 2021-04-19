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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DecryptFiles {
    private static final String ALGORITHM = "blowfish";

    public static byte[] decrypt(String key, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("Blowfish/CBC/NoPadding");
        SecretKeySpec _key = new SecretKeySpec(key.getBytes(),"Blowfish");
        byte[] iv = Hex.decodeHex("1234567812345678");
        cipher.init(Cipher.DECRYPT_MODE, _key, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    public static void main(String[] args) throws IOException, CryptoException, NoSuchPaddingException, ShortBufferException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String fileFolder = "D:/a/";
        String jsonfolder = "D:/jetbrains/gdrive/javafx/src/main/resources/projects/cbstsrv/";
        Application app = new Application(jsonfolder);
        Project project = app.project();

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
        File file = null;
        String current = null;
        while (it.hasNext()) {
            current = it.next();
            System.out.printf("Encrypting %s.%s\n", current, "mp4");

            File input = new File(String.format("%s/%s.%s", fileFolder, current, "a.mp4"));
            File output = new File(String.format("%s/%s.%s", fileFolder, current+"asdf", "mp4"));

            FileFacade.transform(input.toPath(), output.toPath(), bytes -> ExecutorWrapper.execute(() -> {
                return decrypt(app.pass, bytes);
            }));
            Files.delete(input.toPath());

            System.out.printf("Decrypted %s.%s\n", current, "a.mp4");
        }
    }
}
