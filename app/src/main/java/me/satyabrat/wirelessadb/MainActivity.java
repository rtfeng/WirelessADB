/**
 * Jmz Wireless ADB
 * <p>
 * Copyright 2016 by Jmz Software <support@jmzsoftware.com>
 * <p>
 * <p>
 * Some open source application is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * <p>
 * Some open source application is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package me.satyabrat.wirelessadb;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {

    Context context;
    TextView textView;
    TextView textView1;
    ToggleButton adb;
    ToggleButton nadb;
    String adbState;
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        adb = (ToggleButton) findViewById(R.id.button);
        nadb = (ToggleButton) findViewById(R.id.button1);
        textView = (TextView) findViewById(R.id.textView);
        textView1 = (TextView) findViewById(R.id.textView1);

        adbState = checkAdb().trim();
        if (adbState.equalsIgnoreCase("running")) {
            adb.setText(R.string.disable_adb);
            nadb.setChecked(true);
        }
        updateStatus();
        nadb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (nadb.isChecked()) {
                    // The toggle is enabled
                    enableNadb();
                    updateStatus();
                    adb.setChecked(true);
                    Log.e(LOG_TAG, "enable");
                } else {
                    // The toggle is disabled
                    disableAdb();
                    updateStatus();
                    adb.setChecked(false);
                    Log.e(LOG_TAG, "disable");
                }
            }
        });

        adb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adb.isChecked()) {
                    // The toggle is enabled
                    enableAdb();
                    adb.setText(R.string.disable_adb);
                    nadb.setChecked(true);
                    updateStatus();
                    Log.e(LOG_TAG, "enable");
                } else {
                    // The toggle is disabled
                    disableAdb();
                    adb.setText(R.string.enable_adb);
                    updateStatus();
                    nadb.setChecked(false);
                    Log.e(LOG_TAG, "disable");
                }
            }
        });

    }

    public void updateStatus() {
        adbState = checkAdb().trim();
        textView.setText(adbState);
        textView1.setText(adbState);
        Log.e(LOG_TAG, adbState);
    }

    public String checkAdb() {
        final String commands = "getprop init.svc.adbd";
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec(commands);
            // Reads stdout.
            // NOTE: You can write to stdin of the command using
            //       process.getOutputStream().
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();

            // Waits for the command to finish.
            process.waitFor();
            Log.e(LOG_TAG, output.toString());
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void enableAdb() {
        final String[] commands = {"setprop service.adb.tcp.port 5555", "stop adbd", "start adbd"};
        Thread runSu = new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(commands);
            }
        });
        runSu.start();
        try {
            runSu.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void enableNadb() {
        final String[] commands = {"stop adbd", "setprop ctl.start adbd",};
        Thread runSu = new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(commands);
            }
        });
        runSu.start();
        try {
            runSu.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disableAdb() {
        final String[] commands = {"stop adbd"};
        Thread runSu = new Thread(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run(commands);
            }
        });
        runSu.start();
        try {
            runSu.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getIP() {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ip = mWifiManager.getConnectionInfo().getIpAddress();
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);

    }
}
