package com.belluxx.java2smali.util;

import com.belluxx.java2smali.Main;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static com.belluxx.java2smali.Main.SEPARATOR;
import static com.belluxx.java2smali.Main.print;

public class ConversionManager {

    public static File compileJavaFile(File javaFile) {
        String workingDirPath = getParent(javaFile);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        OutputStream errors = new OutputStream() {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public void write(int b) {
                this.sb.append((char) b);
            }

            @Override
            public String toString() {
                return this.sb.toString();
            }
        };

        if (compiler == null) {
            print("[E] You are running a JRE instead of a JDK.");
            System.exit(1);
        } else {
            compiler.run(null, null, errors, "-source", "1.8", "-target", "1.8", javaFile.getAbsolutePath());
        }
        checkErrors(errors.toString(), javaFile.getName());

        return new File(workingDirPath + SEPARATOR + javaFile.getName().replace(Main.JAVA_EXT, Main.CLASS_EXT));
    }

    public static File compileJavaFolder(File javaFolder) {
        String workingDirPath = getParent(javaFolder);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        OutputStream errors = new OutputStream() {
            private final StringBuilder sb = new StringBuilder();

            @Override
            public void write(int b) {
                this.sb.append((char) b);
            }

            @Override
            public String toString() {
                return this.sb.toString();
            }
        };

        if (compiler == null) {
            print("[E] You are running a JRE instead of a JDK");
            System.exit(1);
        } else {
            File fileList = new File(workingDirPath + SEPARATOR + "FilesList.txt");
            generateFilesList(javaFolder, fileList, new String[]{"java"});
            compiler.run(null, null, errors, "-source", "1.8", "-target", "1.8", "@" + fileList.getAbsolutePath().replace(" ", "\\ "), "-d", workingDirPath + SEPARATOR + javaFolder.getName().concat("_compiled"));
        }
        checkErrors(errors.toString(), javaFolder.getName() + " folder");

        return new File(workingDirPath + SEPARATOR + javaFolder.getName() + "_compiled");
    }

    public static File dexClassFile(File classFile, boolean optimize) {
        File dexFile = new File(getParent(classFile) + SEPARATOR + "classes.dex");

        if (optimize) {
            ToolsManager.runDx(new String[]{"--dex", "--no-strict", "--no-warning", "--output", dexFile.getAbsolutePath(), classFile.getAbsolutePath()});
        } else {
            ToolsManager.runDx(new String[]{"--dex", "--no-strict", "--no-optimize", "--no-warning", "--output", dexFile.getAbsolutePath(), classFile.getAbsolutePath()});
        }

        return dexFile;
    }

    public static File dexClassFolder(File classFolder, boolean optimize) {
        File dexFile = new File(getParent(classFolder) + SEPARATOR + "classes.dex");

        if (optimize) {
            ToolsManager.runDx(new String[]{"--dex", "--no-strict", "--no-warning", "--output", dexFile.getAbsolutePath(), classFolder.getAbsolutePath()});
        } else {
            ToolsManager.runDx(new String[]{"--dex", "--no-strict", "--no-optimize", "--no-warning", "--output", dexFile.getAbsolutePath(), classFolder.getAbsolutePath()});
        }

        return dexFile;
    }

    public static void baksmaliDex(File dexFile) {
        ToolsManager.runBaksmali(dexFile.getAbsolutePath());
    }

    public static void clean(File folderFile, File compiledJava) {
        File[] filesToDelete = new File[]{
                new File(folderFile.getAbsolutePath() + SEPARATOR + "FilesList.txt"),
                new File(folderFile.getAbsolutePath() + SEPARATOR + "classes.dex"),
                compiledJava.getAbsoluteFile()
        };

        for (File file : filesToDelete) {
            if (file.exists()) deleteFile(file);
        }
    }

    private static void generateFilesList(File folderFile, File listFile, String extensionsAllowed[]) {
        if (listFile.exists()) deleteFile(listFile);

        try {
            boolean created = listFile.createNewFile();
            if (!created) throw new IOException("File already exists");
        } catch (IOException e) {
            print("[E] Cannot recreate " + listFile.getName());
            return;
        }

        generateFilesListRecursive(folderFile, listFile, extensionsAllowed);
    }

    private static void generateFilesListRecursive(File folderFile, File listFile, String extensionsAllowed[]) {
        File[] files = folderFile.listFiles();

        try {
            for (File file : files) {
                if (!file.isDirectory()) {
                    if (Arrays.stream(extensionsAllowed).anyMatch(getExtension(file)::equals)) {
                    Files.write(listFile.toPath(), (file.getAbsolutePath() + "\n").getBytes(), StandardOpenOption.APPEND);
                    } else print("[I] Ignoring " + file.getName() + " because it does not match the allowed extensions.");
                } else generateFilesListRecursive(file, listFile, extensionsAllowed);
            }
        } catch (IOException e) { print("[E] Cannot create " + listFile.getName()); }
    }

    private static void checkErrors(String log, String fileName) {
        if (log.contains("error")) {
            print("[E] Error during the compilation of " + fileName + "\n" + log);
            print("[E] Compilation failed, check errors above for more info");
            System.exit(1);
        }
    }

    private static void deleteFile(File file) {
        if (file.exists()) {
            try {
                if (file.isDirectory()) deleteFolder(file);
                else Files.delete(file.getAbsoluteFile().toPath());
            } catch (IOException e) { print("[W] Cannot delete " + file.getName()); }
        } else print("[W] Cannot delete " + file.getName() + " because it does not exist");
    }

    private static void deleteFolder(File folder) throws IOException {
        File[] files = folder.listFiles();

        if (files != null) {
            for (File tempFile : files) {
                if (!tempFile.delete()) print("[W] Cannot delete " + tempFile.getName());
            }
            if (!folder.delete()) throw new IOException("[E] Cannot delete " + folder.getName());
        } else throw new IOException("[E] Directory " + folder.getName() + " is empty");
    }

    public static String getParent(File file) {
        return file.getAbsoluteFile().getParentFile().getAbsolutePath();
    }

    public static String getExtension(File file) {
        return file.getName().substring(file.getName().lastIndexOf('.')+1);
    }

}
