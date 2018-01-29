package com.arnm.syph.simplefileexplorer.Utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Syph on 17/12/2017.
 */

public class Extra {

    public static int checkWiFiConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiMgr != null;
        if (wifiMgr.isWifiEnabled()) { // WiFi adapter is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getNetworkId() == -1) {
                return 1; // Not connected to an access-Point
            }
            return 0;      // Connected to an Access Point
        } else {
            return 2; // WiFi adapter is OFF
        }
    }
}
