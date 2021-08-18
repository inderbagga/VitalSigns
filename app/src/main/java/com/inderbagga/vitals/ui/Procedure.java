package com.inderbagga.vitals.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.inderbagga.vitals.R;

public class Procedure extends Fragment {
    private Button btnStart;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_procedure, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.procedure_title));

        btnStart = root.findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(
                        getActivity(), Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(),"Starting the process.",Toast.LENGTH_LONG).show();
                }
                else
                   askOrEnableCameraPermission();
            }
        });
        return root;
    }

    private void askOrEnableCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted)
                    Toast.makeText(getActivity(),"Starting the process.",Toast.LENGTH_LONG).show();
                else if(!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(getString(R.string.app_name)+" says,")
                            .setMessage("Please open Settings and enable the CAMERA permission to monitor vital signs.")
                            .setPositiveButton("Dismiss", (dialog, WhichButton) -> {
                                dialog.cancel();
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }
                else {
                    AlertDialog.Builder alertDialog=new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(getString(R.string.app_name)+" says,")
                            .setMessage("Access to CAMERA is required to monitor vital signs.")
                            .setPositiveButton("ENABLE", (dialog, WhichButton) -> {
                                dialog.cancel();
                                askOrEnableCameraPermission();
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }
            });
}
