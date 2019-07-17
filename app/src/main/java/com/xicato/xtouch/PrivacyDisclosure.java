package com.xicato.xtouch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by andy on 1/16/18.
 */

final class PrivacyDisclosure {
    public static void showDisclosure(Context mContext){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("PRIVACY AND SECURITY WARNING");

        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView disclosureText = new TextView(mContext);
        disclosureText.setEllipsize(TextUtils.TruncateAt.END);
        disclosureText.setHorizontallyScrolling(false);
        disclosureText.setText("This application has the ability to save your plaintext XIG credentials, if you so choose. "
                + "By default, it will only store the encrypted, server-revocable API token, and NOT your credentials used to obtain that token. "
                + "If you choose to store your plaintext credentials, you will weaken the security and securability of your GalaXi installation.\n"
                + "\nWe DO NOT recommend you store your credentials, but we understand it may be convenient for some users."
                + "\n\n"
                + "In all cases, we do not share your credentials, token, or information from your XIG with any third parties.\n");
        layout.addView(disclosureText);

        ScrollView scrollView = new ScrollView(mContext);
        scrollView.addView(layout);

        builder.setView(scrollView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }
}
