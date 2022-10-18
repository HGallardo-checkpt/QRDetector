package com.securityandsafetythings.examples.helloworld.events;

import android.graphics.Bitmap;
import android.util.Pair;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;

public class OnPostProccessingNotDetectedEvent extends BaseEvent {

    public boolean detectedCode;

    public OnPostProccessingNotDetectedEvent(boolean detectedCode) {
        this.detectedCode = detectedCode;
    }

    public boolean getResult() {

        return detectedCode;

    }
}
