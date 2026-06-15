package com.shivani.buddyroute;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class OfflineMapHelper {

    // Check if internet is available
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(
                cm.getActiveNetwork());
        if (caps == null) return false;

        return caps.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // Get cache size used by OSMDroid in MB
    public static long getCacheSizeMB(Context context) {
        org.osmdroid.config.Configuration.getInstance()
                .load(context,
                        android.preference.PreferenceManager
                                .getDefaultSharedPreferences(context));

        java.io.File cacheDir = new java.io.File(
                context.getCacheDir(), "osmdroid");
        return getFolderSize(cacheDir) / (1024 * 1024);
    }

    private static long getFolderSize(java.io.File dir) {
        long size = 0;
        if (dir == null || !dir.exists()) return 0;
        java.io.File[] files = dir.listFiles();
        if (files == null) return 0;
        for (java.io.File f : files) {
            size += f.isDirectory() ?
                    getFolderSize(f) : f.length();
        }
        return size;
    }

    // Clear map cache
    public static void clearCache(Context context) {
        java.io.File cacheDir = new java.io.File(
                context.getCacheDir(), "osmdroid");
        deleteDir(cacheDir);
    }

    private static void deleteDir(java.io.File dir) {
        if (dir == null || !dir.exists()) return;
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }
}