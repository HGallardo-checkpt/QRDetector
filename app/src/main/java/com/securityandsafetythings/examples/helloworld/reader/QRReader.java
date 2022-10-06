package com.securityandsafetythings.examples.helloworld.reader;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.Pair;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.securityandsafetythings.examples.helloworld.interfaces.OnQRTranslateListener;
import com.securityandsafetythings.examples.helloworld.opencv.Renderer;
import com.securityandsafetythings.examples.helloworld.rest.QRDetectionEndPoint;

import java.util.List;

public class QRReader {

    private static QRReader sInstance = null;

     private static BarcodeScanner scanner;
    private static BarcodeScannerOptions options;

    QRReader(){

    }
    public static synchronized QRReader getInstance() {
        if (sInstance == null) {
            sInstance = new QRReader();
        }
        options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        scanner = BarcodeScanning.getClient(options);


        return sInstance;

    }



    public void  readCode(Bitmap bitmap,OnQRTranslateListener onQRTranslateListener){
        final String[] result = {""};
         InputImage image = InputImage.fromBitmap(bitmap, 0);
         scanner.process(image).addOnSuccessListener(barcodes -> {
             onQRTranslateListener.setResult(new Pair(bitmap,barcodes));

         /*   while(barcodes.listIterator().hasNext()){
                 barcodes.listIterator().next().getRawValue();
                onQRTranslateListener.setResult(new Pair(bitmap,barcodes));
            }*/
        });
      }



}
