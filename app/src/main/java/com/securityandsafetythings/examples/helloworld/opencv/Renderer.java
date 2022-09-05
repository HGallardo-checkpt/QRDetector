package com.securityandsafetythings.examples.helloworld.opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;
import com.securityandsafetythings.examples.helloworld.TfLiteDetectorApplication;
import com.securityandsafetythings.examples.helloworld.inference.tflite.Detector;
import com.securityandsafetythings.examples.helloworld.interfaces.OnQRTranslateListener;
import com.securityandsafetythings.examples.helloworld.reader.QRReader;
import com.securityandsafetythings.examples.helloworld.storage.StorageBitmapLocally;
import com.securityandsafetythings.jumpsuite.commonhelpers.BitmapUtils;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class Renderer{

    private String codeTranslation = "Default value";
    private static Renderer sInstance = null;

    private static QRReader qrReader = QRReader.getInstance();

    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;

    private Renderer(){
        OpenCVLoader.initDebug();
     }
    public static synchronized Renderer getInstance() {
        if (sInstance == null) {
            sInstance = new Renderer();
        }

        return sInstance;
    }

    public byte[] getImageWithBoxesAsBytes(final Bitmap imageBmp, final List<Detector.Recognition> tfResults) {

        Mat mat = new Mat();
        Bitmap bitmap = imageBmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap, mat);

        if (tfResults.size() > 0) {
            for (final Detector.Recognition tfResult : tfResults) {
                final RectF location = tfResult.getLocation();
                if (location != null && tfResult.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                    Point p1 = new Point(location.left,location.top);
                    Point p2 = new Point(location.right,location.bottom);
                    Imgproc.rectangle(mat,p1,p2,new Scalar(76,255,0));
                    if(p1.x>=0 && p1.y >= 0){
                        qrReader.readCode(Bitmap.createBitmap(
                                bitmap,
                                (int) p1.x,
                                (int) p1.y,
                                (int) location.width(),
                                (int) location.height()), new OnQRTranslateListener() {
                            @Override
                            public void setString(String _codeTranslation) {
                                codeTranslation = _codeTranslation;

                            }
                        });

                        int fontType = Imgproc.FONT_HERSHEY_PLAIN;
                        int fontSze = 2;
                        int thickness = 2;
                        Scalar color = new Scalar(0, 0, 255);
                        Imgproc.putText(mat, codeTranslation, p2, fontType,
                                fontSze, color, thickness);

                    }
                }
            }

        }

        Utils.matToBitmap(mat,bitmap);
        return BitmapUtils.compressBitmap(bitmap);
    }



}
