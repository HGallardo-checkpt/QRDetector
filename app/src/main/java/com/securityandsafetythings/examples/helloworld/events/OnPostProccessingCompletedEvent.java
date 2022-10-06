package com.securityandsafetythings.examples.helloworld.events;

import android.graphics.Bitmap;
import android.util.Pair;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;

import java.util.List;

public class OnPostProccessingCompletedEvent extends BaseEvent {

    public Pair<Bitmap, List<Barcode>> detectionResult;

    public OnPostProccessingCompletedEvent(Pair<Bitmap, List<Barcode>> detectionResult) {
        this.detectionResult = detectionResult;
    }

    public Pair<Bitmap, List<Barcode>> getResult() {

        return detectionResult;

    }
}
