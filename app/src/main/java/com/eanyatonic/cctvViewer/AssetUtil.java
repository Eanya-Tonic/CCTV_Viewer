package com.eanyatonic.cctvViewer;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;

public class AssetUtil {

    public static boolean fileExistsInAssets(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list("");
            if (files != null) {
                for (String file : files) {
                    if (file.equals(fileName)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}