package combustiblelemons.obsqr;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.List;

public class WiFi {
    private Context context;
    private final static String TAG = "Obsqr E";

    public WiFi(Context context) {
        this.context = context;
    }

    @SuppressWarnings("unused")
    private static String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes()));
    }

    void enable(String ssid, String pass, String encryption, boolean hidden) {
        final WifiInfo wifiInfo = new WifiInfo(ssid, pass, WifiEncryption.Companion.fromName(encryption), hidden);
        final WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final EnableWiFi wifi = new EnableWiFi(context, manager, wifiInfo);
        wifi.execute((Void[]) null);
    }

    private static class EnableWiFi extends AsyncTask<Void, Boolean, Boolean> {

        private Context context;
        private WifiManager manager;
        private WifiInfo wifiInfo;

        EnableWiFi(Context context, WifiManager manager, WifiInfo wifiInfo) {
            this.context = context;
            this.manager = manager;
            this.wifiInfo = wifiInfo;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!manager.isWifiEnabled()) {
                Log.d(TAG, "====================NOT ENABLED=======================");
                if (manager.setWifiEnabled(true)) {
                    Log.v("Obsqr", "WiFi enabled");
                    Log.d(TAG, "===========================================");
                } else {
                    Log.v("Obsqr", "WiFi couldn't be enabled");
                    return false;
                }
                int count = 0;
                while (!manager.isWifiEnabled()) {
                    if (count >= 10) {
                        Log.d(TAG, "Took over 10s to enable");
                        return false;
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ignored) {
                    }
                    count++;
                }
            } else {
                Log.d(TAG, "====================WIFI IS AREADY ENABLED=======================");
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            final String ssid = wifiInfo.getSsid();
            final boolean hidden = wifiInfo.getHidden();
            final String pass = wifiInfo.getPass();
            final WifiEncryption encryption = wifiInfo.getEncryption();
            if (encryption == WifiEncryption.NO_PASS) {
                injectNetwork(
                        context,
                        manager,
                        configureUnencrypted(ssid, hidden)
                );

            } else if (encryption == WifiEncryption.WEP) {
                injectNetwork(
                        context,
                        manager,
                        configureForWEP(pass, ssid, hidden)
                );

            } else if (encryption == WifiEncryption.WPA) {
                injectNetwork(
                        context,
                        manager,
                        configureForWPA(pass, ssid, hidden)
                );
            }
        }
    }

    private static Integer injectNetwork(Context context, WifiManager manager, WifiConfiguration wifiConfiguration) {
        final String ssid = wifiConfiguration.SSID;
        final Integer knownId = isKnown(manager, wifiConfiguration.SSID);
        if (knownId != -1) {
            Log.d(TAG, "Removing an old config");
            manager.removeNetwork(knownId);
            manager.saveConfiguration();
        }
        String message = "";
        try {

            int newNetworkId = manager.addNetwork(wifiConfiguration);
            if (newNetworkId >= 0) {
                Log.d(TAG, "Trying to enable network ID = " + newNetworkId + "\tssid = " + ssid);
                if (manager.enableNetwork(newNetworkId, true)) {
                    message = context.getResources().getString(R.string.alert_wifi_msg_connecting);
                    manager.saveConfiguration();
                    return 1;
                } else {
                    Log.v(TAG, "Failed to enable network");
                    message = context.getResources().getString(R.string.alert_wifi_msg_enabling_failed);
                }
            } else {
                Log.e(TAG, "Couldn't add network " + wifiConfiguration.SSID);
                message = context.getResources().getString(R.string.alert_wifi_msg_failed_adding);
                return -1;
            }
        } finally {
            Toast.makeText(context, message + " " + ssid, Toast.LENGTH_LONG).show();
        }
        return -1;
    }

    private static WifiConfiguration freshConfig(String ssid, boolean _hidden) {
        WifiConfiguration cfg = new WifiConfiguration();
        cfg.allowedAuthAlgorithms.clear();
        cfg.allowedGroupCiphers.clear();
        cfg.allowedPairwiseCiphers.clear();
        cfg.allowedKeyManagement.clear();
        cfg.allowedProtocols.clear();
        cfg.SSID = quote(ssid);
        cfg.hiddenSSID = _hidden;
        return cfg;
    }

    private static WifiConfiguration configureUnencrypted(String ssid, boolean hidden) {
        final WifiConfiguration cfg = freshConfig(ssid, hidden);
        cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return cfg;
    }

    private static WifiConfiguration configureForWEP(String pass, String ssid, boolean hidden) {
        final WifiConfiguration cfg = freshConfig(ssid, hidden);
        cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        Log.d(TAG, "Configuring WEP with a pass = " + pass);
        cfg.wepTxKeyIndex = 0;
        cfg.wepKeys[0] = quote(pass);
        return cfg;
    }

    private static WifiConfiguration configureForWPA(String pass, String ssid, boolean hidden) {
        final WifiConfiguration cfg = freshConfig(ssid, hidden);
        cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
        cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        cfg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        cfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        Log.d(TAG, "Configuring WPA with a pass = " + pass);
        cfg.preSharedKey = quote(pass);
        return cfg;
    }

    /**
     * @param manager
     * @param ssid
     * @return -1 if not found in the known list of networks
     */
    private static Integer isKnown(WifiManager manager, String ssid) {
        List<WifiConfiguration> known = manager.getConfiguredNetworks();
        for (WifiConfiguration network : known) {
            if (network.SSID.equals(ssid)) {
                Log.d(TAG, ssid + "is already known\n ID = " + network.networkId);
                return network.networkId;
            }
        }
        Log.d(TAG, ssid + " does not appear to be known");
        return -1;
    }

    private static String quote(String SSID) {
        if (!SSID.startsWith("\"")) {
            SSID = '\"' + SSID;
            if (!SSID.endsWith("\""))
                SSID = SSID + '\"';
        }
        return SSID;
    }

}
