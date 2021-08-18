package com.inderbagga.vitals.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.inderbagga.vitals.R;
import com.inderbagga.vitals.model.Sex;

public class GeneralInfo extends Fragment {

    private Button btnNextStep;
    private EditText etAge;
    private EditText etWeight;
    private EditText etHeight;
    private TextView tvRequiredWarning;
    private RadioButton rbFemale;
    private RadioButton rbMale;
    private RadioGroup radioGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_general_info, container, false);
        btnNextStep = view.findViewById(R.id.btn_general_step);
        etWeight = view.findViewById(R.id.et_weight);
        etHeight = view.findViewById(R.id.et_height);
        etAge = view.findViewById(R.id.et_age);
        tvRequiredWarning = view.findViewById(R.id.tv_required_warning);
        rbFemale = view.findViewById(R.id.rb_female);
        rbMale = view.findViewById(R.id.rb_male);
        radioGroup = view.findViewById(R.id.radioGroup);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.general_info_title));

        btnNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String weight = etWeight.getText().toString();
                String height = etHeight.getText().toString();
                String age = etAge.getText().toString();
                Sex sex;

                if (!TextUtils.isEmpty(age) && !TextUtils.isEmpty(height) && !TextUtils.isEmpty(weight) && radioGroup.getCheckedRadioButtonId() != -1) {
                    if (rbFemale.isChecked()) {
                        sex = Sex.Female;
                    } else {
                        sex = Sex.Male;
                    }
              /*      profile.setAge(Integer.parseInt(age));
                    profile.setHeight(Double.parseDouble(height));
                    profile.setWeight(Double.parseDouble(weight));
                    profile.setSex(sex);
                    return profile;*/

                    Navigation.findNavController(view).navigate(R.id.fragment_procedure);

                } else
                    Toast.makeText(getActivity(),getString(R.string.all_fields_required),Toast.LENGTH_LONG).show();
            }
        });
    }
}