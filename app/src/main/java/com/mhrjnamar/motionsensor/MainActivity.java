package com.mhrjnamar.motionsensor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 0X01;
    public static final int ALL_PERMISSION_RESULT = 0X02;
    private static final int UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;

    private Sensor mAccelometer;
    private Sensor mGyroscope;
    private Sensor mStepCounter;
    private SensorManager mSensorManager;
    private TextView accText;
    private TextView gyroText;
    private TextView posText;
    private TextView step_counter;
    private TextView locationText;
    private TextView movementText;
    private GoogleApiClient mApiClient;

    private BroadcastReceiver broadcastReceiver;
    private Location mLocation;
    private LocationRequest mLocationRequest;

    private float gX, gY, gZ;
    private float aX, aY, aZ;

    private ArrayList<String> mPermissionToRequest;
    private ArrayList<String> mPermissionRegjected = new ArrayList<>();
    private ArrayList<String> mPermissions = new ArrayList<>();

    private LineChart mLineChart;
    private Thread mThread;
    private boolean isPlotData = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accText = findViewById(R.id.x_text);
        gyroText = findViewById(R.id.y_text);
        posText = findViewById(R.id.z_text);
        step_counter = findViewById(R.id.step_counter);
        locationText = findViewById(R.id.location);
        movementText = findViewById(R.id.movement);

        mLineChart = findViewById(R.id.accelerometer_chart);
        mLineChart.getDescription().setEnabled(true);
        mLineChart.getDescription().setText("Real Time Accelerometer");
        mLineChart.setTouchEnabled(false);
        mLineChart.setDragEnabled(false);
        mLineChart.setScaleEnabled(false);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setPinchZoom(false);
        mLineChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        mLineChart.setData(data);

        Legend l = mLineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis xl = mLineChart.getXAxis();
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(10f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setEnabled(true);

        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(true);

        mPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        mPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        mPermissionToRequest = permissionToRequest(mPermissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mPermissionToRequest.size() > 0) {
                requestPermissions(mPermissionToRequest.toArray(new String[mPermissionToRequest.size()])
                        , ALL_PERMISSION_RESULT);
            }
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mAccelometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mSensorManager.registerListener(this, mAccelometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);

        startService(new Intent(MainActivity.this, MS_Service.class));

        mApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(ActivityRecognition.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();
        mApiClient.connect();


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                movementText.setText(intent.getExtras().getString("Type"));

            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("com.mhrjnamar.motionsensor.ActivityIntent"));


        startPlot();
    }

    private void startPlot() {
        if (mThread != null) {
            mThread.interrupt();
        }

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    isPlotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mApiClient != null) {
            mApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            locationText.setText(new StringBuilder().append("You need to install Google Services to use this App Properly"));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mApiClient != null && mApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, MainActivity.this);
            mApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mThread.interrupt();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(MainActivity.this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(MainActivity.this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }
        return false;
    }

    private ArrayList<String> permissionToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String permission : wantedPermissions) {
            if (!hasPermission(permission)) {
                result.add(permission);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (isPlotData) {
            addEntry(event);
            isPlotData = false;
        }

        Sensor sensor = event.sensor;
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: {
                aX = event.values[0];
                aY = event.values[1];
                aZ = event.values[2];
                accText.setText(String.format(Locale.getDefault(), "X: %.2f\nY: %.2f\nZ: %.2f\n", aX, aY, aZ));
                break;
            }
            case Sensor.TYPE_GYROSCOPE: {
                gX = event.values[0];
                gY = event.values[1];
                gZ = event.values[2];
                gyroText.setText(String.format(Locale.getDefault(), "X: %.2f\nY: %.2f\nZ: %.2f\n", gX, gY, gZ));
                break;
            }
            case Sensor.TYPE_STEP_COUNTER: {
                step_counter.setText(String.valueOf(event.values[0]));
            }
        }

        posText.setText(new StringBuilder().append(getPosition()).append(" ").append(getMovement()));


    }

    private void addEntry(SensorEvent event) {
        LineData data = mLineChart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), event.values[0] + 5), 0);
            data.notifyDataChanged();
            mLineChart.setMaxVisibleValueCount(150);
            mLineChart.moveViewToX(data.getEntryCount());
        }
    }

    private ILineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.GRAY);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private String getPosition() {
        if (aY > 7 && aY < 11) {
            return "Standing";
        } else if (aZ > 7) {
            return "Seating";
        } else if (aY > 11) {
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

        Intent intent = new Intent(MainActivity.this, ActivityIntentService.class);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient,2000, pendingIntent);
        ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(MainActivity.this);
        Task task = activityRecognitionClient.requestActivityUpdates(1000, pendingIntent);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mApiClient);

        if (mLocation != null) {
            locationText.setText(String.format(Locale.getDefault(), "Latitude: %f /n Longitude: %f", mLocation.getLatitude(), mLocation.getLongitude()));
        }
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "You need to enable permissions to display location ! ", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, MainActivity.this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {


        if (mLocation != null) {
            locationText.setText(String.format(Locale.getDefault(), "Latitude: %f \n Longitude: %f", mLocation.getLatitude(), mLocation.getLongitude()));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case ALL_PERMISSION_RESULT: {
                for (String permission : mPermissionToRequest) {
                    if (!hasPermission(permission)) {
                        mPermissionRegjected.add(permission);
                    }
                }

                if (mPermissionRegjected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(mPermissionRegjected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("These permission are mandatory to get your location. You need to allow them.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(mPermissionRegjected.toArray(new String[mPermissionRegjected.size()]),
                                                        ALL_PERMISSION_RESULT);
                                            }

                                        }
                                    }).setNegativeButton("Cancel", null).create().show();
                            return;
                        }
                    }
                } else {
                    if (mApiClient != null) {
                        mApiClient.connect();
                    }
                }

            }
        }
    }
}
