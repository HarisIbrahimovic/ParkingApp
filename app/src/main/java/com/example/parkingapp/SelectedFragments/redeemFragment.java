package com.example.parkingapp.SelectedFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.parkingapp.R;
import com.example.parkingapp.ViewModel.MapViewModel;


public class redeemFragment extends Fragment implements iFragment{
    private EditText code;
    private Button redeemButton;
    private iMapBuy mapBuy;
    private MapViewModel mapViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_redeem, container, false);
        setUpStuff(view);
        observe();
        return view;
    }

    private void setUpStuff(View view) {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        mapViewModel.init();
        mapBuy.refresh();
        redeemButton = view.findViewById(R.id.reedemButton);
        code = view.findViewById(R.id.codeEditText);
        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapViewModel.reedemCode(code.getText().toString());
            }
        });
    }

    private void observe() {
        mapViewModel.getToastMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s.equals("Empty"))return;
                Toast.makeText(getActivity().getApplicationContext(),s,Toast.LENGTH_SHORT).show();
                mapViewModel.setToastMessage("Empty");
            }
        });
    }

    @Override
    public void updateInfo(String value) {

    }

    @Override
    public void setParentActivity(iMapBuy mapBuy) {
        this.mapBuy = mapBuy;
    }
}