package com.example.parkingapp.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkingapp.Model.User;
import com.example.parkingapp.R;
import com.example.parkingapp.ViewModel.SettingsViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    private TextView numberOfCredits;
    private EditText email;
    private EditText oldPassword;
    private EditText newPassword;
    private Button saveChanges;
    private Button deleteAccount;
    private Button monthlySub;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpStuff();
        observe();
        onClicks();
    }

    private void onClicks() {
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String OldPassword = oldPassword.getText().toString().trim();
                String NewPassword = newPassword.getText().toString().trim();
                viewModel.updatePassword(OldPassword,NewPassword);
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.deleteAccount();
                Toast.makeText(getApplicationContext(),"Account removed",Toast.LENGTH_SHORT).show();
            }
        });
        monthlySub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.buySub();
            }
        });
    }

    private void observe() {
        viewModel.getCurrentUser().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if(user==null)return;
                email.setText(user.getEmail());
                numberOfCredits.setText("Credits: "+user.getCredits());
            }
        });
        viewModel.getToastMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equals("None"))return;;
                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                viewModel.setToastMessage("None");
            }
        });
        viewModel.getDeleted().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void setUpStuff() {
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);
        viewModel.init();
        numberOfCredits = findViewById(R.id.creditsValue);
        email = findViewById(R.id.settEmail);
        oldPassword = findViewById(R.id.oldPassSet);
        newPassword = findViewById(R.id.newPassword);
        saveChanges = findViewById(R.id.saveChanges);
        deleteAccount = findViewById(R.id.DeleteAccount);
        monthlySub = findViewById(R.id.monthlySub);
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(),MapActivity.class));
        finish();
    }


}