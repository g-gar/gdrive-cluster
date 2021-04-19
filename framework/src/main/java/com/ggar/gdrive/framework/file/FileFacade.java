package com.ggar.gdrive.framework.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

public class FileFacade {

    public enum Options {
        NO_CLOSE,
        CLOSE
    }
    public static FileOutputStream append(FileOutputStream os, byte[] data) throws IOException {
        return FileFacade.append(FileFacade.Options.CLOSE, os, data);
    }
    public static FileOutputStream append(Options mode, FileOutputStream os, byte[] data) throws IOException {
        os.write(data);
        if (mode.equals(Options.CLOSE)) {
            os.close();
        }
        return os;
    }

    public static void transform(Path input, Function<byte[], byte[]> fn) throws IOException {
        FileFacade.transform(input, input, fn);
    }

    public static void transform(Path input, Path output, Function<byte[], byte[]> fn) throws IOException {
        FileInputStream fin;
        FileOutputStream fos;
        byte[] buffer;
        int length;

        if (Files.exists(input) && Files.isRegularFile(input) && Files.isReadable(input)) {
            fin = new FileInputStream(input.toFile());
            fos = new FileOutputStream(output.toFile());
            buffer = new byte[1024];

            int i = 0;
            while ((length = fin.read(buffer)) > 0) {
                fos.write(fn.apply(buffer), 0, length);
            }

            fin.close();
            fos.close();
        }
    }

    public static void read(Path input, Path output, Consumer<byte[]> consumer) throws IOException {
        FileInputStream fin;
        byte[] buffer;

        if (Files.exists(input) && Files.isRegularFile(input) && Files.isReadable(input)) {
            fin = new FileInputStream(input.toFile());
            buffer = new byte[1024];

            while (fin.read(buffer) > 0) {
                consumer.accept(buffer);
            }

            fin.close();
        }
    }

    public static void delete(Path path) throws IOException {
        Files.delete(path);
    }

}
