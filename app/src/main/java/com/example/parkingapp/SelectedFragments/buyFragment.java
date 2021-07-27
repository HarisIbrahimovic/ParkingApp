package com.example.parkingapp.SelectedFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkingapp.Model.Place;
import com.example.parkingapp.R;
import com.example.parkingapp.ViewModel.MapViewModel;

import java.util.Calendar;


public class buyFragment extends Fragment implements iFragment{

    private iMapBuy parentActivity;
    private Button submitButton;
    private String placeName;
    private MapViewModel viewModel;
    private String price;
    private TextView priceText;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private int finalTime;
    private int numHours;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy, container, false);
        setUpStuff(view);
        observe();
        onClicks();
        parentActivity.refresh();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkButton(view);
            }
        });
        return view;
    }

    private void setUpStuff(View view) {
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        viewModel.init();
        priceText = view.findViewById(R.id.price);
        radioGroup = view.findViewById(R.id.radioGroup);
        submitButton = view.findViewById(R.id.submitButtonF);
    }

    private void onClicks() {
        submitButton.setOnClickListener(v -> {
            if(radioButton==null){
                Toast.makeText(getActivity().getApplicationContext(),"Select duration first",Toast.LENGTH_SHORT).show();
                return;
            }
            parentActivity.submitData(String.valueOf(numHours));

        });

        radioGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(radioButton==null)return;
            }
        });

    }

    private void observe() {
        viewModel.getMarker(placeName).observe(getViewLifecycleOwner(), new Observer<Place>() {
            @Override
            public void onChanged(Place place) {
                price= place.getPrice();
            }
        });
    }



    public void checkButton(View v){
        int radioId = radioGroup.getCheckedRadioButtonId();
        radioButton= v.findViewById(radioId);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInfo(radioButton.getText().toString());
            }
        });
    }

    private void checkInfo(String hours) {
        String time="1";
        if(hours.equals("One Hour"))time="1";
        else if(hours.equals("Two Hours"))time="2";
        else if(hours.equals("Three Hours"))time="3";
        else if(hours.equals("Six Hours"))time="6";
        else if(hours.equals("Twelve Hours"))time="12";
        else if(hours.equals("Subscription")) {
            time = "Subscription";
            priceText.setText("Buy for free.");
            numHours=-1;
            return;
        };
        numHours = Integer.parseInt(time);
        int numPrice = Integer.parseInt(price);
        finalTime = numHours*numPrice;
        if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>17)finalTime*=2;
        priceText.setText("Current price is "+finalTime+" tickets");

    }

    @Override
    public void updateInfo(String value) {
        placeName= value;
    }

    @Override
    public void setParentActivity(iMapBuy mapBuy) {
        parentActivity= mapBuy;
    }
}