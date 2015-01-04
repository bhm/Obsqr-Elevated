package combustiblelemons.obsqr.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import combustiblelemons.obsqr.R;
import combustiblelemons.obsqr.asyn.AbsAsynTask;

/**
 * Created by hiv on 04.01.15.
 */
public class EnableAndInjectWifiAsyn extends AbsAsynTask<WifiConfiguration, Boolean> {

    public static final String TAG = EnableAndInjectWifiAsyn.class.getSimpleName();
    private WifiManager mManager;

    public EnableAndInjectWifiAsyn(Context context) {
        super(context);
        mManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    protected Boolean call(WifiConfiguration... params) throws Exception {
        for (WifiConfiguration conf : params) {
            if (conf != null) {
                if (enableWifi()) {
                    boolean injected = injectNetwork(conf);
                    publishProgress(conf, injected);
                }
            }
        }
        return null;
    }

    private boolean enableWifi() {
        if (!mManager.isWifiEnabled()) {
            if (mManager.setWifiEnabled(true)) {
                Log.v("Obsqr", "WiFi enabled");
            } else {
                Log.v("Obsqr", "WiFi couldn't be enabled");
                return true;
            }
            int count = 0;
            while (!mManager.isWifiEnabled()) {
                if (count >= 10) {
                    return true;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {

                }
                count++;
            }
        }
        return false;
    }

    private boolean injectNetwork(WifiConfiguration config) {
        Integer knownId = isKnown(mManager, config.SSID);
        if (knownId != -1) {
            Log.d(TAG, "Removing an old config");
            mManager.removeNetwork(knownId);
            mManager.saveConfiguration();
            return true;
        }
        String message = "";
        try {

            int newNetworkId = mManager.addNetwork(config);
            if (newNetworkId >= 0) {
                Log.d(TAG, "Trying to enable network ID = " + newNetworkId + "\tSSID = " + config.SSID);
                if (mManager.enableNetwork(newNetworkId, true)) {
                    message = context.getResources().getString(R.string.alert_wifi_msg_connecting);
                    mManager.saveConfiguration();
                    return true;
                } else {
                    Log.v(TAG, "Failed to enable network");
                    message = context.getResources().getString(R.string.alert_wifi_msg_enabling_failed);
                }
            } else {
                Log.e(TAG, "Couldn't add network " + config.SSID);
                message = context.getResources().getString(R.string.alert_wifi_msg_failed_adding);
                return false;
            }
        } finally {
            Toast.makeText(context, message + " " + config.SSID, Toast.LENGTH_LONG).show();
        }
        return false;
    }


    /**
     * @param mManager
     * @param SSID
     * @return -1 if not found in the known list of networks
     */
    private Integer isKnown(WifiManager mManager, String SSID) {
        List<WifiConfiguration> known = mManager.getConfiguredNetworks();
        for (WifiConfiguration network : known) {
            if (network.SSID.equals(SSID)) {
                Log.d(TAG, SSID + "is already known\n ID = " + network.networkId);
                return network.networkId;
            }
        }
        Log.d(TAG, SSID + " does not appear to be known");
        return -1;
    }


    @Override
    protected boolean onException(Exception e) {
        return false;
    }

    @Override
    protected boolean onSuccess(Boolean result) {
        return false;
    }

    @Override
    public void onProgressUpdate(WifiConfiguration param, Boolean result) {

    }
}
