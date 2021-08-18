package com.inderbagga.vitals;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);

        alertDialog.setTitle(getString(R.string.app_name)+" says,")
                .setMessage("Are you sure, you want to exit?")
                .setNegativeButton("Yes", (dialog, WhichButton) -> {
                    dialog.cancel();
                    finish();
                })
                .setPositiveButton("No", (dialog, WhichButton) -> {
                    dialog.cancel();
                })
                .setCancelable(true)
                .create()
                .show();
    }
}