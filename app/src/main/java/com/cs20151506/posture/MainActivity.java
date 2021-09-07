package com.cs20151506.posture;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    TextView tvLightMax, tvLight, tvPosture, tvTimeDiff;
    TextView tvSumLight, tvAvgLight;
    Button btLightCheck;


    long checkStart = 0;
    long checkEnd = 0;
    long timeDiff = 0;
    int timeCount = 0;
    int cycleCheck = 0;
    float currentLight = 0;
    float sumLight = 0;
    float avgLight = 0;
    float maxLight = 0;
    boolean resetCheck = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* start time check */
        checkStart = System.currentTimeMillis();

        tvLightMax = (TextView)findViewById(R.id.tvLightMax);
        tvLight = (TextView)findViewById(R.id.tvLight);
        tvPosture = (TextView)findViewById(R.id.tvPosture);
        tvTimeDiff = (TextView)findViewById(R.id.tvTimeDiff);
        tvSumLight = (TextView)findViewById(R.id.tvSumLight);
        tvAvgLight = (TextView)findViewById(R.id.tvAvgLight);
        btLightCheck = (Button)findViewById(R.id.btLightCheck);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (lightSensor == null) {
            Toast.makeText(this, "No Light Sensor.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            /* end time check */
            checkEnd = System.currentTimeMillis();
            timeDiff = checkEnd - checkStart;
            currentLight = event.values[0];

            tvLightMax.setText("Light Max : " + String.valueOf(maxLight));
            tvLight.setText("Light : " + String.valueOf(currentLight));
            tvTimeDiff.setText("Timer : " + String.valueOf(timeCount));

            /* update maxLight */
            if (maxLight < currentLight)
                maxLight = currentLight;

            /* maxLight reset */
            btLightCheck.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetCheck = true;

                    maxLight = 0;
                    sumLight = 0;
                    avgLight = 0;

                    tvLightMax.setText("Light Max : " + String.valueOf((int)maxLight));
                    tvSumLight.setText("Sum Value : " + String.valueOf((int)sumLight));
                    tvAvgLight.setText("Avg Value : " + String.valueOf((int)avgLight));
                    tvPosture.setText("Reset posture checking.");

                    checkStart = System.currentTimeMillis();
                }
            });

            /* check time difference by 1s */
            timeCount = (int)timeDiff / 1000;

            if (timeCount > 10)
                resetCheck = false;

            /* working on only each 1s */
            if ((cycleCheck != timeCount) && (resetCheck == false)) {
                cycleCheck = timeCount;
                sumLight += currentLight;
                tvSumLight.setText("Sum Value : " + String.valueOf(sumLight));

                /* predict posture each 10s */
                if ((cycleCheck != 0) && (cycleCheck % 10 == 0)) {
                    avgLight = sumLight / 10;
                    tvAvgLight.setText("Avg Value : " + String.valueOf(avgLight));

                    if (avgLight <= (maxLight*0.4))
                        tvPosture.setText("Type A\nPhone is lain on the desk.");

                    else if (avgLight > (maxLight*0.4) && avgLight <= (maxLight*0.75))
                        tvPosture.setText("Type C\nPhone is gripped perpendicular.");

                    else
                        tvPosture.setText("Type B\nPhone is gripped 60 ~ 70-degree.");

                    sumLight = 0;
                }
            }

            /* reset time at 60s */
            if (timeCount == 60)
                checkStart = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
