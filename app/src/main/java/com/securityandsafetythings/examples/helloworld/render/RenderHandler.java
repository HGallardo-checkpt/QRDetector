package com.securityandsafetythings.examples.helloworld.render;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;
import com.securityandsafetythings.examples.helloworld.rest.QRDetectionEndPoint;


public class RenderHandler extends Handler {

    private static final String LOGTAG = RenderHandler.class.getSimpleName();
    private RenderDetectionOnUI renderDetectionOnUI = RenderDetectionOnUI.getInstance();


    public RenderHandler(final Looper looper) {
        super(looper);

    }


    @Override
    public void handleMessage(final android.os.Message msg) {
        final RenderHandler.Message messageType = RenderHandler.Message.fromOrdinal(msg.what);

        switch (messageType) {
            case RENDER_DATA:
                DetectionResult detectionResult = (DetectionResult) msg.obj;
                Log.e("RENDER_DATA-->",""+detectionResult.detectionResult.size());
                QRDetectionEndPoint.getInstance().setImage(renderDetectionOnUI.renderUI(detectionResult));
                //HelloWorldEndpoint.getInstance().setImage(RotateBitmap(currentBitmap,180));
                break;
            default:
                Log.e(LOGTAG, "Unknown message received on BitmapHandlerThread");
        }
    }

    /**
     * Enum defining the messages that the BitmapHandlerThread can process.
     */
    public enum Message {
        RENDER_DATA;
        private static final RenderHandler.Message[] VALUES = values();
        private static RenderHandler.Message fromOrdinal(final int ordinal) {
            return VALUES[ordinal];
        }
    }

}
