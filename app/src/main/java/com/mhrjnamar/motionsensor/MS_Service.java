package com.mhrjnamar.motionsensor;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

public class MS_Service extends IntentService {

    private Sensor mAccelometer;
    private Sensor mGyroscope;
    private SensorManager mSensorManager;
    private float gX, gY, gZ;
    private float aX, aY, aZ;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;


    public MS_Service() {
        super("MS_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        notificationManager = NotificationManagerCompat.from(this);

        notificationBuilder = new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("")
                .setContentText("")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(listener, mAccelometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(listener, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                aX = event.values[0];
                aY = event.values[1];
                aZ = event.values[2];
            } else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gX = event.values[0];
                gY = event.values[1];
                gZ = event.values[2];
            }

            notificationBuilder.setContentText(new StringBuilder().append(getPosition()).append(" ").append(getMovement()));
            notificationManager.notify(0, notificationBuilder.build());

        }

        private String getPosition() {
            if (aY > 7 && aY < 10) {
                return "Standing";
            } else if (aZ > 7) {
                return "Seating";
            } else if (aY > 10) {
                return "Jumping";
            } else if (aY < -5) {
                return "Bending";
            } else if (aX > 7) {
                return "Leaning Left";
            } else if (aX < -7) {
                return "Leaning Right";
            } else {
                return "";
            }
        }


        private String getMovement() {
            if (gY < -0.2) {
                return "Turning Right";
            } else if (gY > 0.2) {
                return "Turning Left";
            } else {
                return "No movement";
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}





