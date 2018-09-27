package com.deepakkaku.compassapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;

    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;

    TextView readingAzimuth, readingPitch, readingRoll;
    Compass myCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        readingAzimuth = (TextView)findViewById(R.id.azimuth);
        readingPitch = (TextView)findViewById(R.id.pitch);
        readingRoll = (TextView)findViewById(R.id.roll);

        myCompass = (Compass)findViewById(R.id.mycompass);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];

        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];
    }

    @Override
    protected void onResume() {

        sensorManager.registerListener(this,
                sensorAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this,
                sensorMagneticField,
                SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    protected void onPause() {

        sensorManager.unregisterListener(this,
                sensorAccelerometer);
        sensorManager.unregisterListener(this,
                sensorMagneticField);
        super.onPause();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub

        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
               /* for(int i =0; i < 3; i++){
                    valuesAccelerometer[i] = event.values[i];
                }*/
               valuesAccelerometer = lowPass(event.values.clone(), valuesAccelerometer);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                /*for(int i =0; i < 3; i++){
                    valuesMagneticField[i] = event.values[i];
                }*/
                valuesMagneticField = lowPass(event.values.clone(), valuesMagneticField);
                break;
        }

        boolean success = SensorManager.getRotationMatrix(
                matrixR,
                matrixI,
                valuesAccelerometer,
                valuesMagneticField);

        if(success){
            SensorManager.getOrientation(matrixR, matrixValues);

            double azimuth = Math.toDegrees(matrixValues[0]);
            double pitch = Math.toDegrees(matrixValues[1]);
            double roll = Math.toDegrees(matrixValues[2]);

            readingAzimuth.setText("Azimuth: " + String.valueOf(azimuth));
            readingPitch.setText("Pitch: " + String.valueOf(pitch));
            readingRoll.setText("Roll: " + String.valueOf(roll));
            myCompass.update(round(matrixValues[0],2));
        }

    }
    static final float ALPHA = 0.25f;

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
