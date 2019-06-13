//package com.mhrjnamar.motionsensor.db;
//
//import android.content.Context;
//import android.hardware.SensorManager;
//
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//@Database(entities = {Seating.class}, version = 1)
//public abstract class SensorDatabase extends RoomDatabase {
//
//    public abstract MotionDao motionDao();
//
//    public static volatile SensorDatabase INSTANCE;
//
//    static SensorDatabase getDatabase(final Context context){
//        if (INSTANCE == null){
//            synchronized (SensorDatabase.class){
//                if (INSTANCE == null){
//                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),SensorDatabase.class,"sensor_database")
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
//    }
//}
