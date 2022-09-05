package com.securityandsafetythings.examples.helloworld.events;

import com.securityandsafetythings.examples.helloworld.events.BaseEvent;

public class OnInferenceCompletedEvent extends BaseEvent {
    private final byte[] imageBytes;

    public OnInferenceCompletedEvent(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public byte[] getImageAsBytes() {
        return imageBytes;
    }

}
