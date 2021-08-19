package com.inderbagga.vitals.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.inderbagga.vitals.R;
import com.inderbagga.vitals.core.Fft;
import com.inderbagga.vitals.core.Fft2;
import com.inderbagga.vitals.core.ImageProcessing;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

public class Process extends Fragment {

    //Variables Initialization
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;

    //ProgressBar
    private ProgressBar progressBar;
    public int progressValue = 0;
    public int inc = 0;

    //Beats variable
    public int Beats = 0;
    public double bufferAvgB = 0;

    //Freq + timer variable
    private static long startTime = 0;
    private double SamplingFreq;

    //SPO2 variable
    private static Double[] RedBlueRatio;
    public int o2;
    double stdDevRed = 0;
    double stdDevBlue= 0;
    double sumRed = 0;
    double sumBlue = 0;

    //RR variable
    public int Breath = 0;
    public double bufferAvgBr = 0;

    //BloodPressure variables
    public double age, height, weight;
    public double Q = 4.5;
    private static int SP = 0, DP = 0;

    //arraylist
    public ArrayList<Double> avgGreenList = new ArrayList<>();
    public ArrayList<Double> avgRedList = new ArrayList<>();
    public ArrayList<Double> avgBlueList = new ArrayList<>();
    public int frames = 0;

    private TextView statusView,errorView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_process, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.process_title));

        TextView tvUserAge = root.findViewById(R.id.tv_user_age);
        TextView tvUserSex = root.findViewById(R.id.tv_user_sex);
        TextView tvUserWeight = root.findViewById(R.id.tv_user_weight);
        TextView tvUserHeight = root.findViewById(R.id.tv_user_height);
        statusView= root.findViewById(R.id.statusView);
        errorView= root.findViewById(R.id.errorView);

        height = getArguments().getInt("height");
        weight = getArguments().getInt("weight");
        age = getArguments().getInt("age");

        if (getArguments().getInt("sex") == 2) {
            Q = 5;
            tvUserSex.setText("Male");
        }

        tvUserAge.setText(age+"");
        tvUserWeight.setText(weight + " kg");
        tvUserHeight.setText(height + " cm");

        preview = root.findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        progressBar = root.findViewById(R.id.progressBar);
        progressBar.setProgress(0);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        startTime = System.currentTimeMillis();
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    //this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    @Override
    public void onPause() {
        super.onPause();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    //getting frames data from the camera and start the measuring process
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            //if data or size == null ****
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            //Atomically sets the value to the given updated value if the current value == the expected value.
            if (!processing.compareAndSet(false, true)) return;

            //put width + height of the camera inside the variables
            int width = size.width;
            int height = size.height;

            //RGB intensities initialization
            double avgGreen;
            double avgRed;
            double avgBlue;

            avgGreen = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 3); //Getting Green intensity after applying image processing on frame data, 3 stands for green

            avgRed = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 1); //Getting Red intensity after applying image processing on frame data, 1 stands for red
            sumRed = sumRed + avgRed; //Summing Red intensity for the whole period of recording which is 30 second

            avgBlue = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 2); //Getting Blue intensity after applying image processing on frame data, 2 stands for blue
            sumBlue = sumBlue + avgBlue; //Summing Red intensity for the whole period of recording which is 30 second

            //Adding RGB intensity values to listofarrays
            avgGreenList.add(avgGreen);
            avgRedList.add(avgRed);
            avgBlueList.add(avgBlue);

            ++frames; //counts the number of frames for the whole period of recording " 30 s "

            //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
            if (avgRed < 200) {
                inc = 0;
                progressValue = inc;
                frames = 0;
                progressBar.setProgress(progressValue);
                errorView.setText(Html.fromHtml(getString(R.string.poor_intensity)));
                processing.set(false);
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d; //convert time to seconds to be compared with 30 seconds

            if (totalTimeInSecs >= 30) {
                //convert listOfArrays to arrays to be used in processing
                Double[] greenArray = avgGreenList.toArray(new Double[avgGreenList.size()]);
                Double[] redArray = avgRedList.toArray(new Double[avgRedList.size()]);
                Double[] blueArray = avgBlueList.toArray(new Double[avgBlueList.size()]);

                SamplingFreq = (frames / totalTimeInSecs); //calculating sampling frequency

                if(frames==0){
                    errorView.setText(Html.fromHtml(getString(R.string.no_frame)));
                    return;
                }

                //sending the rg arrays with the counter to make an fft process to get the heartbeats out of it
                double HRFreq = Fft.FFT(greenArray, frames, SamplingFreq);
                double bpm = (int) ceil(HRFreq * 60);
                double HR1Freq = Fft.FFT(redArray, frames, SamplingFreq);
                double bpm1 = (int) ceil(HR1Freq * 60);

                //sending the rg arrays with the counter to make an fft process then a bandpass filter to get the respiration rate out of it
                double RRFreq = Fft2.FFT(greenArray, frames, SamplingFreq);
                double breath = (int) ceil(RRFreq * 60);
                double RR1Freq = Fft2.FFT(redArray, frames, SamplingFreq);
                double breath1 = (int) ceil(RR1Freq * 60);

                //calculating the mean of red and blue intensities on the whole period of recording
                double meanRed = sumRed / frames;
                double meanBlue = sumBlue / frames;

                //calculating the standard  deviation
                for (int i = 0; i < frames - 1; i++) {
                    double blueBuffer = blueArray[i];
                    stdDevBlue = stdDevBlue + ((blueBuffer - meanBlue) * (blueBuffer - meanBlue));

                    double redBuffer = redArray[i];
                    stdDevRed = stdDevRed + ((redBuffer - meanRed) * (redBuffer - meanRed));
                }

                //calculating the variance
                double redVariance = sqrt(stdDevRed / (frames - 1));
                double blueVariance = sqrt(stdDevRed / (frames - 1));

                //calculating ratio between the two means and two variances
                double ratio = (redVariance / meanRed) / (blueVariance / meanBlue);

                //estimating SPo2
                double spo2 = 100 - 5 * (ratio);
                o2 = (int) (spo2);

                //comparing if heartbeat and Respiration rate are reasonable from the green and red intensities then take the average, otherwise value from green or red intensity if one of them is good and other is bad.
                if ((bpm > 45 || bpm < 200) || (breath > 10 || breath < 20)) {
                    if ((bpm1 > 45 || bpm1 < 200) || (breath1 > 10 || breath1 < 24)) {

                        bufferAvgB = (bpm + bpm1) / 2;
                        bufferAvgBr = (breath + breath1) / 2;

                    } else {
                        bufferAvgB = bpm;
                        bufferAvgBr = breath;
                    }
                } else if ((bpm1 > 45 || bpm1 < 200) || (breath1 > 10 || breath1 < 20)) {
                    bufferAvgB = bpm1;
                    bufferAvgBr = breath1;
                }

                //if the values of hr and o2 are not reasonable then show a toast that measurement failed and restart the progress bar and the whole recording process for another 30 seconds
                if ((bufferAvgB < 45 || bufferAvgB > 200) || (bufferAvgBr < 10 || bufferAvgBr > 24)) {
                    inc = 0;
                    progressValue = inc;
                    progressBar.setProgress(progressValue);
                    errorView.setText("Measurement failed.");
                    startTime = System.currentTimeMillis();
                    frames = 0;
                    processing.set(false);
                    return;
                }

                Beats = (int) bufferAvgB;
                Breath = (int) bufferAvgBr;

                //estimations to estimate the blood pressure
                double ROB = 18.5;
                double ET = (364.5 - 1.23 * Beats);
                double BSA = 0.007184 * (Math.pow(weight, 0.425)) * (Math.pow(height, 0.725));
                double SV = (-6.6 + (0.25 * (ET - 35)) - (0.62 * Beats) + (40.4 * BSA) - (0.51 * age));
                double PP = SV / ((0.013 * weight - 0.007 * age - 0.004 * Beats) + 1.307);
                double MPP = Q * ROB;

                SP = (int) (MPP + 3 / 2 * PP);
                DP = (int) (MPP - PP / 3);
            }

            //if all those variable contains a valid values then display the results
            if ((Beats != 0) && (SP != 0) && (DP != 0) && (o2 != 0) && (Breath != 0)) {

                Bundle bundle = new Bundle();
                bundle.putInt("SP", SP);
                bundle.putInt("DP", DP);
                bundle.putInt("BPM", Beats);
                bundle.putInt("breath", Breath);
                bundle.putInt("O2R", o2);

                Navigation.findNavController(Process.super.getView()).navigate(R.id.fragment_result,bundle);
            }

            //keeps incrementing the progress bar and keeps the loop until we have a valid values for the previous if state
            if (avgRed != 0) {
                progressValue = inc++ / 34;
                statusView.setText("Frame:"+frames+",Progress:"+progressValue+",Time:"+totalTimeInSecs);
                progressBar.setProgress(progressValue);
            }
            processing.set(false);
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) { }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) { }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea) result = size;
                }
            }
        }
        return result;
    }
}
