package com.securityandsafetythings.examples.helloworld.render;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Pair;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class RenderDetectionOnUI {

    private static RenderDetectionOnUI instance = null;
    RenderDetectionOnUI(){
        OpenCVLoader.initDebug();
    }


    public static synchronized RenderDetectionOnUI getInstance() {
        if (instance == null) {
            instance = new RenderDetectionOnUI();
        }
        return instance;
    }

    public Bitmap renderUI(DetectionResult detectionResult){
        Bitmap bitmap = detectionResult.bitmap;
        Bitmap bitmapResult = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        int fontType = Imgproc.FONT_HERSHEY_PLAIN;
        int fontSze = 2;
        int thickness = 2;
        Scalar color = new Scalar(0, 0, 255);

        if(detectionResult.detectionResult!=null){

            while(detectionResult.detectionResult.listIterator().hasNext()){
                Pair<RectF,String> dataResult =  detectionResult.detectionResult.listIterator().next();
                Point p1 = new Point(dataResult.first.left,dataResult.first.top);
                Point p2 = new Point(dataResult.first.right,dataResult.first.bottom);

                Imgproc.rectangle(mat,p1,p2,new Scalar(76,255,0));
                Imgproc.putText(mat, dataResult.second, p2, fontType,
                        fontSze, color, thickness);


            }
        }
         Utils.matToBitmap(mat,bitmapResult);
        return bitmapResult;
    }

}
