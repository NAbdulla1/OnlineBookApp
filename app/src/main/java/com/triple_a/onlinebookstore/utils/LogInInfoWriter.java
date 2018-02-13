package com.triple_a.onlinebookstore.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by nayon on 15-Jan-18.
 */

public class LogInInfoWriter {
    public static final String BOOK_STORE_INFO = ".info";

    public static void write(AlreadyLoggedIn alreadyLoggedIn) {
        File writeDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File writeDir = Environment.getDataDirectory();
        boolean f = true;
        if (!writeDir.exists()) {
            f = writeDir.mkdir();
        }
        if (!f)
            return;

        File logInInfoFile = new File(writeDir, BOOK_STORE_INFO);
        try {
            if (!logInInfoFile.exists())
                if (!logInInfoFile.createNewFile())
                    return;
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(logInInfoFile));
            oos.writeObject(alreadyLoggedIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
