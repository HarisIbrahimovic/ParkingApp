package com.example.parkingapp.Repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginRepository {
    private MutableLiveData<FirebaseUser> user = new MutableLiveData<FirebaseUser>();
    private FirebaseUser cUser= FirebaseAuth.getInstance().getCurrentUser();
    private static loginRepository repository;

    public MutableLiveData<FirebaseUser> getUser() {
        user.postValue(cUser);
        user.setValue(cUser);
        return user;
    }

    public static loginRepository getInstance(){
        if(repository==null)repository=new loginRepository();
        return repository;
    }

    public void setcUser(){
        cUser= FirebaseAuth.getInstance().getCurrentUser();
        user.postValue(cUser);
        user.setValue(cUser);
    }
}
