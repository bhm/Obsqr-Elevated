package combustiblelemons.obsqr.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.text.TextUtils;

import java.math.BigInteger;
import java.util.Locale;

import combustiblelemons.obsqr.R;


public class WiFi {
    private final static String tag = "Obsqr E";
    private Encryption        mEncryption;
    private WifiConfiguration config;

    public WiFi(String ssid, String pass, String encryption, boolean hidden) {
        mEncryption = Encryption.fromString(encryption);
        WifiConfiguration fresh = freshConfig(ssid, hidden);
        config = mEncryption.configure(fresh, pass);
    }

    @SuppressWarnings("unused")
    private static String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes()));
    }

    private static String quote(String SSID) {
        if (!SSID.startsWith("\"")) {
            SSID = '\"' + SSID;
            if (!SSID.endsWith("\"")) {
                SSID = SSID + '\"';
            }
        }
        return SSID;
    }

    public static WiFi from(String raw) {
        WiFi wifi = null;
        if (!TextUtils.isEmpty(raw)) {
            String info = raw.substring(5);
            String[] rows = info.split(";");
            String ssid = "";
            String encryption = Encryption.NOPASS.name().toLowerCase(Locale.ENGLISH);
            String pass = "";
            boolean hidden = false;
            for (String row : rows) {
                if (row.split(":").length > 1) {
                    String[] tags = row.split(":");
                    String property = tags[0];
                    String value = tags[1];
                    if ("S".equalsIgnoreCase(property)) {
                        ssid = value;
                    } else if ("P".equalsIgnoreCase(property)) {
                        pass = value;
                    } else if ("H".equalsIgnoreCase(property)) {
                        hidden = Boolean.valueOf(value);
                    } else if ("T".equalsIgnoreCase(property)) {
                        encryption = value;
                        if (WiFi.Encryption.NOPASS.name().equalsIgnoreCase(encryption)) {
                            pass = "";
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(ssid)
                    && !TextUtils.isEmpty(pass)
                    && !TextUtils.isEmpty(encryption)) {
                wifi = new WiFi(ssid, pass, encryption, hidden);
            }
        }
        return wifi;
    }

    private WifiConfiguration freshConfig(String ssid, boolean hidden) {
        WifiConfiguration cfg = new WifiConfiguration();
        cfg.allowedAuthAlgorithms.clear();
        cfg.allowedGroupCiphers.clear();
        cfg.allowedPairwiseCiphers.clear();
        cfg.allowedKeyManagement.clear();
        cfg.allowedProtocols.clear();
        cfg.SSID = quote(ssid);
        cfg.hiddenSSID = hidden;
        return cfg;
    }

    public String toString(Context context) {
        StringBuilder res = new StringBuilder();
        String text = context.getString(R.string.wifi_qr_network_name);
        res.append(text)
                .append(" ")
                .append(config.SSID);
        if (!WiFi.Encryption.NOPASS.name().equals(mEncryption)) {
            res.append(context.getString(R.string.wifi_qr_nopass_for_secured_network));
        }
        return res.toString();
    }

    public boolean enable(Context context) {
        EnableAndInjectWifiAsyn wifi = new EnableAndInjectWifiAsyn(context);
        return wifi.executeCrossPlatform(config) != null;
    }

    public static enum Encryption {
        WEP {
            @Override
            public WifiConfiguration configure(WifiConfiguration conf, String pass) {
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.wepTxKeyIndex = 0;
                conf.wepKeys[0] = quote(pass);
                return conf;
            }
        }, WAP {
            @Override
            public WifiConfiguration configure(WifiConfiguration conf, String pass) {
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.preSharedKey = quote(pass);
                return conf;
            }
        }, NOPASS {
            @Override
            public WifiConfiguration configure(WifiConfiguration conf, String pass) {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                return conf;
            }
        };

        public static Encryption fromString(String val) {
            try {
                return valueOf(Encryption.class, val);
            } catch (IllegalArgumentException e) {
                return NOPASS;
            }
        }

        public abstract WifiConfiguration configure(WifiConfiguration conf, String pass);
    }

}
