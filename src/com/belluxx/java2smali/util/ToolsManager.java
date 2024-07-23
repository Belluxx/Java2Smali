package com.belluxx.java2smali.util;

import com.belluxx.java2smali.Main;

import java.io.File;

import static com.belluxx.java2smali.Main.OUT_SMALI_FOLDER;

public class ToolsManager {
    public static void runDx(String[] args) {
        com.android.dx.command.Main.main(args);
    }

    public static void runBaksmali(String dexFilePath) {
        final String outDirPath = ConversionManager.getParent(new File(dexFilePath));
        com.android.tools.smali.baksmali.Main.main(new String[]{"d", dexFilePath, "-o", outDirPath + Main.SEPARATOR + OUT_SMALI_FOLDER});
    }
}
