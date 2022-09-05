package com.securityandsafetythings.examples.helloworld.storage;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import com.securityandsafetythings.examples.helloworld.TfLiteDetectorApplication;
import com.securityandsafetythings.examples.helloworld.inference.tflite.Detector;
import com.securityandsafetythings.examples.helloworld.inference.tflite.TFLiteObjectDetectionAPIModel;

import java.io.*;
import java.util.List;
import java.util.UUID;

public final class StorageBitmapLocally {

    private static StorageBitmapLocally sInstance = null;
    private static  Context context;

    private StorageBitmapLocally(){
        context = TfLiteDetectorApplication.getAppContext();
    }



    public static synchronized StorageBitmapLocally getInstance() {
        if (sInstance == null) {
            sInstance = new StorageBitmapLocally();
        }
        return sInstance;
    }

    public synchronized void setImage(final Bitmap bitmap) {
       saveToInternalStorage(bitmap);
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        UUID uuid = UUID.randomUUID();

        File mypath=new File(directory, uuid.toString()+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


}
