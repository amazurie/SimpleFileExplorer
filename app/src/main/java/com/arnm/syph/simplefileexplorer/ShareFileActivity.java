package com.arnm.syph.simplefileexplorer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.arnm.syph.simplefileexplorer.Utils.Extra;
import com.arnm.syph.simplefileexplorer.WifiP2pService.ServiceCallBacks;
import com.arnm.syph.simplefileexplorer.WifiP2pService.WifiP2pServiceDiscovery;
import com.arnm.syph.simplefileexplorer.notif.Notification;

import java.util.ArrayList;
import java.util.List;

public class ShareFileActivity extends AppCompatActivity implements ServiceCallBacks {

    SharedPreferences sharedPrefs;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private WifiP2pServiceDiscovery wifiService;
    Intent wifiP2pServiceDiscoveryIntent = null;
    private boolean bound = false;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        toolbar = findViewById(R.id.shareToolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sharedPrefs.getBoolean(getString(R.string.key_auto_start_discovery), false)){
            // bind to Service
            Intent intent = new Intent(this, WifiP2pServiceDiscovery.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from service
        if (bound) {
            wifiService.setCallBacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    public void upDevicesList() {
        Notification.showToast(this, "Testing setCallBack success stopping service");
        stopService(wifiP2pServiceDiscoveryIntent);
    }

    /** Callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            WifiP2pServiceDiscovery.LocalBinder binder = (WifiP2pServiceDiscovery.LocalBinder) service;
            wifiService = binder.getService();
            bound = true;
            wifiService.setCallBacks(ShareFileActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public void startWifiDirectServiceDiscovery(){

        int i;

        if (!checkAndRequestAllReqPermissions(true)){
            Notification.showToast(this, getString(R.string.discovery_perm_error));
            return ;
        }
        if ((i = Extra.checkWiFiConnected(ShareFileActivity.this)) == 0 || i == 1) {

            wifiP2pServiceDiscoveryIntent = new Intent(this, WifiP2pServiceDiscovery.class);
            bindService(wifiP2pServiceDiscoveryIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else if (i == 2){

            final AlertDialog wifiNotEnable = new AlertDialog.Builder(this).create();
            wifiNotEnable.setTitle(R.string.app_name);
            wifiNotEnable.setMessage(getString(R.string.wifi_needed));
            wifiNotEnable.setButton(AlertDialog.BUTTON_NEGATIVE, this.getString(R.string.btn_cancel), (dialogInterface, i1) -> wifiNotEnable.dismiss());
            wifiNotEnable.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", (dialogInterface, i12) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
            wifiNotEnable.show();
        }
    }

    private boolean checkAndRequestAllReqPermissions(boolean demand) {
        int awifi = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        int cwifi = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE);
        int anetstate = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        int cnetstate = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE);
        int net = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int phonestate = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (awifi != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_WIFI_STATE);
        if (cwifi != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.CHANGE_WIFI_STATE);
        if (anetstate != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_NETWORK_STATE);
        if (cnetstate != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.CHANGE_NETWORK_STATE);
        if (net != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.INTERNET);
        if (phonestate != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(android.Manifest.permission.READ_PHONE_STATE);
        if (demand && !listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return checkAndRequestAllReqPermissions(false);
        }
        return listPermissionsNeeded.isEmpty();
    }
}
