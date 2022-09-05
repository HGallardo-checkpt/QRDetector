package com.securityandsafetythings.examples.helloworld.inference.handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.securityandsafetythings.examples.helloworld.TfLiteDetectorApplication;
import com.securityandsafetythings.examples.helloworld.events.OnInferenceCompletedEvent;
import com.securityandsafetythings.examples.helloworld.inference.tflite.Detector;
import com.securityandsafetythings.examples.helloworld.inference.tflite.TFLiteObjectDetectionAPIModel;
import com.securityandsafetythings.examples.helloworld.opencv.Renderer;
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
    private static final int TF_OD_API_INPUT_SIZE = 416;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "tflite/model.tflite";
    private static final String TF_OD_API_LABELS_FILE = "tflite/labelmap.txt";

    private Detector detector;
    int cropSize = TF_OD_API_INPUT_SIZE;

    private Context context;

    private Renderer renderer;

    public InferenceHandler(final Looper looper) {
        super(looper);
         context = TfLiteDetectorApplication.getAppContext();
         renderer = Renderer.getInstance();
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
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            context,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();

        }
    }
    private void handleRunningInference(final Bitmap imageBmp) {
        if(detector!=null){
            final List<Detector.Recognition> tfResults = detector.recognizeImage(imageBmp);
            new OnInferenceCompletedEvent(renderer.getImageWithBoxesAsBytes(imageBmp,tfResults)).broadcastEvent();
        }
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
