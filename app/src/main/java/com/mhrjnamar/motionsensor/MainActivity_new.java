package com.mhrjnamar.motionsensor;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity_new extends AppCompatActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {
    private Sensor mAccelometer;
    private Sensor mGyroscope;
    private Sensor mStepCounter;
    private SensorManager mSensorManager;

    private GoogleApiClient mApiClient;

    private RecyclerView recyclerView;

    private float gX, gY, gZ;
    private float aX, aY, aZ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mSensorManager.registerListener(this, mAccelometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);

        startService(new Intent(MainActivity_new.this, MS_Service.class));

        mApiClient = new GoogleApiClient.Builder(MainActivity_new.this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(MainActivity_new.this)
                .addOnConnectionFailedListener(MainActivity_new.this)
                .build();
        mApiClient.connect();


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        switch (sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:{
                aX = event.values[0];
                aY = event.values[1];
                aZ = event.values[2];
                //accText.setText(String.format(Locale.getDefault(), "X: %.2f\nY: %.2f\nZ: %.2f\n", aX, aY, aZ));
                break;
            }case Sensor.TYPE_GYROSCOPE:{
                gX = event.values[0];
                gY = event.values[1];
                gZ = event.values[2];
                //gyroText.setText(String.format(Locale.getDefault(), "X: %.2f\nY: %.2f\nZ: %.2f\n", gX, gY, gZ));
                break;
            }case Sensor.TYPE_STEP_COUNTER:{
                //gyroText.setText(String.valueOf(event.values[0]));
            }
        }

        //posText.setText(new StringBuilder().append(getPosition()).append(" ").append(getMovement()));


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



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(MainActivity_new.this,ActivityIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity_new.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient,2000, pendingIntent);
        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(MainActivity_new.this);
        Task task = activityRecognitionClient.requestActivityUpdates(1000, pendingIntent);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    private class SensorListAdaper extends RecyclerView.Adapter<SensorListAdaper.SensorViewHolder>{


        @NonNull
        @Override
        public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SensorViewHolder(LayoutInflater.from(MainActivity_new.this).inflate(R.layout.sensor_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class SensorViewHolder extends RecyclerView.ViewHolder {
            public SensorViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
