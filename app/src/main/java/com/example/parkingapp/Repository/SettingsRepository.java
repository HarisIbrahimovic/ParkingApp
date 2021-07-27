package com.example.parkingapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.parkingapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class SettingsRepository {
    static SettingsRepository instance;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    public static SettingsRepository getInstance() {
        if(instance==null)instance = new SettingsRepository();
        return instance;
    }

    public MutableLiveData<User> getCurrentUser() {
        setCurrrentUser();
        return currentUser;
    }

    private void setCurrrentUser() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser.postValue(snapshot.getValue(User.class));
                        currentUser.setValue(snapshot.getValue(User.class));
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        }
        );
    }

    public void buySub() {
        Date calendar = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(calendar);
        c.add(Calendar.HOUR_OF_DAY,720);
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(id).child("duration");
        databaseReference.setValue(c.getTime());
    }

}
