package com.example.parkingapp.ViewModel;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkingapp.Model.Code;
import com.example.parkingapp.Model.Place;
import com.example.parkingapp.Model.Spot;
import com.example.parkingapp.Repository.MapRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class MapViewModel extends ViewModel {
    private MapRepository repository;
    private MutableLiveData<String> numberOfSlots;
    private MutableLiveData<Boolean> status;
    private MutableLiveData<Spot> place;
    private MutableLiveData<Integer> Accepted= new MutableLiveData<>();
    private MutableLiveData<Place> marker = new MutableLiveData<>();
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public MutableLiveData<Place> getMarker(String name) {
        marker = repository.getMarker(name);
        return marker;
    }

    public void init(){
        repository = MapRepository.getInstance();
        if(numberOfSlots!=null||status!=null||place!=null)return;
        numberOfSlots = repository.getNumberOfSlots("Mejdan");
        status= repository.getStatus();
        place = repository.getPlace();
        Accepted.postValue(0);
        Accepted.setValue(0);
        toastMessage.postValue("Empty");
        toastMessage.setValue("Empty");
    }

    public LiveData<String> getToastMessage() {return toastMessage;}
    public LiveData<String> getNumberOfSlots() {
        return numberOfSlots;
    }
    public LiveData<Integer> getAccepted() { return Accepted;}
    public LiveData<Boolean> getStatus() {return status;}
    public LiveData<Spot> getPlace() {return place;}
    public void leaveParking(String s) {
        repository.leaveParking(s);
    }

    public void setNumberOfSlots(String value) {
        numberOfSlots = repository.getNumberOfSlots(value);
    }

    public void setAccepted(int accepted) {
        Accepted.postValue(accepted);
        Accepted.setValue(accepted);
    }

    public void setToastMessage(String value){
        toastMessage.postValue(value);
        toastMessage.setValue(value);
    }

    public void takeSpot(String spotName, String freeSpotNum, String finalHours) {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(id).child("Credits");
        if(finalHours.equals("-1")){
            checkMonthSub(spotName, freeSpotNum, String.valueOf(24));
            return;
        }
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String credit = snapshot.getValue(String.class);
                int i = Integer.parseInt(credit);
                int j = Integer.parseInt(finalHours);
                if(i-j*2>0){
                    repository.removePlace(spotName,freeSpotNum, j);
                    Accepted.postValue(1);
                    Accepted.setValue(1);
                    databaseReference.setValue(String.valueOf(i-j));

                }else{
                    Accepted.postValue(2);
                    Accepted.setValue(2);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void checkMonthSub(String spotName, String freeSpotNum, String finalHours) {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(id).child("duration");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Date date = snapshot.getValue(Date.class);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                Date currentDate = Calendar.getInstance().getTime();
                Calendar cTime = Calendar.getInstance();
                cTime.setTime(currentDate);
                if(c.compareTo(cTime) > 0){
                    int j = Integer.parseInt(finalHours);
                    repository.removePlace(spotName,freeSpotNum, j);
                    setAccepted(1);
                    setToastMessage("Spot taken.");
                    return;
                }
                Accepted.postValue(3);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void takeCredits() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(userId).child("Credits");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                int num = Integer.parseInt(value);
                num-=10;
                databaseReference.setValue(String.valueOf(num));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void reedemCode(String sCode) {
        if(sCode==null)return;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Codes");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Code code;
                if (!snapshot.hasChild(sCode)) {
                    setToastMessage("Code does not exist");
                    return;
                }
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    code = dataSnapshot.getValue(Code.class);
                    if(code.getId().equals(sCode)){
                        addCredits(code.getCredits(),code.getId());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void addCredits(String codeCredits, String codeId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReferenceC = FirebaseDatabase.getInstance().getReference("ParkingApp").child("Codes").child(codeId);
        databaseReferenceC.removeValue();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(userId).child("Credits");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                int num = Integer.parseInt(value);
                int numCredits = Integer.parseInt(codeCredits);
                num+=numCredits;
                databaseReference.setValue(String.valueOf(num));
                setToastMessage(numCredits+" credits added.");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
