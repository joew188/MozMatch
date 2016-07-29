package moz.common;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Laxton-Joe on 2016/7/15.
 */
public class SysConfigUtil {
    private static final HashMap<String, String> settingMap = new HashMap<String, String>();

    public static String getSetting(String key) {
        return settingMap.get(key).toString();
    }

    public static void setSetting(String k, String v) {
        settingMap.put(k, v);
    }

    public static String getNowString() {
        return(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date());
    }

}
