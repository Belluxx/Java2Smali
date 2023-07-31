package com.belluxx.java2smali.util;

import com.belluxx.java2smali.Main;

import java.io.File;

import static com.belluxx.java2smali.Main.OUT_SMALI_FOLDER;

public class ToolsManager {
    public static void runDx(String[] args) {
        com.android.dx.command.Main.main(args);
    }

    public static void runBaksmali(String dexFilePath) {
        org.jf.baksmali.Main.main(new String[]{"d", dexFilePath, "-o", new File(dexFilePath).getAbsoluteFile().getParentFile().getAbsolutePath() + Main.SEPARATOR + OUT_SMALI_FOLDER});
    }
}
