package com.securityandsafetythings.examples.helloworld.events;

public class OnFinishDetectionEvent extends BaseEvent {

    public String direction;

    public OnFinishDetectionEvent(String direction) {
        this.direction = direction;
    }

    public String getResult() {

        return direction;

    }
}
