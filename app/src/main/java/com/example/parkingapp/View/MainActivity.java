package com.example.parkingapp.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkingapp.R;
import com.example.parkingapp.ViewModel.loginViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private loginViewModel viewModel;
    private TextView upperText;
    private TextView lowerText;
    private EditText password;
    private EditText email;
    private Button button;
    private int state;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpViews();
        checkUser();
        viewModel = ViewModelProviders.of(this).get(loginViewModel.class);
        viewModel.init();
        observe();
        switchText();
        onClicks();
    }


    private void checkUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(MainActivity.this,MapActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkStates() {
        String eEmail = email.getText().toString().trim();
        String ePassword = password.getText().toString().trim();
        if(TextUtils.isEmpty(eEmail)||TextUtils.isEmpty(ePassword)) {
            Toast.makeText(MainActivity.this, "Fill in the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(state==1) viewModel.createUser(eEmail, ePassword);
        else viewModel.loginUser(eEmail, ePassword);


    }

    private void observe() {
        viewModel.getCheckProblemsData().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer==1){
                    viewModel.setCheckProblemsData(0);
                    Toast.makeText(MainActivity.this,"Problems.",Toast.LENGTH_SHORT).show();
                }
                if(integer==2){
                    viewModel.setCheckProblemsData(0);
                    Toast.makeText(MainActivity.this,"Invalid email or password.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getcState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                state=integer;
                switchText();
            }
        });

        viewModel.getUser().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                user= FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null&&state==1){
                    state=-1;
                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                    startActivity(intent);
                    finish();
                }else if(user!=null&&state==0){
                    state=-1;
                    Intent intent = new Intent(MainActivity.this,MapActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }



    private void switchText() {
        if(state==1) {
            upperText.setText("Signup");
            lowerText.setText("Double tap SignUp to Login..");
            button.setText("SignUp");
        }else {
            upperText.setText("Login");
            lowerText.setText("Double tap Login to SignUp..");
            button.setText("Login");
        }
    }

    private void setUpViews() {
        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        button = findViewById(R.id.loginButton);
        upperText = findViewById(R.id.upperText);
        lowerText = findViewById(R.id.lowerText);

    }

    private void onClicks() {
        upperText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.changeState();
                switchText();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkStates();
            }
        });
    }


}