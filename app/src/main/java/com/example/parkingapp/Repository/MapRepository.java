package com.example.parkingapp.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.parkingapp.Model.Place;
import com.example.parkingapp.Model.Spot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class MapRepository {
    static MapRepository instance;
    private MutableLiveData<String> numberOfSlots = new MutableLiveData<>();
    private MutableLiveData<Boolean> status = new MutableLiveData<>();
    private MutableLiveData<Spot> place = new MutableLiveData<>();
    private MutableLiveData<Place> marker = new MutableLiveData<>();

    public MutableLiveData<Place> getMarker(String name) {
        setMarker(name);
        return marker;
    }

    public static MapRepository getInstance(){
        if(instance==null)instance= new MapRepository();
        return instance;
    }

    public MutableLiveData<String> getNumberOfSlots(String value) {
        setNumberOfSlots(value);
        return numberOfSlots;
    }

    public MutableLiveData<Boolean> getStatus(){
        setStatus();
        return status;
    }

    public MutableLiveData<Spot> getPlace() {
        setPlace();
        return place;
    }

    private boolean checkUser() {
        if(FirebaseAuth.getInstance().getCurrentUser()==null)return true;
        return false;
    }

    private void setNumberOfSlots(String value) {
        if(value.equals("Empty"))value="Mejdan";
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Spots").child(value).child("Slots");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numberOfSlots.postValue(snapshot.getValue(String.class));
                numberOfSlots.setValue(snapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void leaveParking(String s) {
        if(checkUser())return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("TakenSpots").child(userId);
        databaseReference.removeValue();
        databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Spots").child(s).child("Slots");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int num = Integer.parseInt(snapshot.getValue(String.class));
                num++;
                addSlot(s,num);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addSlot(String s, int num) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Spots").child(s).child("Slots");
        databaseReference.setValue(String.valueOf(num));
    }

    public void setStatus() {
        if(checkUser())return;
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("TakenSpots");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    Spot spot1 = snapshot1.getValue(Spot.class);
                    if(spot1.getUserId().equals(id)){
                        status.postValue(true);
                        status.setValue(true);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



    public void setPlace() {
        if(checkUser())return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("TakenSpots");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    Spot spot1 = snapshot1.getValue(Spot.class);
                    if(spot1.getUserId().equals(userId)){
                        place.postValue(spot1);
                        place.setValue(spot1);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }




    public void removePlace( String spotName, String freeSpots, int hours) {
        String newValue = freeSpots;
        int number = Integer.parseInt(newValue);
        number--;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Spots").child(spotName).child("Slots");
        databaseReference.setValue(String.valueOf(number));
        takeSpot(spotName, hours);
    }

    private void takeSpot(String spotName, int hours) {
        if(checkUser())return;
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Date calendar = Calendar.getInstance().getTime();
        Calendar c = Calendar.getInstance();
        c.setTime(calendar);
        c.add(Calendar.HOUR_OF_DAY,hours);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("TakenSpots").child(id);
        databaseReference.child("userId").setValue(id);
        databaseReference.child("duration").setValue(c.getTime());
        databaseReference.child("sTime").setValue(c.getTime().toString());
        databaseReference.child("place").setValue(spotName);
    }

    private void setMarker(String name) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Spots");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Place p1;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    p1= dataSnapshot.getValue(Place.class);
                    if(p1.getName().equals(name)){
                        marker.postValue(p1);
                        marker.setValue(p1);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



}

