package com.inderbagga.vitals;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.os.Bundle;

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
                .setMessage("You may select below options?")
                .setNegativeButton("Exit", (dialog, WhichButton) -> {
                    dialog.cancel();
                    finish();
                })
                .setPositiveButton("Reset", (dialog, WhichButton) -> {
                    dialog.cancel();
                    Navigation.findNavController(this.findViewById(R.id.nav_host_fragment)).navigate(R.id.fragment_general_info);
                })
                .setCancelable(true)
                .create()
                .show();
    }
}