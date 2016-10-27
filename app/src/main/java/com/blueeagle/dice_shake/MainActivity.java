package com.blueeagle.dice_shake;

import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    private long lastUpdate = 0;
    private int lastSide = 1;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 400;
    private boolean paused = true;

    private AnimationDrawable animationDrawable;
    private ImageView imvDice;
    private int[] dices = new int[]{
            R.drawable.d1,
            R.drawable.d2,
            R.drawable.d3,
            R.drawable.d4,
            R.drawable.d5,
            R.drawable.d6
    };

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        imvDice = (ImageView) findViewById(R.id.imvDice);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        if (sensor.getType() != Sensor.TYPE_ACCELEROMETER || !paused)
            return;

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[1];

        long curTime = System.currentTimeMillis();

        // Ignore events that have occurred less than 400 ms
        if (curTime - lastUpdate > 400) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            // Calculate motion speed
            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                lastSide = getNextDiceSide();
                rollDice();
                Log.d(TAG, "New side is " + lastSide);
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    // Roll dice to the last side
    public void rollDice() {

        // Make animation
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imvDice.setBackgroundResource(R.drawable.dice_rotation);
                animationDrawable = (AnimationDrawable) imvDice.getBackground();

                // Start animation
                animationDrawable.start();
                paused = false;
                Log.d(TAG, "Rotating...");

                // Stop animation after 3s
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationDrawable.stop();
                        paused = true;

                        // Set dice to last side
                        imvDice.setBackgroundResource(dices[lastSide - 1]);
                        Log.d(TAG, "Stop rotating");
                    }
                }, 3000);
            }
        });
    }

    public int getNextDiceSide() {
        Random random = new Random();
        int num = random.nextInt(5) + 1;

        while (num == lastSide) {
            num = random.nextInt(5) + 1;
        }

        return num;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
