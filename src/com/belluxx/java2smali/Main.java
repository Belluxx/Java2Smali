package com.belluxx.java2smali;

import com.belluxx.java2smali.util.ConversionManager;

import java.io.File;

public class Main {
    public static final String SEPARATOR = File.separator;
    public static final String OUT_SMALI_FOLDER = "converted";
    public static final String JAVA_EXT = ".java";
    public static final String CLASS_EXT = ".class";
    public static final String SMALI_EXT = ".smali";
    private static final String VERSION = "1.3";
    private static final String HELP_MESSAGE = "Usage:" +
            "\n\tJava2Smali [--no-optimize] <JavaSource / JavaSourcesDirectory>" +
            "\n\tJava2Smali --version" +
            "\n\tJava2Smali --help";
    private static final String NOFILE_ERROR = "[E] $FILE$ does not exist.";
    private File compiledJava;

    public static void main(String[] args) {
        new Main().init(args);
    }

    public static void print(String s) {
        System.out.println(s);
    }

    private void init(String[] args) {
        if (args.length == 0) {
            print(HELP_MESSAGE);
            System.exit(1);
        } else {
            if (args[0].startsWith("-")) {
                switch (args[0]) {
                    case "--version":
                    case "-v":
                        print("Java2Smali v" + VERSION);
                        break;
                    case "--help":
                    case "-h":
                        print(HELP_MESSAGE);
                        break;
                    case "--no-optimize":
                    case "-n":
                        if (args.length > 1) convertAndClean(args[1], false);
                        else print("[E] Missing file.");
                        break;
                    default:
                        print("[E] Unknown command.");
                        print(HELP_MESSAGE);
                        break;
                }
            } else {
                convertAndClean(args[0], true);
            }
        }
    }

    private void convertAndClean(String providedPath, boolean optimize) {
        String providedFormattedPath = formatPath(providedPath);
        checkPath(providedFormattedPath);
        File providedFile = new File(providedFormattedPath).getAbsoluteFile();

        if (providedFile.exists()) {
            // Convert to smali
            convert(providedFile, optimize);

            // Cleanup
            print("[I] Cleaning...");
            ConversionManager.clean(providedFile.getParentFile(), compiledJava);
            print("[I] Done.");
        } else {
            print(NOFILE_ERROR.replace("$FILE$", providedFormattedPath));
        }
    }

    private void convert(File providedFile, boolean optimize) {

        print("[I] Compiling...");
        if (providedFile.isDirectory()) {
            compiledJava = ConversionManager.compileJavaFolder(providedFile);
        } else {
            compiledJava = ConversionManager.compileJavaFile(providedFile);
        }

        print("[I] Dexing...");
        File dexFile;
        if (providedFile.isDirectory()) {
            dexFile = ConversionManager.dexClassFolder(compiledJava, optimize);
        } else {
            dexFile = ConversionManager.dexClassFile(compiledJava, optimize);
        }

        print("[I] Running baksmali...");
        ConversionManager.baksmaliDex(dexFile);
    }

    private String formatPath(String path) {
        String formattedPath = path;
        if (path.endsWith("\"")) {
            formattedPath = formattedPath.substring(0, formattedPath.length() - 1);
        }

        return formattedPath;
    }

    private void checkPath(String path) {
        if (new File(path).isDirectory() && path.contains(" ")) {
            print("[E] The path provided contains spaces");
            print("[I] Only the conversion of single files is available for paths that contain spaces");
            System.exit(1);
        }
    }
}
