package moz.common;
import java.sql.*;
import java.util.HashMap;
/**
 * Created by Laxton-Joe on 2016/7/26.
 */
public   class SqlDBHelper {
    private final static String sDBHost;
    private final static String sDBUsName;
    private final static String sDBPwd;
    private final static String sDBName;
    private static Connection _CONN = null;


    static {
        sDBHost=SysConfigUtil.getSetting("import-response-host");
        sDBUsName=SysConfigUtil.getSetting("import-response-user");
        sDBPwd=SysConfigUtil.getSetting("import-response-pwd");
        sDBName=SysConfigUtil.getSetting("import-response-db");
    }

    private static boolean OpenConnection() {
        if (_CONN != null) {
            return true;
        }
        try {
            String sDriverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            String sDBUrl = "jdbc:sqlserver://" + sDBHost + ";databaseName=" + sDBName;
            Class.forName(sDriverName);
            _CONN = DriverManager.getConnection(sDBUrl, sDBUsName, sDBPwd);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

    //关闭连接
    public static void  CloseConnection() {
        if (_CONN != null) {
            try {
                _CONN.close();
                _CONN = null;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                _CONN = null;
            }
        }
    }

    public static Object GetSingle(String sSQL, Object... objParams)
    {
        try
        {
            OpenConnection();
            PreparedStatement ps = _CONN.prepareStatement(sSQL);
            if (objParams != null)
            {
                for (int i = 0; i < objParams.length; i++)
                {
                    ps.setObject(i + 1, objParams[i]);
                }
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return rs.getString(1);//索引从1开始
            }
        } catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        } finally
        {
            CloseConnection();
        }
        return null;
    }


    public static int UpdateData(String sSQL, Object[] objParams){
        int iResult = 0;
        try {
            OpenConnection();
            PreparedStatement ps = _CONN.prepareStatement(sSQL);
            int count = objParams.length;//获得参数的数量
            for (int j = 1; j <= count; j++) {
                ps.setObject(j, objParams[j - 1]);
            }
            iResult = ps.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return -1;
        } finally {
            CloseConnection();
        }
        return iResult;
    }


}
