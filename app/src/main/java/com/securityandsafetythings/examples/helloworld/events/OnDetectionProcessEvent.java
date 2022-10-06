package com.securityandsafetythings.examples.helloworld.events;

import android.graphics.Bitmap;
import com.securityandsafetythings.examples.helloworld.pojos.DetectionResult;

public class OnDetectionProcessEvent extends BaseEvent {

    public Bitmap bitmap;

    public OnDetectionProcessEvent(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getResult() {

        return bitmap;

    }
}