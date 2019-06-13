//package com.mhrjnamar.motionsensor.db;
//
//import androidx.lifecycle.LiveData;
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//
//import java.util.List;
//
//@Dao
//public interface MotionDao {
//
//    @Insert
//    void insert(FootSteps steps);
//
//    @Query("DELETE FROM foot_steps")
//    void deleteAll();
//
//   @Insert
//    void insert(Seating seating);
//
//   @Query("DELETE FROM seating")
//    void deleteAllSeating();
//
//   @Query("Select * FROM seating")
//    LiveData<List<Seating>> getAllSeating();
//
//
//
//}
