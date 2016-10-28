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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imvDice;
    private TextView tvScore;

    private SensorManager sensorManager;
    private Sensor mAccelerometer;

    private long lastUpdate = 0;
    private int lastSide = 1;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 200;
    private boolean paused = true;

    private AnimationDrawable animationDrawable;
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
        tvScore = (TextView) findViewById(R.id.tvScore);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register listener for sensorManager
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // Unregister listener for sensorManager
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        // Determine sensor and dice's animation is paused
        if (sensor.getType() != Sensor.TYPE_ACCELEROMETER || !paused)
            return;

        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[1];

        long curTime = System.currentTimeMillis();

        // Ignore events that have occurred less than 200 ms
        if (curTime - lastUpdate > 200) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            // Calculate motion speed
            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                lastSide = getNextDiceSide();
                rollDice(speed);
                Log.d(TAG, "New side is " + lastSide);
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    // Roll dice to the last side
    public void rollDice(final float speed) {

        // Make animation
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Set speed for dice
                if (speed > 800)
                    imvDice.setBackgroundResource(R.drawable.dice_rotation_fast_speed);
                else if (speed > 400)
                    imvDice.setBackgroundResource(R.drawable.dice_rotation_normal_speed);
                else
                    imvDice.setBackgroundResource(R.drawable.dice_rotation_low_speed);

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
                        Toast.makeText(getApplicationContext(), "Speed: " + speed, Toast.LENGTH_SHORT).show();

                        // Set dice to last side
                        imvDice.setBackgroundResource(dices[lastSide - 1]);
                        tvScore.setText("Your score: " + lastSide);
                        Log.d(TAG, "Stop rotating");
                    }
                }, 3000);
            }
        });
    }

    // Get next dice side
    public int getNextDiceSide() {
        Random random = new Random();
        return random.nextInt(5) + 1;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
