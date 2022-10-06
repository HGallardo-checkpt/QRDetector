package com.securityandsafetythings.examples.helloworld.inference.handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.google.mlkit.vision.common.InputImage;
import com.securityandsafetythings.examples.helloworld.TfLiteDetectorApplication;
import com.securityandsafetythings.examples.helloworld.events.OnInferenceCompletedEvent;
import com.securityandsafetythings.examples.helloworld.events.OnPostProccessingCompletedEvent;
import com.securityandsafetythings.examples.helloworld.inference.tflite.Detector;
import com.securityandsafetythings.examples.helloworld.inference.tflite.TFLiteObjectDetectionAPIModel;
import com.securityandsafetythings.examples.helloworld.opencv.Renderer;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;
import com.securityandsafetythings.examples.helloworld.reader.QRReader;
import com.securityandsafetythings.examples.helloworld.rest.QRDetectionEndPoint;
 import com.securityandsafetythings.jumpsuite.commonhelpers.BitmapUtils;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.DetectionModel;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class InferenceHandler extends Handler{
    private static final String LOGTAG = InferenceHandler.class.getSimpleName();

    private Context context;

    private static QRReader qrReader ;


    public InferenceHandler(final Looper looper) {
        super(looper);

    }

    @Override
    public void handleMessage(final android.os.Message msg) {
        final Message messageType = Message.fromOrdinal(msg.what);
        switch (messageType) {
            case CONFIGURE_DETECTOR:
                 handleConfigureDetector();
                break;
            case RUN_INFERENCE:
                final Bitmap imageBmp = (Bitmap)msg.obj;
                handleRunningInference(imageBmp);
                break;
            default:
                Log.e(LOGTAG, "Unknown message received on InferenceThread");
        }
    }

    private void handleConfigureDetector() {
        qrReader = QRReader.getInstance();
    }
    private void handleRunningInference(final Bitmap imageBmp) {
        qrReader.readCode(imageBmp, result -> {
              Log.e("----->",""+result.second.size());

              if(result.second.size()>0){
                  new OnPostProccessingCompletedEvent(result).broadcastEvent();

              }

             /* if(result.second.size()>0){

                  Mat mat = new Mat();
                  Utils.bitmapToMat(result.first,mat);
                  Rect location =  result.second.get(0).getBoundingBox();
                  Point p1 = new Point(location.left,location.top);
                  Point p2 = new Point(location.right,location.bottom);
                  Imgproc.rectangle(mat,p1,p2,new Scalar(76,255,0));
                  int fontType = Imgproc.FONT_HERSHEY_PLAIN;
                  int fontSze = 2;
                  int thickness = 2;
                  Scalar color = new Scalar(0, 0, 255);
                  Imgproc.putText(mat, result.second.listIterator().next().getRawValue(), p2, fontType,
                          fontSze, color, thickness);
                  Utils.matToBitmap(mat,result.first);

              }
*/       });

        /*if(detector!=null){
            final List<Detector.Recognition> tfResults = detector.recognizeImage(imageBmp);
            renderer.getImageWithBoxesAndTextAsBytes(imageBmp,tfResults,detectionResult -> {
                new OnPostProccessingCompletedEvent(detectionResult).broadcastEvent();
            });

            //new OnInferenceCompletedEvent(renderer.getImageWithBoxesAsBytes(imageBmp,tfResults)).broadcastEvent();
        }*/
    }

    /**
     * Enum defining the messages that the InferenceThread can process.
     */
    public enum Message {
        /**
         * Send this message to configure a detector.
         */
        CONFIGURE_DETECTOR,
        /**
         * Send this message to run inference on an image.
         */
        RUN_INFERENCE;

        private static final Message[] VALUES = values();

        /**
         * Retrieves the Message value given an ordinal value.
         *
         * @param ordinal The ordinal value representation of an {@code Message}.
         * @return An {@code Message} represented by the provided ordinal value.
         */
        private static Message fromOrdinal(final int ordinal) {
            return VALUES[ordinal];
        }
    }


}
