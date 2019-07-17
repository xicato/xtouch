package com.xicato.xtouch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.net.UrlEscapers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 12/8/17.
 */

class WebAppInterface {
    Context mContext;

    private ConfigurationData cd;
    private Handler server_callback_handler;

    RequestQueue queue;

    WebAppInterface(Context c, ConfigurationData cd, Callback serverCallback) {
        mContext = c;
        server_callback_handler = new Handler(serverCallback);
        queue = Volley.newRequestQueue(mContext);
        if (cd != null) {
            this.cd = cd;
        } else {
            this.cd = new ConfigurationData(null, null, null, null, null);
        }
    }

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
    }

    public void retry(final String address, final int method) {
        StringRequest tokenRequest = new StringRequest(Request.Method.GET, "http://" + cd.server + ":8000/api/token", new Response.Listener<String>() {
            @Override
            public void onResponse(final String newToken) {
                cd.token = newToken;
                showToast("Acquired new token; retrying request");
                StringRequest retryRequest = new StringRequest(method, address, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showToast("Retry successful");
                        Message msg = Message.obtain();
                        msg.obj = cd;
                        server_callback_handler.sendMessage(msg);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showToast("Error! " + error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        //add params <key,value>
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        // add headers <key,value>
                        String credentials = cd.token;
                        String auth = "Bearer " + cd.token;
                        headers.put("Authorization", auth);
                        return headers;
                    }
                };
                queue.add(retryRequest);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showToast("Error! " + error);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //add params <key,value>
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                // add headers <key,value>
                String credentials = cd.savedUser + ":" + cd.savedPassword;
                String auth = "Basic "
                        + Base64.encodeToString(credentials.getBytes(),
                        Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };
        queue.add(tokenRequest);
    }

    @JavascriptInterface
    public void button(int n) {
        ButtonConfiguration bc = cd.buttonConfigs[n - 1];
        String deviceChunk = "/device/";
        String id = bc.id;
        if (bc.isGroup) {
            deviceChunk = "/group/";
        }
        String actionChunk = "setintensity/";
        if (bc.isSceneRecall) {
            actionChunk = "recallscene/";
        }
        final String addressString = "http://" + cd.server + ":8000" + deviceChunk + actionChunk + UrlEscapers.urlPathSegmentEscaper().escape(cd.network) + "/" + id + "/" + bc.targetNumber + ((bc.fading != null && !bc.fading.equals(""))? "/" + bc.fading : "");
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addressString, new Response.Listener<String>() {
                @Override
                public void onResponse(String token) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    showToast("Error accessing " + addressString + "! " + error);
                    error.printStackTrace();
                    if ((error instanceof com.android.volley.AuthFailureError) && (cd.savedUser != null) && (cd.savedPassword != null)) {
                        showToast("Retrying with stored credentials");
                        retry(addressString, Request.Method.POST);
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    //add params <key,value>
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    // add headers <key,value>
                    String credentials = cd.token;
                    String auth = "Bearer " + cd.token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            queue.add(stringRequest);
        } catch (Exception e) {
            System.out.println("Couldn't recall scene");
        }
    }

    @JavascriptInterface
    public void slider(String n) {
        final String addressString = "http://" + cd.server + ":8000/device/setintensity/" + UrlEscapers.urlPathSegmentEscaper().escape(cd.network) + "/" + cd.sliderAddress + "/" + n + ((cd.sliderFading != null && !cd.sliderFading.equals("")) ? "/" + cd.sliderFading : "");
//        showToast(addressString);
        try {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addressString, new Response.Listener<String>() {
                @Override
                public void onResponse(String token) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    showToast("Error accessing " + addressString + "! " + error);
                    if ((error instanceof com.android.volley.AuthFailureError) && (cd.savedUser != null) && (cd.savedPassword != null)) {
                        showToast("Retrying with stored credentials");
                        retry(addressString, Request.Method.POST);
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    //add params <key,value>
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    // add headers <key,value>
                    String credentials = cd.token;
                    String auth = "Bearer " + cd.token;
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            queue.add(stringRequest);
        } catch (Exception e) {
            System.out.println("Couldn't slide");
        }
    }

    @JavascriptInterface
    public void showPreferenceDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Connection Settings");

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText serverInput = new EditText(mContext);
        serverInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        serverInput.setHint("XIG Address");
        layout.addView(serverInput);

        final CheckBox loginCheckbox = new CheckBox(mContext);
        loginCheckbox.setText("Login?");
        layout.addView(loginCheckbox);

        final EditText usernameInput = new EditText(mContext);
        usernameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        usernameInput.setHint("Username");
        layout.addView(usernameInput);

        final EditText passwordInput = new EditText(mContext);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Password");
        layout.addView(passwordInput);

        final CheckBox saveCredentialsCheckbox = new CheckBox(mContext);
        saveCredentialsCheckbox.setText("Save Credentials (NOT SECURE)");
        layout.addView(saveCredentialsCheckbox);

        final EditText networkInput = new EditText(mContext);
        networkInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        networkInput.setHint("Network");
        layout.addView(networkInput);

        View hr1 = new View(mContext);
        hr1.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr1, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox enableButton1Checkbox = new CheckBox(mContext);
        enableButton1Checkbox.setText("Enable Button 1?");
        layout.addView(enableButton1Checkbox);

        final EditText button1LabelInput = new EditText(mContext);
        button1LabelInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button1LabelInput.setHint("Label");
        layout.addView(button1LabelInput);

        final RadioGroup button1GroupOrDeviceRadioGroup = new RadioGroup(mContext);
        final RadioButton button1GroupRadioButton = new RadioButton(mContext);
        button1GroupRadioButton.setText("Group");
        final RadioButton button1DeviceRadioButton = new RadioButton(mContext);
        button1DeviceRadioButton.setText("Device");
        button1GroupOrDeviceRadioGroup.addView(button1GroupRadioButton);
        button1GroupOrDeviceRadioGroup.addView(button1DeviceRadioButton);
        layout.addView(button1GroupOrDeviceRadioGroup);

        final EditText button1TargetInput = new EditText(mContext);
        button1TargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button1TargetInput.setHint("Target ID");
        layout.addView(button1TargetInput);

        final RadioGroup button1ActionSelectRadioGroup = new RadioGroup(mContext);
        final RadioButton button1SceneRecallRadioButton = new RadioButton(mContext);
        button1SceneRecallRadioButton.setText("Scene Recall");
        final RadioButton button1DirectIntensityRadioButton = new RadioButton(mContext);
        button1DirectIntensityRadioButton.setText("Direct Intensity");
        button1ActionSelectRadioGroup.addView(button1DirectIntensityRadioButton);
        button1ActionSelectRadioGroup.addView(button1SceneRecallRadioButton);
        layout.addView(button1ActionSelectRadioGroup);

        final EditText button1IntensityInput = new EditText(mContext);
        button1IntensityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button1IntensityInput.setHint("Intensity or Scene Number");
        layout.addView(button1IntensityInput);

        final EditText button1FadingInput = new EditText(mContext);
        button1FadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button1FadingInput.setHint("Fading (in ms)");
        layout.addView(button1FadingInput);

        View hr2 = new View(mContext);
        hr2.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox enableButton2Checkbox = new CheckBox(mContext);
        enableButton2Checkbox.setText("Enable Button 2?");
        layout.addView(enableButton2Checkbox);

        final EditText button2LabelInput = new EditText(mContext);
        button2LabelInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button2LabelInput.setHint("Label");
        layout.addView(button2LabelInput);

        final RadioGroup button2GroupOrDeviceRadioGroup = new RadioGroup(mContext);
        final RadioButton button2GroupRadioButton = new RadioButton(mContext);
        button2GroupRadioButton.setText("Group");
        final RadioButton button2DeviceRadioButton = new RadioButton(mContext);
        button2DeviceRadioButton.setText("Device");
        button2GroupOrDeviceRadioGroup.addView(button2GroupRadioButton);
        button2GroupOrDeviceRadioGroup.addView(button2DeviceRadioButton);
        layout.addView(button2GroupOrDeviceRadioGroup);

        final EditText button2TargetInput = new EditText(mContext);
        button2TargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button2TargetInput.setHint("Target ID");
        layout.addView(button2TargetInput);

        final RadioGroup button2ActionSelectRadioGroup = new RadioGroup(mContext);
        final RadioButton button2SceneRecallRadioButton = new RadioButton(mContext);
        button2SceneRecallRadioButton.setText("Scene Recall");
        final RadioButton button2DirectIntensityRadioButton = new RadioButton(mContext);
        button2DirectIntensityRadioButton.setText("Direct Intensity");
        button2ActionSelectRadioGroup.addView(button2DirectIntensityRadioButton);
        button2ActionSelectRadioGroup.addView(button2SceneRecallRadioButton);
        layout.addView(button2ActionSelectRadioGroup);

        final EditText button2IntensityInput = new EditText(mContext);
        button2IntensityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button2IntensityInput.setHint("Intensity or Scene Number");
        layout.addView(button2IntensityInput);

        final EditText button2FadingInput = new EditText(mContext);
        button2FadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button2FadingInput.setHint("Fading (in ms)");
        layout.addView(button2FadingInput);

        View hr3 = new View(mContext);
        hr3.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr3, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox enableButton3Checkbox = new CheckBox(mContext);
        enableButton3Checkbox.setText("Enable Button 3?");
        layout.addView(enableButton3Checkbox);

        final EditText button3LabelInput = new EditText(mContext);
        button3LabelInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button3LabelInput.setHint("Label");
        layout.addView(button3LabelInput);

        final RadioGroup button3GroupOrDeviceRadioGroup = new RadioGroup(mContext);
        final RadioButton button3GroupRadioButton = new RadioButton(mContext);
        button3GroupRadioButton.setText("Group");
        final RadioButton button3DeviceRadioButton = new RadioButton(mContext);
        button3DeviceRadioButton.setText("Device");
        button3GroupOrDeviceRadioGroup.addView(button3GroupRadioButton);
        button3GroupOrDeviceRadioGroup.addView(button3DeviceRadioButton);
        layout.addView(button3GroupOrDeviceRadioGroup);

        final EditText button3TargetInput = new EditText(mContext);
        button3TargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button3TargetInput.setHint("Target ID");
        layout.addView(button3TargetInput);

        final RadioGroup button3ActionSelectRadioGroup = new RadioGroup(mContext);
        final RadioButton button3SceneRecallRadioButton = new RadioButton(mContext);
        button3SceneRecallRadioButton.setText("Scene Recall");
        final RadioButton button3DirectIntensityRadioButton = new RadioButton(mContext);
        button3DirectIntensityRadioButton.setText("Direct Intensity");
        button3ActionSelectRadioGroup.addView(button3DirectIntensityRadioButton);
        button3ActionSelectRadioGroup.addView(button3SceneRecallRadioButton);
        layout.addView(button3ActionSelectRadioGroup);

        final EditText button3IntensityInput = new EditText(mContext);
        button3IntensityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button3IntensityInput.setHint("Intensity or Scene Number");
        layout.addView(button3IntensityInput);

        final EditText button3FadingInput = new EditText(mContext);
        button3FadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button3FadingInput.setHint("Fading (in ms)");
        layout.addView(button3FadingInput);

        View hr4 = new View(mContext);
        hr4.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr4, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox enableButton4Checkbox = new CheckBox(mContext);
        enableButton4Checkbox.setText("Enable Button 4?");
        layout.addView(enableButton4Checkbox);

        final EditText button4LabelInput = new EditText(mContext);
        button4LabelInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button4LabelInput.setHint("Label");
        layout.addView(button4LabelInput);

        final RadioGroup button4GroupOrDeviceRadioGroup = new RadioGroup(mContext);
        final RadioButton button4GroupRadioButton = new RadioButton(mContext);
        button4GroupRadioButton.setText("Group");
        final RadioButton button4DeviceRadioButton = new RadioButton(mContext);
        button4DeviceRadioButton.setText("Device");
        button4GroupOrDeviceRadioGroup.addView(button4GroupRadioButton);
        button4GroupOrDeviceRadioGroup.addView(button4DeviceRadioButton);
        layout.addView(button4GroupOrDeviceRadioGroup);

        final EditText button4TargetInput = new EditText(mContext);
        button4TargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button4TargetInput.setHint("Target ID");
        layout.addView(button4TargetInput);

        final RadioGroup button4ActionSelectRadioGroup = new RadioGroup(mContext);
        final RadioButton button4SceneRecallRadioButton = new RadioButton(mContext);
        button4SceneRecallRadioButton.setText("Scene Recall");
        final RadioButton button4DirectIntensityRadioButton = new RadioButton(mContext);
        button4DirectIntensityRadioButton.setText("Direct Intensity");
        button4ActionSelectRadioGroup.addView(button4DirectIntensityRadioButton);
        button4ActionSelectRadioGroup.addView(button4SceneRecallRadioButton);
        layout.addView(button4ActionSelectRadioGroup);

        final EditText button4IntensityInput = new EditText(mContext);
        button4IntensityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button4IntensityInput.setHint("Intensity or Scene Number");
        layout.addView(button4IntensityInput);

        final EditText button4FadingInput = new EditText(mContext);
        button4FadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button4FadingInput.setHint("Fading (in ms)");
        layout.addView(button4FadingInput);

        View hr5 = new View(mContext);
        hr5.setBackgroundColor(0xFFFFFFFF);
        layout.addView(hr5, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox enableButton5Checkbox = new CheckBox(mContext);
        enableButton5Checkbox.setText("Enable Button 5?");
        layout.addView(enableButton5Checkbox);

        final EditText button5LabelInput = new EditText(mContext);
        button5LabelInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button5LabelInput.setHint("Label");
        layout.addView(button5LabelInput);

        final RadioGroup button5GroupOrDeviceRadioGroup = new RadioGroup(mContext);
        final RadioButton button5GroupRadioButton = new RadioButton(mContext);
        button5GroupRadioButton.setText("Group");
        final RadioButton button5DeviceRadioButton = new RadioButton(mContext);
        button5DeviceRadioButton.setText("Device");
        button5GroupOrDeviceRadioGroup.addView(button5GroupRadioButton);
        button5GroupOrDeviceRadioGroup.addView(button5DeviceRadioButton);
        layout.addView(button5GroupOrDeviceRadioGroup);

        final EditText button5TargetInput = new EditText(mContext);
        button5TargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button5TargetInput.setHint("Target ID");
        layout.addView(button5TargetInput);

        final RadioGroup button5ActionSelectRadioGroup = new RadioGroup(mContext);
        final RadioButton button5SceneRecallRadioButton = new RadioButton(mContext);
        button5SceneRecallRadioButton.setText("Scene Recall");
        final RadioButton button5DirectIntensityRadioButton = new RadioButton(mContext);
        button5DirectIntensityRadioButton.setText("Direct Intensity");
        button5ActionSelectRadioGroup.addView(button5DirectIntensityRadioButton);
        button5ActionSelectRadioGroup.addView(button5SceneRecallRadioButton);
        layout.addView(button5ActionSelectRadioGroup);

        final EditText button5IntensityInput = new EditText(mContext);
        button5IntensityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button5IntensityInput.setHint("Intensity or Scene Number");
        layout.addView(button5IntensityInput);

        final EditText button5FadingInput = new EditText(mContext);
        button5FadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button5FadingInput.setHint("Fading (in ms)");
        layout.addView(button5FadingInput);

        View hr6 = new View(mContext);
        hr6.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr6, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox enableButton6Checkbox = new CheckBox(mContext);
        enableButton6Checkbox.setText("Enable Button 6?");
        layout.addView(enableButton6Checkbox);

        final EditText button6LabelInput = new EditText(mContext);
        button6LabelInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        button6LabelInput.setHint("Label");
        layout.addView(button6LabelInput);

        final RadioGroup button6GroupOrDeviceRadioGroup = new RadioGroup(mContext);
        final RadioButton button6GroupRadioButton = new RadioButton(mContext);
        button6GroupRadioButton.setText("Group");
        final RadioButton button6DeviceRadioButton = new RadioButton(mContext);
        button6DeviceRadioButton.setText("Device");
        button6GroupOrDeviceRadioGroup.addView(button6GroupRadioButton);
        button6GroupOrDeviceRadioGroup.addView(button6DeviceRadioButton);
        layout.addView(button6GroupOrDeviceRadioGroup);

        final EditText button6TargetInput = new EditText(mContext);
        button6TargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button6TargetInput.setHint("Target ID");
        layout.addView(button6TargetInput);

        final RadioGroup button6ActionSelectRadioGroup = new RadioGroup(mContext);
        final RadioButton button6SceneRecallRadioButton = new RadioButton(mContext);
        button6SceneRecallRadioButton.setText("Scene Recall");
        final RadioButton button6DirectIntensityRadioButton = new RadioButton(mContext);
        button6DirectIntensityRadioButton.setText("Direct Intensity");
        button6ActionSelectRadioGroup.addView(button6DirectIntensityRadioButton);
        button6ActionSelectRadioGroup.addView(button6SceneRecallRadioButton);
        layout.addView(button6ActionSelectRadioGroup);

        final EditText button6IntensityInput = new EditText(mContext);
        button6IntensityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button6IntensityInput.setHint("Intensity or Scene Number");
        layout.addView(button6IntensityInput);

        final EditText button6FadingInput = new EditText(mContext);
        button6FadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        button6FadingInput.setHint("Fading (in ms)");
        layout.addView(button6FadingInput);

        View hr7 = new View(mContext);
        hr7.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr7, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final CheckBox sliderControlsGroupCheckbox = new CheckBox(mContext);
        sliderControlsGroupCheckbox.setText("Slider controls group?");
        layout.addView(sliderControlsGroupCheckbox);

        final EditText sliderTargetInput = new EditText(mContext);
        sliderTargetInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        sliderTargetInput.setHint("Slider Target ID");
        layout.addView(sliderTargetInput);

        final EditText sliderFadingInput = new EditText(mContext);
        sliderFadingInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        sliderFadingInput.setHint("Slider Fading");
        layout.addView(sliderFadingInput);

        View hr8 = new View(mContext);
        hr8.setBackgroundColor(0xFFFFFF00);
        layout.addView(hr8, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

        final EditText foregroundColorInput = new EditText(mContext);
        foregroundColorInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        foregroundColorInput.setHint("Foreground Color (#XXXXXX format)");
        layout.addView(foregroundColorInput);

        final EditText backgroundColorInput = new EditText(mContext);
        backgroundColorInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        backgroundColorInput.setHint("Background Color (#XXXXXX format)");
        layout.addView(backgroundColorInput);

        final Button licenseButton = new Button(mContext);
        licenseButton.setText("License Information");
        licenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
                builder2.setTitle("License");

                LinearLayout layout2 = new LinearLayout(mContext);
                layout2.setOrientation(LinearLayout.VERTICAL);

                final TextView licenseText = new TextView(mContext);
                licenseText.setEllipsize(TextUtils.TruncateAt.END);
                licenseText.setHorizontallyScrolling(false);
                licenseText.setText("============================================\n"
                        + "Xtouch is placed under the BSD license \n"
                        + "Copyright (c) 2018, Xicato Inc. \n"
                        + "\n"
                        + "All rights reserved.\n"
                        + "\n"
                        + "Redistribution and use in source and binary forms, with or without modification, \n"
                        + "are permitted provided that the following conditions are met:\n"
                        + "* Redistributions of source code must retain the above copyright notice, \n"
                        + "  this list of conditions and the following disclaimer.\n"
                        + "* Redistributions in binary form must reproduce the above copyright notice, \n"
                        + "  this list of conditions and the following disclaimer in the documentation \n"
                        + "  and/or other materials provided with the distribution.\n"
                        + "* Neither the name of Xicato nor the names of its contributors may be used\n"
                        + "  to endorse or promote products derived from this software without specific \n"
                        + "  prior written permission.\n"
                        + "\n"
                        + "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND \n"
                        + "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED \n"
                        + "WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. \n"
                        + "IN NO EVENT SHALL XICATO BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,\n"
                        + "SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, \n"
                        + "PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR \n"
                        + "BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN \n"
                        + "CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN \n"
                        + "ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF \n"
                        + "SUCH DAMAGE.\n"
                        + "============================================\n"
                        + "\n"
                        + "Software and libraries included in this software program are released by their \n"
                        + "owners/copyright holders under the following software license agreements. \n\n"
                        + "Volley is released by Google under the Apache License V2.0\n"
                        + "                                 Apache License\n"
                        + "                           Version 2.0, January 2004\n"
                        + "                        http://www.apache.org/licenses/\n"
                        + "\n"
                        + "   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION\n"
                        + "\n"
                        + "   1. Definitions.\n"
                        + "\n"
                        + "      \"License\" shall mean the terms and conditions for use, reproduction,\n"
                        + "      and distribution as defined by Sections 1 through 9 of this document.\n"
                        + "\n"
                        + "      \"Licensor\" shall mean the copyright owner or entity authorized by\n"
                        + "      the copyright owner that is granting the License.\n"
                        + "\n"
                        + "      \"Legal Entity\" shall mean the union of the acting entity and all\n"
                        + "      other entities that control, are controlled by, or are under common\n"
                        + "      control with that entity. For the purposes of this definition,\n"
                        + "      \"control\" means (i) the power, direct or indirect, to cause the\n"
                        + "      direction or management of such entity, whether by contract or\n"
                        + "      otherwise, or (ii) ownership of fifty percent (50%) or more of the\n"
                        + "      outstanding shares, or (iii) beneficial ownership of such entity.\n"
                        + "\n"
                        + "      \"You\" (or \"Your\") shall mean an individual or Legal Entity\n"
                        + "      exercising permissions granted by this License.\n"
                        + "\n"
                        + "      \"Source\" form shall mean the preferred form for making modifications,\n"
                        + "      including but not limited to software source code, documentation\n"
                        + "      source, and configuration files.\n"
                        + "\n"
                        + "      \"Object\" form shall mean any form resulting from mechanical\n"
                        + "      transformation or translation of a Source form, including but\n"
                        + "      not limited to compiled object code, generated documentation,\n"
                        + "      and conversions to other media types.\n"
                        + "\n"
                        + "      \"Work\" shall mean the work of authorship, whether in Source or\n"
                        + "      Object form, made available under the License, as indicated by a\n"
                        + "      copyright notice that is included in or attached to the work\n"
                        + "      (an example is provided in the Appendix below).\n"
                        + "\n"
                        + "      \"Derivative Works\" shall mean any work, whether in Source or Object\n"
                        + "      form, that is based on (or derived from) the Work and for which the\n"
                        + "      editorial revisions, annotations, elaborations, or other modifications\n"
                        + "      represent, as a whole, an original work of authorship. For the purposes\n"
                        + "      of this License, Derivative Works shall not include works that remain\n"
                        + "      separable from, or merely link (or bind by name) to the interfaces of,\n"
                        + "      the Work and Derivative Works thereof.\n"
                        + "\n"
                        + "      \"Contribution\" shall mean any work of authorship, including\n"
                        + "      the original version of the Work and any modifications or additions\n"
                        + "      to that Work or Derivative Works thereof, that is intentionally\n"
                        + "      submitted to Licensor for inclusion in the Work by the copyright owner\n"
                        + "      or by an individual or Legal Entity authorized to submit on behalf of\n"
                        + "      the copyright owner. For the purposes of this definition, \"submitted\"\n"
                        + "      means any form of electronic, verbal, or written communication sent\n"
                        + "      to the Licensor or its representatives, including but not limited to\n"
                        + "      communication on electronic mailing lists, source code control systems,\n"
                        + "      and issue tracking systems that are managed by, or on behalf of, the\n"
                        + "      Licensor for the purpose of discussing and improving the Work, but\n"
                        + "      excluding communication that is conspicuously marked or otherwise\n"
                        + "      designated in writing by the copyright owner as \"Not a Contribution.\"\n"
                        + "\n"
                        + "      \"Contributor\" shall mean Licensor and any individual or Legal Entity\n"
                        + "      on behalf of whom a Contribution has been received by Licensor and\n"
                        + "      subsequently incorporated within the Work.\n"
                        + "\n"
                        + "   2. Grant of Copyright License. Subject to the terms and conditions of\n"
                        + "      this License, each Contributor hereby grants to You a perpetual,\n"
                        + "      worldwide, non-exclusive, no-charge, royalty-free, irrevocable\n"
                        + "      copyright license to reproduce, prepare Derivative Works of,\n"
                        + "      publicly display, publicly perform, sublicense, and distribute the\n"
                        + "      Work and such Derivative Works in Source or Object form.\n"
                        + "\n"
                        + "   3. Grant of Patent License. Subject to the terms and conditions of\n"
                        + "      this License, each Contributor hereby grants to You a perpetual,\n"
                        + "      worldwide, non-exclusive, no-charge, royalty-free, irrevocable\n"
                        + "      (except as stated in this section) patent license to make, have made,\n"
                        + "      use, offer to sell, sell, import, and otherwise transfer the Work,\n"
                        + "      where such license applies only to those patent claims licensable\n"
                        + "      by such Contributor that are necessarily infringed by their\n"
                        + "      Contribution(s) alone or by combination of their Contribution(s)\n"
                        + "      with the Work to which such Contribution(s) was submitted. If You\n"
                        + "      institute patent litigation against any entity (including a\n"
                        + "      cross-claim or counterclaim in a lawsuit) alleging that the Work\n"
                        + "      or a Contribution incorporated within the Work constitutes direct\n"
                        + "      or contributory patent infringement, then any patent licenses\n"
                        + "      granted to You under this License for that Work shall terminate\n"
                        + "      as of the date such litigation is filed.\n"
                        + "\n"
                        + "   4. Redistribution. You may reproduce and distribute copies of the\n"
                        + "      Work or Derivative Works thereof in any medium, with or without\n"
                        + "      modifications, and in Source or Object form, provided that You\n"
                        + "      meet the following conditions:\n"
                        + "\n"
                        + "      (a) You must give any other recipients of the Work or\n"
                        + "          Derivative Works a copy of this License; and\n"
                        + "\n"
                        + "      (b) You must cause any modified files to carry prominent notices\n"
                        + "          stating that You changed the files; and\n"
                        + "\n"
                        + "      (c) You must retain, in the Source form of any Derivative Works\n"
                        + "          that You distribute, all copyright, patent, trademark, and\n"
                        + "          attribution notices from the Source form of the Work,\n"
                        + "          excluding those notices that do not pertain to any part of\n"
                        + "          the Derivative Works; and\n"
                        + "\n"
                        + "      (d) If the Work includes a \"NOTICE\" text file as part of its\n"
                        + "          distribution, then any Derivative Works that You distribute must\n"
                        + "          include a readable copy of the attribution notices contained\n"
                        + "          within such NOTICE file, excluding those notices that do not\n"
                        + "          pertain to any part of the Derivative Works, in at least one\n"
                        + "          of the following places: within a NOTICE text file distributed\n"
                        + "          as part of the Derivative Works; within the Source form or\n"
                        + "          documentation, if provided along with the Derivative Works; or,\n"
                        + "          within a display generated by the Derivative Works, if and\n"
                        + "          wherever such third-party notices normally appear. The contents\n"
                        + "          of the NOTICE file are for informational purposes only and\n"
                        + "          do not modify the License. You may add Your own attribution\n"
                        + "          notices within Derivative Works that You distribute, alongside\n"
                        + "          or as an addendum to the NOTICE text from the Work, provided\n"
                        + "          that such additional attribution notices cannot be construed\n"
                        + "          as modifying the License.\n"
                        + "\n"
                        + "      You may add Your own copyright statement to Your modifications and\n"
                        + "      may provide additional or different license terms and conditions\n"
                        + "      for use, reproduction, or distribution of Your modifications, or\n"
                        + "      for any such Derivative Works as a whole, provided Your use,\n"
                        + "      reproduction, and distribution of the Work otherwise complies with\n"
                        + "      the conditions stated in this License.\n"
                        + "\n"
                        + "   5. Submission of Contributions. Unless You explicitly state otherwise,\n"
                        + "      any Contribution intentionally submitted for inclusion in the Work\n"
                        + "      by You to the Licensor shall be under the terms and conditions of\n"
                        + "      this License, without any additional terms or conditions.\n"
                        + "      Notwithstanding the above, nothing herein shall supersede or modify\n"
                        + "      the terms of any separate license agreement you may have executed\n"
                        + "      with Licensor regarding such Contributions.\n"
                        + "\n"
                        + "   6. Trademarks. This License does not grant permission to use the trade\n"
                        + "      names, trademarks, service marks, or product names of the Licensor,\n"
                        + "      except as required for reasonable and customary use in describing the\n"
                        + "      origin of the Work and reproducing the content of the NOTICE file.\n"
                        + "\n"
                        + "   7. Disclaimer of Warranty. Unless required by applicable law or\n"
                        + "      agreed to in writing, Licensor provides the Work (and each\n"
                        + "      Contributor provides its Contributions) on an \"AS IS\" BASIS,\n"
                        + "      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or\n"
                        + "      implied, including, without limitation, any warranties or conditions\n"
                        + "      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A\n"
                        + "      PARTICULAR PURPOSE. You are solely responsible for determining the\n"
                        + "      appropriateness of using or redistributing the Work and assume any\n"
                        + "      risks associated with Your exercise of permissions under this License.\n"
                        + "\n"
                        + "   8. Limitation of Liability. In no event and under no legal theory,\n"
                        + "      whether in tort (including negligence), contract, or otherwise,\n"
                        + "      unless required by applicable law (such as deliberate and grossly\n"
                        + "      negligent acts) or agreed to in writing, shall any Contributor be\n"
                        + "      liable to You for damages, including any direct, indirect, special,\n"
                        + "      incidental, or consequential damages of any character arising as a\n"
                        + "      result of this License or out of the use or inability to use the\n"
                        + "      Work (including but not limited to damages for loss of goodwill,\n"
                        + "      work stoppage, computer failure or malfunction, or any and all\n"
                        + "      other commercial damages or losses), even if such Contributor\n"
                        + "      has been advised of the possibility of such damages.\n"
                        + "\n"
                        + "   9. Accepting Warranty or Additional Liability. While redistributing\n"
                        + "      the Work or Derivative Works thereof, You may choose to offer,\n"
                        + "      and charge a fee for, acceptance of support, warranty, indemnity,\n"
                        + "      or other liability obligations and/or rights consistent with this\n"
                        + "      License. However, in accepting such obligations, You may act only\n"
                        + "      on Your own behalf and on Your sole responsibility, not on behalf\n"
                        + "      of any other Contributor, and only if You agree to indemnify,\n"
                        + "      defend, and hold each Contributor harmless for any liability\n"
                        + "      incurred by, or claims asserted against, such Contributor by reason\n"
                        + "      of your accepting any such warranty or additional liability.\n"
                        + "\n"
                        + "   END OF TERMS AND CONDITIONS\n"
                        + "\n"
                        + "   APPENDIX: How to apply the Apache License to your work.\n"
                        + "\n"
                        + "      To apply the Apache License to your work, attach the following\n"
                        + "      boilerplate notice, with the fields enclosed by brackets \"[]\"\n"
                        + "      replaced with your own identifying information. (Don't include\n"
                        + "      the brackets!)  The text should be enclosed in the appropriate\n"
                        + "      comment syntax for the file format. We also recommend that a\n"
                        + "      file or class name and description of purpose be included on the\n"
                        + "      same \"printed page\" as the copyright notice for easier\n"
                        + "      identification within third-party archives.\n"
                        + "\n"
                        + "   Copyright 2018 Google\n"
                        + "\n"
                        + "   Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                        + "   you may not use this file except in compliance with the License.\n"
                        + "   You may obtain a copy of the License at\n"
                        + "\n"
                        + "       http://www.apache.org/licenses/LICENSE-2.0\n"
                        + "\n"
                        + "   Unless required by applicable law or agreed to in writing, software\n"
                        + "   distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                        + "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                        + "   See the License for the specific language governing permissions and\n"
                        + "   limitations under the License.\n");
                layout2.addView(licenseText);

                ScrollView scrollView2 = new ScrollView(mContext);
                scrollView2.addView(layout2);

                builder2.setView(scrollView2);

                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                builder2.show();
            }
        });
        layout.addView(licenseButton);

        final Button privacyButton = new Button(mContext);
        privacyButton.setText("Privacy and Security Warning");
        privacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrivacyDisclosure.showDisclosure(mContext);
            }
        });
        layout.addView(privacyButton);

        ScrollView scrollView = new ScrollView(mContext);
        scrollView.addView(layout);

        builder.setView(scrollView);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ButtonConfiguration button1Config = new ButtonConfiguration(enableButton1Checkbox.isChecked(), button1GroupRadioButton.isChecked(), button1SceneRecallRadioButton.isChecked(), button1TargetInput.getText().toString(), button1IntensityInput.getText().toString(), button1FadingInput.getText().toString(), button1LabelInput.getText().toString());
                ButtonConfiguration button2Config = new ButtonConfiguration(enableButton2Checkbox.isChecked(), button2GroupRadioButton.isChecked(), button2SceneRecallRadioButton.isChecked(), button2TargetInput.getText().toString(), button2IntensityInput.getText().toString(), button2FadingInput.getText().toString(), button2LabelInput.getText().toString());
                ButtonConfiguration button3Config = new ButtonConfiguration(enableButton3Checkbox.isChecked(), button3GroupRadioButton.isChecked(), button3SceneRecallRadioButton.isChecked(), button3TargetInput.getText().toString(), button3IntensityInput.getText().toString(), button3FadingInput.getText().toString(), button3LabelInput.getText().toString());
                ButtonConfiguration button4Config = new ButtonConfiguration(enableButton4Checkbox.isChecked(), button4GroupRadioButton.isChecked(), button4SceneRecallRadioButton.isChecked(), button4TargetInput.getText().toString(), button4IntensityInput.getText().toString(), button4FadingInput.getText().toString(), button4LabelInput.getText().toString());
                ButtonConfiguration button5Config = new ButtonConfiguration(enableButton5Checkbox.isChecked(), button5GroupRadioButton.isChecked(), button5SceneRecallRadioButton.isChecked(), button5TargetInput.getText().toString(), button5IntensityInput.getText().toString(), button5FadingInput.getText().toString(), button5LabelInput.getText().toString());
                ButtonConfiguration button6Config = new ButtonConfiguration(enableButton6Checkbox.isChecked(), button6GroupRadioButton.isChecked(), button6SceneRecallRadioButton.isChecked(), button6TargetInput.getText().toString(), button6IntensityInput.getText().toString(), button6FadingInput.getText().toString(), button6LabelInput.getText().toString());
                cd.buttonConfigs = new ButtonConfiguration[]{button1Config, button2Config, button3Config, button4Config, button5Config, button6Config};
                cd.server = serverInput.getText().toString();
                cd.network = networkInput.getText().toString();
                cd.backgroundColor = backgroundColorInput.getText().toString();
                cd.foregroundColor = foregroundColorInput.getText().toString();
                if (sliderControlsGroupCheckbox.isChecked()) {
                    cd.sliderAddress = Integer.toString(Integer.parseInt(sliderTargetInput.getText().toString()) + 0xC000);

                } else {
                    cd.sliderAddress = sliderTargetInput.getText().toString();
                }
                if (sliderFadingInput.getText().toString().equals("")) {
                    cd.sliderFading = null;
                } else {
                    cd.sliderFading = sliderFadingInput.getText().toString();
                }
                if (saveCredentialsCheckbox.isChecked()){
                    cd.savedUser = usernameInput.getText().toString();
                    cd.savedPassword = passwordInput.getText().toString();
                }
                showToast("Server set to " + cd.server + ". Network: " + cd.network);
                if (loginCheckbox.isChecked()) {
                    final String user = usernameInput.getText().toString();
                    final String password = passwordInput.getText().toString();
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://" + cd.server + ":8000/api/token", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String newToken) {
                            cd.token = newToken;
                            Message msg = Message.obtain();
                            msg.obj = cd;
                            showToast("Acquired token");
                            server_callback_handler.sendMessage(msg);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showToast("Error! " + error);
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            //add params <key,value>
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<String, String>();
                            // add headers <key,value>
                            String credentials = user + ":" + password;
                            String auth = "Basic "
                                    + Base64.encodeToString(credentials.getBytes(),
                                    Base64.NO_WRAP);
                            headers.put("Authorization", auth);
                            return headers;
                        }
                    };
                    queue.add(stringRequest);
                } else {
                    Message msg = Message.obtain();
                    msg.obj = cd;
                    showToast("Updated config");
                    server_callback_handler.sendMessage(msg);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        if (cd != null) {
            if (cd.server != null) {
                serverInput.setText(cd.server);
            }
            if (cd.network != null) {
                networkInput.setText(cd.network);
            }
            if (cd.token != null) {
                loginCheckbox.setChecked(false);
                loginCheckbox.setText("Update token?");
            }
            if (cd.buttonConfigs != null) {
                ButtonConfiguration bc1 = cd.buttonConfigs[0];
                enableButton1Checkbox.setChecked(bc1.isEnabled);
                button1LabelInput.setText(bc1.label);
                button1GroupRadioButton.setChecked(bc1.isGroup);
                button1DeviceRadioButton.setChecked(!bc1.isGroup);
                button1TargetInput.setText(bc1.id);
                button1SceneRecallRadioButton.setChecked(bc1.isSceneRecall);
                button1DirectIntensityRadioButton.setChecked(!bc1.isSceneRecall);
                button1IntensityInput.setText(bc1.targetNumber);
                button1FadingInput.setText(bc1.fading);

                ButtonConfiguration bc2 = cd.buttonConfigs[1];
                enableButton2Checkbox.setChecked(bc2.isEnabled);
                button2LabelInput.setText(bc2.label);
                button2GroupRadioButton.setChecked(bc2.isGroup);
                button2DeviceRadioButton.setChecked(!bc2.isGroup);
                button2TargetInput.setText(bc2.id);
                button2SceneRecallRadioButton.setChecked(bc2.isSceneRecall);
                button2DirectIntensityRadioButton.setChecked(!bc2.isSceneRecall);
                button2IntensityInput.setText(bc2.targetNumber);
                button2FadingInput.setText(bc2.fading);

                ButtonConfiguration bc3 = cd.buttonConfigs[2];
                enableButton3Checkbox.setChecked(bc3.isEnabled);
                button3LabelInput.setText(bc3.label);
                button3GroupRadioButton.setChecked(bc3.isGroup);
                button3DeviceRadioButton.setChecked(!bc3.isGroup);
                button3TargetInput.setText(bc3.id);
                button3SceneRecallRadioButton.setChecked(bc3.isSceneRecall);
                button3DirectIntensityRadioButton.setChecked(!bc3.isSceneRecall);
                button3IntensityInput.setText(bc3.targetNumber);
                button3FadingInput.setText(bc3.fading);

                ButtonConfiguration bc4 = cd.buttonConfigs[3];
                enableButton4Checkbox.setChecked(bc4.isEnabled);
                button4LabelInput.setText(bc4.label);
                button4GroupRadioButton.setChecked(bc4.isGroup);
                button4DeviceRadioButton.setChecked(!bc4.isGroup);
                button4TargetInput.setText(bc4.id);
                button4SceneRecallRadioButton.setChecked(bc4.isSceneRecall);
                button4DirectIntensityRadioButton.setChecked(!bc4.isSceneRecall);
                button4IntensityInput.setText(bc4.targetNumber);
                button4FadingInput.setText(bc4.fading);

                ButtonConfiguration bc5 = cd.buttonConfigs[4];
                enableButton5Checkbox.setChecked(bc5.isEnabled);
                button5LabelInput.setText(bc5.label);
                button5GroupRadioButton.setChecked(bc5.isGroup);
                button5DeviceRadioButton.setChecked(!bc5.isGroup);
                button5TargetInput.setText(bc5.id);
                button5SceneRecallRadioButton.setChecked(bc5.isSceneRecall);
                button5DirectIntensityRadioButton.setChecked(!bc5.isSceneRecall);
                button5IntensityInput.setText(bc5.targetNumber);
                button5FadingInput.setText(bc5.fading);

                ButtonConfiguration bc6 = cd.buttonConfigs[5];
                enableButton6Checkbox.setChecked(bc6.isEnabled);
                button6LabelInput.setText(bc6.label);
                button6GroupRadioButton.setChecked(bc6.isGroup);
                button6DeviceRadioButton.setChecked(!bc6.isGroup);
                button6TargetInput.setText(bc6.id);
                button6SceneRecallRadioButton.setChecked(bc6.isSceneRecall);
                button6DirectIntensityRadioButton.setChecked(!bc6.isSceneRecall);
                button6IntensityInput.setText(bc6.targetNumber);
                button6FadingInput.setText(bc6.fading);
            } else {
                button1LabelInput.setText("1");
                button2LabelInput.setText("2");
                button3LabelInput.setText("3");
                button4LabelInput.setText("4");
                button5LabelInput.setText("5");
                button6LabelInput.setText("6");
            }
            if (cd.sliderAddress != null && !cd.sliderAddress.equals("")) {
                Integer sliderInt = Integer.parseInt(cd.sliderAddress);
                if (sliderInt > 0xC000 && sliderInt < 65535) {
                    sliderTargetInput.setText(Integer.toString(sliderInt - 0xC000));
                    sliderControlsGroupCheckbox.setChecked(true);
                } else {
                    sliderTargetInput.setText(cd.sliderAddress);
                    sliderControlsGroupCheckbox.setChecked(false);
                }

                showToast("Bad slider configuration, resetting it.");
            }
            if (cd.backgroundColor != null && cd.foregroundColor != null) {
                foregroundColorInput.setText(cd.foregroundColor);
                backgroundColorInput.setText(cd.backgroundColor);
            }
            if (cd.savedUser != null && cd.savedPassword != null) {
                usernameInput.setText(cd.savedUser);
                passwordInput.setText(cd.savedPassword);
                saveCredentialsCheckbox.setChecked(true);
            }
        }

        builder.show();
    }
}
