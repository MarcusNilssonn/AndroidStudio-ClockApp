package com.example.applicationclock;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.applicationclock.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private TextView textview;
    private TextView textclock;
    private SimpleDateFormat timeFormat;

        @Override
        protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //Get parameters from xml-file
        textview = findViewById(R.id.textview_second);
        textclock = findViewById(R.id.textclock);

        timeFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));

        //Call function for getting time
        UpdateTime();

        //Make function run/update every second
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                UpdateTime();
                }
            }, 1000);
        super.onResume();

    }

    //Set Time based on internet connection or not. If connection set NTP. Else set System.
    private void UpdateTime() {
        if (isNetworkAvailable()) {
            new Thread(new Runnable() { //Run on new thread and not main.
                @Override
                public void run() {
                    try {
                        Date nT = getNetworkTime();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nT.setTime(nT.getTime());
                                String st = timeFormat.format(nT);
                                textclock.setText(st);
                                textview.setText("NTP time:");
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {

            Date systemTime = Calendar.getInstance().getTime();
            String system_time = timeFormat.format(systemTime);
            textclock.setText(system_time);
            textview.setText("System time:");

        }
    }
    //Method for getting the NTP time.
    private Date getNetworkTime() throws IOException {
        NTPUDPClient timeClient = new NTPUDPClient();
        timeClient.setDefaultTimeout(2000);
        TimeInfo timeInfo;

        InetAddress inetAddress = InetAddress.getByName("1.se.pool.ntp.org");
        timeInfo = timeClient.getTime(inetAddress);
        long NTPTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        Date date = new Date(NTPTime);
        System.out.println("Returning NTPtime");
        return date;

    }

    //Check if internet connection.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}