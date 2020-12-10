package com.example.gobiblio;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadDialog {
    Activity activity;
    AlertDialog alertDialog;

    public LoadDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        builder.setView(layoutInflater.inflate(R.layout.progress_dialog_layout, null));
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    public void dismissDialog() {
        alertDialog.dismiss();
    }
}
