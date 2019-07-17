package com.xicato.xtouch;

/**
 * Created by andy on 12/8/17.
 */

class ButtonConfiguration {
    boolean isEnabled;
    boolean isGroup;
    boolean isSceneRecall;
    String id;
    String targetNumber;
    String fading;
    String label;

    ButtonConfiguration(boolean isEnabled, boolean isGroup, boolean isSceneRecall, String id, String targetNumber, String fading, String label){
        this.isEnabled = isEnabled;
        this.isGroup = isGroup;
        this.isSceneRecall = isSceneRecall;
        this.id = id;
        this.targetNumber = targetNumber;
        this.fading = fading;
        this.label = label;
    }
}
