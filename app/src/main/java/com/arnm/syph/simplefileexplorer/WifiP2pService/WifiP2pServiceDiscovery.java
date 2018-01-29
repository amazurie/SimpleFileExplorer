package com.arnm.syph.simplefileexplorer.WifiP2pService;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.arnm.syph.simplefileexplorer.R;
import com.arnm.syph.simplefileexplorer.notif.Notification;

import java.util.HashMap;
import java.util.Map;

public class WifiP2pServiceDiscovery extends Service {

    private final IBinder mBinder = new LocalBinder();

    private WifiP2pManager wifiP2pManager;

    static final int SERVER_PORT = 4545;
    public static final String SERVICE_INSTANCE = "simmplefileexplorersharing";
    public static final String SERVICE_TYPE = "_presence._tcp";

    private WifiP2pManager manager;

    private WifiP2pManager.Channel channel;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private ServiceCallBacks serviceCallBacks;

    public WifiP2pServiceDiscovery() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification.showToast(this, "Discovery Starting");

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        startRegistrationAndDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //destroy your service here
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();
        record.put("available", "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_TYPE, record);
        manager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Notification.showToast(WifiP2pServiceDiscovery.this, "discovery Device Success");
            }

            @Override
            public void onFailure(int i) {
                Notification.showToast(WifiP2pServiceDiscovery.this, getString(R.string.discovery_device_add_fail));
            }
        });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Notification.showToast(WifiP2pServiceDiscovery.this, "Added service discovery request");
                        Log.d("DEBUG", "Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d("DEBUG", "ERRORCEPTION: Failed adding service discovery request");
                    }
                });
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Notification.showToast(WifiP2pServiceDiscovery.this, "Service discovery initiated");
                Log.d("DEBUG", "Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Notification.showToast(WifiP2pServiceDiscovery.this, "Failed adding service discovery request");
                Log.d("DEBUG", "Service discovery failed: " + arg0);
            }
        });

        manager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                            Notification.showToast(WifiP2pServiceDiscovery.this, "serviceCallBacks");
                            serviceCallBacks.upDevicesList();
                            // yes it is
                        } else {
                            Notification.showToast(WifiP2pServiceDiscovery.this, "no it isn't");
                            //no it isn't
                        }
                    }
                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Notification.showToast(WifiP2pServiceDiscovery.this, "onDnsSdTxtRecordAvailable");
                        boolean isGroupOwner = device.isGroupOwner();
                        //peerPort = Integer.parseInt(record.get(TransferConstants.KEY_PORT_NUMBER).toString());
                        // further process
                    }
                });
    }

    public void setCallBacks(ServiceCallBacks callbacks) {
        serviceCallBacks = callbacks;
    }

    public class LocalBinder extends Binder {
        /**
         * @return the service you want to bind to : i.e. this
         */
        public WifiP2pServiceDiscovery getService() {
            return (WifiP2pServiceDiscovery.this);
        }
    }
}
