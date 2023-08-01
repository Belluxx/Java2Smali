package com.belluxx.java2smali.util;

import com.belluxx.java2smali.Main;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import static com.belluxx.java2smali.Main.SEPARATOR;
import static com.belluxx.java2smali.Main.print;

public class ConversionManager {

    public static File compileJavaFile(File javaFile) {
        String workingDirPath = javaFile.getAbsoluteFile().getParentFile().getAbsolutePath();
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
        String workingDirPath = javaFolder.getAbsoluteFile().getParentFile().getAbsolutePath();
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
            generateFilesList(javaFolder, fileList, true);
            compiler.run(null, null, errors, "-source", "1.8", "-target", "1.8", "@" + fileList.getAbsolutePath().replace(" ", "\\ "), "-d", workingDirPath + SEPARATOR + javaFolder.getName().concat("_compiled"));
        }
        checkErrors(errors.toString(), javaFolder.getName() + " folder");

        return new File(workingDirPath + SEPARATOR + javaFolder.getName() + "_compiled");
    }

    public static File dexClassFile(File classFile, boolean optimize) {
        File dexFile = new File(classFile.getAbsoluteFile().getParentFile().getAbsolutePath() + SEPARATOR + "classes.dex");

        if (optimize) {
            ToolsManager.runDx(new String[]{"--dex", "--no-strict", "--no-warning", "--output", dexFile.getAbsolutePath(), classFile.getAbsolutePath()});
        } else {
            ToolsManager.runDx(new String[]{"--dex", "--no-strict", "--no-optimize", "--no-warning", "--output", dexFile.getAbsolutePath(), classFile.getAbsolutePath()});
        }

        return dexFile;
    }

    public static File dexClassFolder(File classFolder, boolean optimize) {
        File dexFile = new File(classFolder.getAbsoluteFile().getParentFile().getAbsolutePath() + SEPARATOR + "classes.dex");

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
        deleteFile(folderFile.getAbsolutePath() + SEPARATOR + "FilesList.txt");
        deleteFile(folderFile.getAbsolutePath() + SEPARATOR + "classes.dex");
        deleteFile(compiledJava.getAbsoluteFile());
    }

    private static void generateFilesList(File folderFile, File listFile, boolean deleteOldList) {
        File[] files = folderFile.listFiles();

        if (deleteOldList) {
            deleteFile(listFile);
            try {
                boolean created = listFile.createNewFile();
                if (!created) throw new IOException("File already exists");
            } catch (IOException e) {
                print("[E] Cannot recreate " + listFile.getName());
                return;
            }
        }

        try {
            for (File file : Objects.requireNonNull(files)) {
                if (!file.isDirectory()) {
                    Files.write(listFile.toPath(), (file.getAbsolutePath() + "\n").getBytes(), StandardOpenOption.APPEND);
                } else {
                    generateFilesList(file, listFile, false);
                }
            }
        } catch (IOException e) {
            print("[E] Cannot create " + listFile.getName());
        }
    }


    private static void checkErrors(String log, String fileName) {
        if (log.contains("error")) {
            print("[E] Error during the compilation of " + fileName + "\n" + log);
            print("[E] Compilation failed, check errors above for more info");
            System.exit(1);
        }
    }

    private static void deleteFile(String fileName) {
        deleteFile(new File(fileName));
    }

    private static void deleteFile(File file) {
        if (file.exists()) {
            try {
                if (file.isDirectory()) deleteFolder(file);
                else Files.delete(file.getAbsoluteFile().toPath());
            } catch (IOException e) { print("[W] Cannot delete " + file.getName()); }
        } else print("[W] " + file.getName() + " does not exist");
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

}
