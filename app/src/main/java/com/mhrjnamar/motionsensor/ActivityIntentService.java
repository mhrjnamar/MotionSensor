package com.mhrjnamar.motionsensor;

import android.app.IntentService;
import android.content.Intent;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;


public class ActivityIntentService extends IntentService {
    private static final String TAG = "ActivityIntentService";


    public ActivityIntentService() {
        super("ActivityIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                handleDetectedActivity(result.getProbableActivities());
            }
        }
    }


    private void handleDetectedActivity(List<DetectedActivity> probableActivities) {

        for (DetectedActivity detectedActivity : probableActivities) {
            int confidence = detectedActivity.getConfidence();

            if (confidence > 70) {
                broadcastIntent(getActivityType(detectedActivity));

            }
        }
    }

    public void broadcastIntent(String activityType) {
        Intent intent = new Intent();
        intent.setAction("com.mhrjnamar.motionsensor.ActivityIntent");
        intent.putExtra("Type",activityType);
        sendBroadcast(intent);
    }

    private String getActivityType(DetectedActivity detectedActivity){
        switch (detectedActivity.getType()) {

            case DetectedActivity.IN_VEHICLE: {
                Log.i(TAG, "handleDetectedActivity:IN_VEHICLE " + detectedActivity.getConfidence());
                return "IN_VEHICLE";

            }
            case DetectedActivity.ON_BICYCLE: {
                Log.i(TAG, "handleDetectedActivity:ON_BICYCLE " + detectedActivity.getConfidence());
                return "ON_BICYCLE";

            }
            case DetectedActivity.STILL: {
                Log.i(TAG, "handleDetectedActivity:STILL " + detectedActivity.getConfidence());
                return "STILL";

            }
            case DetectedActivity.UNKNOWN: {
                Log.i(TAG, "handleDetectedActivity:UNKNOWN " + detectedActivity.getConfidence());
                return "UNKNOWN";

            }
            case DetectedActivity.TILTING: {
                Log.i(TAG, "handleDetectedActivity:TILTING " + detectedActivity.getConfidence());
                return "TILTING";
            }
            case DetectedActivity.WALKING: {
                Log.i(TAG, "handleDetectedActivity:WALKING " + detectedActivity.getConfidence());
                return "WALKING";
            }
            case DetectedActivity.RUNNING: {
                Log.i(TAG, "handleDetectedActivity:ON_FOOT " + detectedActivity.getConfidence());
                return "RUNNING";
            }
            default:{
                return "";
            }
        }
    }


}
