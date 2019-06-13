//package com.mhrjnamar.motionsensor.db;
//
//import android.app.Application;
//import android.os.AsyncTask;
//
//import androidx.lifecycle.LiveData;
//
//import java.util.List;
//
//public class SensorRepository {
//    private MotionDao mMotionDao;
//    private LiveData<List<Seating>> mAllSeatingData;
//
//    SensorRepository(Application application){
//        SensorDatabase db = SensorDatabase.getDatabase(application);
//        mMotionDao = db.motionDao();
//        mAllSeatingData = mMotionDao.getAllSeating();
//    }
//
//    public LiveData<List<Seating>> getAllSeatingData() {
//        return mAllSeatingData;
//    }
//
//    public void insert(Seating seating){
//        new insertAsyncTask(mMotionDao).execute(seating);
//    }
//
//    private static class insertAsyncTask extends AsyncTask<Seating,Void,Void>{
//
//        private MotionDao mAsyncTaskDao;
//
//        insertAsyncTask(MotionDao dao){
//            mAsyncTaskDao = dao;
//        }
//
//        @Override
//        protected Void doInBackground(Seating... seatings) {
//            mAsyncTaskDao.insert(seatings[0]);
//            return null;
//        }
//    }
//}
