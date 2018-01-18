package com.triple_a.onlinebookstore.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static com.triple_a.onlinebookstore.utils.LogInInfoWriter.BOOK_STORE_INFO;

/**
 * Created by nayon on 15-Jan-18.
 */

public class LogInInfoReader {
    public static AlreadyLoggedIn read() {
        File writeDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //File writeDir = Environment.getDataDirectory();
        boolean f = true;
        if (!writeDir.exists()) {
            f = writeDir.mkdir();
        }
        if (!f)
            return null;

        File logInInfoFile = new File(writeDir, BOOK_STORE_INFO);
        try {
            if (!logInInfoFile.exists()) {
                LogInInfoWriter.write(
                        new AlreadyLoggedIn(false, null, null, null)
                );
            }
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(logInInfoFile));
            return (AlreadyLoggedIn) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
