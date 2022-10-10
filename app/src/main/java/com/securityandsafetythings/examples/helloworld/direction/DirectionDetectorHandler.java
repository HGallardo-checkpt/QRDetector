package com.securityandsafetythings.examples.helloworld.direction;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;
import com.securityandsafetythings.examples.helloworld.render.RenderDetectionOnUI;
import com.securityandsafetythings.examples.helloworld.render.RenderHandler;
import com.securityandsafetythings.examples.helloworld.rest.QRDetectionEndPoint;

import java.util.List;

public class DirectionDetectorHandler  extends Handler {

    private static final String LOGTAG = DirectionDetectorHandler.class.getSimpleName();


    public DirectionDetectorHandler(final Looper looper) {
        super(looper);

    }


    @Override
    public void handleMessage(final android.os.Message msg) {
        final DirectionDetectorHandler.Message messageType = DirectionDetectorHandler.Message.fromOrdinal(msg.what);

        switch (messageType) {
            case CALCULATE_DIRECTION:
                List<DirectionDetection> directionDetectionList = (List<DirectionDetection>) msg.obj;
                 int size = directionDetectionList.size();
                //int area = directionDetectionList.get(0).h * directionDetectionList.get(0).w;
                if( directionDetectionList.get(0).position.first >
                                directionDetectionList.get(size-1).position.first){

                    Log.e("DIRECTION DETECTED","    UP");

                }else if (directionDetectionList.get(0).position.first <
                        directionDetectionList.get(size-1).position.first){

                    Log.e("DIRECTION DETECTED","    DOWN");

                }




        /*        while(directionDetectionList.listIterator().hasNext()){

                    int px = directionDetectionList.listIterator().next().position.first;
                    int py = directionDetectionList.listIterator().next().position.second;


                }
        */        Log.e("STARTING HANDLER DETECTOR DETECTOR","---->");

                break;
            default:
                Log.e(LOGTAG, "Unknown message received on BitmapHandlerThread");
        }
    }

    /**
     * Enum defining the messages that the BitmapHandlerThread can process.
     */
    public enum Message {
        CALCULATE_DIRECTION;
        private static final DirectionDetectorHandler.Message[] VALUES = values();
        private static DirectionDetectorHandler.Message fromOrdinal(final int ordinal) {
            return VALUES[ordinal];
        }
    }

}
