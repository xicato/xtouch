package com.xicato.xtouch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;

public class MainActivity extends Activity {
    private SharedPreferences mPrefs;
    private WebView webView;
    private ConfigurationData cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPrefs = getSharedPreferences("xicato-xtouch.pref", 0);
        String cd_string = mPrefs.getString("ConfigurationData", null);
        if (cd_string != null) {
            cd = new Gson().fromJson(cd_string, ConfigurationData.class);
        } else {
            cd = null;
        }
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (cd != null && cd.server != null && cd.token != null) {
                    setButtonsJS();
                    if (cd.foregroundColor != null && cd.backgroundColor != null) {
                        webView.evaluateJavascript("setColors('" + cd.backgroundColor + "', '" + cd.foregroundColor + "');", null);
                    }
                }
            }
        });

        webView.loadUrl("file:///android_asset/xtouch.html");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        WebAppInterface webAppInterface = new WebAppInterface(this, cd, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                ConfigurationData cd_in = (ConfigurationData) msg.obj;
                cd = cd_in;
                if (cd != null) {
                    if (cd.buttonConfigs != null) {
                        setButtonsJS();
                    }
                    if (cd.foregroundColor != null && cd.backgroundColor != null) {
                        webView.evaluateJavascript("setColors('" + cd.backgroundColor + "', '" + cd.foregroundColor + "');", null);
                    }
                }
                savePreferences();
                return true;
            }
        });

        webView.addJavascriptInterface(webAppInterface, "Android");

        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("SRB Console", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        PrivacyDisclosure.showDisclosure(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
    }

    private void savePreferences() {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString("ConfigurationData", new Gson().toJson(cd));
        ed.commit();
    }

    private void setButtonsJS() {
        Log.d("Buttons", "Setting buttons");
        String disableString = "";
        if (cd.sliderAddress != null) {
            disableString += "enableOnOffButtons(true);";
        } else {
            disableString += "enableOnOffButtons(false);";
        }
        for (int i = 1; i < 7; i++) {
            ButtonConfiguration bc = cd.buttonConfigs[i - 1];
            Log.d("Buttons", "Button " + i + " is Enabled: " + bc.isEnabled);
            disableString += "setButton(" + i + ", " + bc.isEnabled + ", '" + bc.label + "');";
        }
        Log.d("Buttons", disableString);
        webView.evaluateJavascript(disableString, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d("Buttons", "Button JS finished.");
            }
        });
    }

}
