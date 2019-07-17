package com.xicato.xtouch;

/**
 * Created by andy on 12/8/17.
 */

class ConfigurationData {
    String server;
    String network;
    String token;
    ButtonConfiguration[] buttonConfigs;
    String sliderAddress;
    String foregroundColor;
    String backgroundColor;
    String sliderFading;
    String savedUser;
    String savedPassword;

    ConfigurationData(String server, String network, String token, ButtonConfiguration[] buttonConfigs, String sliderAddress) {
        this.server = server;
        this.network = network;
        this.token = token;
        this.buttonConfigs = buttonConfigs;
        this.sliderAddress = sliderAddress;
    }
}
