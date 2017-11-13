package connection.rxconnection.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Created by donydewantrie on 11/6/16.
 */

public final class Utils {

    public static String noSpecialChar = "^[\\w_\\s]+$";
    public static String address = "^[\\w\\s.]+$";
    public static String usernameRegex = "^[\\w_]+$";
    public static String inputForm = "^[\\w,\\s]+$";
    public static String onlyCharUnderScore = "^[a-zA-Z\\s]+$";
    public static String onlyDigits = "^\\d+$";

    public static void openPlayStore(Activity activity) {
        try {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +
                    activity.getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                    ("https://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }

    }

    public final static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
        return "";
    }

    public final static boolean regexUsername(String username) {
        return regex(username, usernameRegex);
    }

    public final static boolean regexOnlyChar(String character) {
        return regex(character, onlyCharUnderScore);
    }

    public final static boolean regexName(String name) {
        return regex(name, onlyCharUnderScore);
    }

    public final static boolean regex(String input, String regex) {
        final Pattern pattern = Pattern.compile(regex);
        return (pattern.matcher(input).matches());
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getMacAddress(){
        try {
            ArrayList<NetworkInterface> all = Collections.
                    list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

 /*   public static <T extends ModelBaseSpinner> T[] newObject(T[] ts, Class<T> tClass) {
        ArrayList<T> arrayList = new ArrayList<>(Arrays.asList(ts));
        ArrayList<T> listT = new ArrayList<>();
        T t = null;
        try {
            t = tClass.newInstance();
            t.setId(0);
            t.setName("-");
            listT.add(t);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        listT.addAll(arrayList);
        return (T[]) listT.toArray(new ModelBaseSpinner[listT.size()]);
    }
*/
}
