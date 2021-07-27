package com.example.parkingapp.ViewModel;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkingapp.Model.User;
import com.example.parkingapp.Repository.SettingsRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class SettingsViewModel extends ViewModel {
    private SettingsRepository repository;
    private MutableLiveData<User> currentUser;
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private MutableLiveData<Integer> Accepted = new MutableLiveData<>();
    private MutableLiveData<Boolean> deleted = new MutableLiveData<>();

    public void init(){
        if(currentUser!=null)return;
        repository = SettingsRepository.getInstance();
        currentUser = repository.getCurrentUser();
        toastMessage.setValue("None");
        deleted.setValue(false);
        Accepted.setValue(0);
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Boolean> getDeleted() {
        return deleted;
    }

    public void updatePassword(String oldPassword, String newPassword) {
        if(TextUtils.isEmpty(oldPassword)||TextUtils.isEmpty(newPassword)){
            toastMessage.setValue("Fill in the fields");
            return;
        }
        if(newPassword.length()<8){
            toastMessage.setValue("New password too short.");
            return;
        }
        if(oldPassword.equals(currentUser.getValue().getPassword())){
            FirebaseUser cUser = FirebaseAuth.getInstance().getCurrentUser();
            cUser.updatePassword(newPassword);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(cUser.getUid()).child("Password");
            databaseReference.setValue(newPassword);
            toastMessage.setValue("Password Changed");
            return;
        }
        toastMessage.setValue("Incorrect old password.");
    }


    public void deleteAccount(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(user.getUid());
        databaseReference.removeValue();
        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleted.setValue(true);
            }
        });
        toastMessage.setValue("Account removed.");
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void setToastMessage(String none) {
        this.toastMessage.setValue(none);
    }

    public void buySub() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ParkingUsers").child(id).child("Credits");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String credit = snapshot.getValue(String.class);
                int i = Integer.parseInt(credit);
                if(i-200>0){
                    Accepted.postValue(1);
                    Accepted.setValue(1);
                    databaseReference.setValue(String.valueOf(i-200));
                    toastMessage.setValue("Subscribed for 30 days.");
                    repository.buySub();
                }else{
                    Accepted.postValue(2);
                    Accepted.setValue(2);
                    toastMessage.setValue("Not enough credits");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
