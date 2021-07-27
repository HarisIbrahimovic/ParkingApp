package com.example.parkingapp.SelectedFragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parkingapp.Model.Spot;
import com.example.parkingapp.R;
import com.example.parkingapp.ViewModel.MapViewModel;
import java.util.Date;
import java.util.Calendar;


public class SelectedMarker extends Fragment implements iFragment{

    private TextView textView;
    private TextView numberOfSlots;
    private MapViewModel viewModel;
    private Button buyTickets;
    private Spot spot;
    private TextView numberText;
    private iMapBuy parentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_selected_marker, container, false);
        setUpStuff(view);
        onClicks();
        observe();
        checkInternetAcces();
        return view;

    }

    private void checkInternetAcces() {
        if(!checkConnection(getActivity().getApplicationContext())) {
            Toast.makeText(getActivity().getApplicationContext(),"You are offline.",Toast.LENGTH_SHORT).show();
            buyTickets.setText("Offline");
        };
    }

    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }

    private void setUpStuff(View view) {
        textView = view.findViewById(R.id.NameOfMarker);
        numberOfSlots = view.findViewById(R.id.numberOfSlots);
        buyTickets = view.findViewById(R.id.buyTicketsButton);
        numberText = view.findViewById(R.id.textView2);
        viewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        viewModel.init();
    }

    private void onClicks() {
        buyTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkStatus();
            }
        });
    }

    private void checkStatus() {
        if(checkOffline())return;
        else if(checkPenalty())return;
        else if(checkLeave())return;
        else if(checkSpace())return;
        else parentActivity.getData(numberOfSlots.getText().toString(),textView.getText().toString());
    }

    private boolean checkOffline() {
        if(buyTickets.getText().toString().equals("Offline")){
            Toast.makeText(getActivity().getApplicationContext(), "You are offline.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean checkSpace() {
        if(numberOfSlots.getText().toString().equals("0")){
            makeToast("No free space.");
            return true;
        }
        return false;
    }

    private void makeToast(String s) {
        Toast.makeText(getActivity().getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    private boolean checkLeave() {
        if(buyTickets.getText().toString().equals("Leave")){
            spot = null;
            viewModel.leaveParking(textView.getText().toString());
            buyTickets.setText("Buy tickets");
            numberOfSlots.setTextSize(15);
            makeToast("You left.");
            return true;
        }
        return false;
    }

    private boolean checkPenalty() {
        if(buyTickets.getText().toString().equals("Penalty")){
            spot = null;
            makeToast("Credits Taken");
            viewModel.takeCredits();
            viewModel.leaveParking(textView.getText().toString());
            buyTickets.setText("Buy tickets");
            numberOfSlots.setTextSize(15);
            makeToast("You left");
            return true;
        }
        return false;
    }

    private void observe() {
        viewModel.getNumberOfSlots().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(spot ==null){
                    numberText.setText("Total empty Slots: ");
                    numberOfSlots.setText(s);
                }
            }
        });

        viewModel.getStatus().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    buyTickets.setText("Leave");
                }
            }
        });
        viewModel.getPlace().observe(getViewLifecycleOwner(), new Observer<Spot>() {
            @Override
            public void onChanged(Spot cSpot) {
                spot = cSpot;
                if(spot !=null) {
                    textView.setText(spot.getPlace());
                    numberOfSlots.setTextSize(10);
                    numberOfSlots.setText(spot.getsTime());
                    numberText.setText("");
                    compareTimes(spot.getDuration());
                }
            }
        });
    }

    private void compareTimes(Date duration) {
        if(duration==null)return;
        Date date = duration;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Date currentDate = Calendar.getInstance().getTime();
        Calendar cTime = Calendar.getInstance();
        cTime.setTime(currentDate);
        if(c.compareTo(cTime)==-1){
            buyTickets.setText("Penalty");
        }
    }


    @Override
    public void updateInfo(String value) {
        if(spot !=null) {
            makeToast("You are already parked.");
            return;
        }
        textView.setText(value);
        viewModel.setNumberOfSlots(value);
    }

    @Override
    public void setParentActivity(iMapBuy mapBuy) {
        this.parentActivity = mapBuy;
    }


}