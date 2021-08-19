package com.inderbagga.vitals.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.inderbagga.vitals.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Result extends Fragment {

    private int VBP1, VBP2, VRR, VHR, VO2;
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_result, container, false);
        String date=df.format(new Date());
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.result_title)+" on "+date);

        TextView tvVSRR = root.findViewById(R.id.tv_VSRR);
        TextView tvVSBPS = root.findViewById(R.id.tv_VSBP);
        TextView tvVSHR = root.findViewById(R.id.tv_VSHR);
        TextView tvVSO2 = root.findViewById(R.id.tv_VSO2);

        Bundle bundle = getArguments();

        if (bundle != null) {
            VRR = bundle.getInt("breath");
            VHR = bundle.getInt("BPM");
            VBP1 = bundle.getInt("SP");
            VBP2 = bundle.getInt("DP");
            VO2 = bundle.getInt("O2R");

            tvVSRR.setText(String.valueOf(VRR));
            tvVSHR.setText(String.valueOf(VHR));
            tvVSBPS.setText(VBP1 + " / " + VBP2);
            tvVSO2.setText(String.valueOf(VO2));
        }

        return root;
    }
}
