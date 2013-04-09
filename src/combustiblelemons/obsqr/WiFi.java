package combustiblelemons.obsqr;

import java.math.BigInteger;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class WiFi {
	private static Context context;
	private final static String tag = "Obsqr E";

	private static WifiConfiguration config;
	private static WifiManager manager;
	
	protected static String SSID;
	private static String PASS;
	protected static String ENCRYPTION;
	
	protected static enum Encryption {
		WEP("WEP"), WPA("WPA"), NONE("nopas");
		String value;
		private Encryption(String s) {
			value = s;
		}
		
	}
	private static boolean hidden;
	
	public WiFi(Context context, String ssid, String pass, String encryption, boolean hidden) {
		WiFi.context = context;
		WiFi.PASS = pass;
		WiFi.SSID = ssid;
		WiFi.ENCRYPTION = encryption;
		WiFi.hidden = hidden;
		manager  = (WifiManager) WiFi.context.getSystemService(Context.WIFI_SERVICE);	
	}
	
	@SuppressWarnings("unused")
	private static String toHex(String arg) {
		return String.format("%x", new BigInteger(1, arg.getBytes()));
	}	
	
	protected boolean enable() {
		EnableWiFi wifi = new EnableWiFi();		
		wifi.execute(config);
		return true;
	}
	
	private static class EnableWiFi extends AsyncTask<WifiConfiguration, Boolean, Boolean> {

		@Override
		protected Boolean doInBackground(WifiConfiguration... params) {			
			if (!manager.isWifiEnabled()) {				
				Log.d(tag, "====================NOT ENABLED=======================");
				if (manager.setWifiEnabled(true)) {
					Log.v("Obsqr", "WiFi enabled");
					Log.d(tag, "===========================================");
				} else {
					Log.v("Obsqr", "WiFi couldn't be enabled");
					return false;
				}
				int count = 0;
				while(!manager.isWifiEnabled()) {
					if (count >=10) {
						Log.d(tag, "Took over 10s to enable");
						return false;
					}
					try {
						Thread.sleep(1000L);						
					} catch (InterruptedException e) {
						
					}
					count++;
				}
			} else {
				Log.d(tag, "====================WIFI IS AREADY ENABLED=======================");
			}
			return true;
		}		
		
		@Override
		protected void onPostExecute(Boolean result) {			
			super.onPostExecute(result);		
			Log.v(tag, "ON POST EXECUTE");
			Log.v(tag, "Network: " + SSID + "\nPass: " + PASS + "\nEncryption: "  + ENCRYPTION + "\nHidden: " + hidden);
			config = freshConfig(SSID, hidden);
			if (ENCRYPTION.equalsIgnoreCase("nopass")) {
				config = configureUnencrypted(config);
				Log.v(tag, "Network is Unencrypted");
			} else if (ENCRYPTION.equalsIgnoreCase("WEP")) {
				Log.v(tag, "Network is WEP");
				config = configureForWEP(config, PASS);
			} else if (ENCRYPTION.equalsIgnoreCase("WPA")) {
				Log.v(tag, "Network is WPA");
				config = configureForWPA(config, PASS);
			}			
			injectNetwork();
		}
	}
	
	private static Integer injectNetwork() {
		Integer knownId = isKnown(manager, config.SSID);
		if (knownId != -1) {
			Log.d(tag, "Removing an old config");
			manager.removeNetwork(knownId);
			manager.saveConfiguration();
		}
		String message = "";
		try {
			
			int newNetworkId = manager.addNetwork(config);
			if (newNetworkId >= 0) {
				Log.d(tag, "Trying to enable network ID = " + newNetworkId + "\tSSID = " + SSID);								
				if (manager.enableNetwork(newNetworkId, true)) {
					message = context.getResources().getString(R.string.alert_wifi_msg_connecting);
					manager.saveConfiguration();
					return 1;
				} else {
					Log.v(tag, "Failed to enable network");
					message = context.getResources().getString(R.string.alert_wifi_msg_enabling_failed);
				}
			} else {
				Log.e(tag, "Couldn't add network " + config.SSID);
				message = context.getResources().getString(R.string.alert_wifi_msg_failed_adding);
				return -1;
			}
		} finally {
			Toast.makeText(context, message + " " + SSID, Toast.LENGTH_LONG).show();
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
	
	private static WifiConfiguration configureUnencrypted(WifiConfiguration cfg) {
		cfg = freshConfig(SSID, hidden);
		cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		return cfg;
	}
	
	private static WifiConfiguration configureForWEP(WifiConfiguration cfg, String pass) {
		cfg = freshConfig(SSID, hidden);
		cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		Log.d(tag, "Configuring WEP with a pass = " + pass);
		cfg.wepTxKeyIndex = 0;
		cfg.wepKeys[0] = quote(pass);			
		return cfg;
	}
	
	private static WifiConfiguration configureForWPA(WifiConfiguration cfg, String pass) {
		cfg = freshConfig(SSID, hidden);
		cfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
		cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		cfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		cfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		cfg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		cfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		Log.d(tag, "Configuring WPA with a pass = " + pass);
		cfg.preSharedKey = quote(pass);
		return cfg;
	}
	
	
	/**
	 * 
	 * @param manager
	 * @param SSID
	 * @return -1 if not found in  the known list of networks
	 */
	private static Integer isKnown(WifiManager manager, String SSID) {
		List<WifiConfiguration> known = manager.getConfiguredNetworks();
		for (WifiConfiguration network : known) {
			if (network.SSID.equals(SSID)) {
				Log.d(tag, SSID + "is already known\n ID = " + network.networkId);
				return network.networkId;
			}
		}	
		Log.d(tag, SSID + " does not appear to be known");
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
