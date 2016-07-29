package moz.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Laxton-Joe on 2016/7/21.
 */
public class FileHelper {
//    private static Logger logger = LoggerFactory.getLogger(FileHelper.class);
    /*read stream to string*/
    public static String readSteam(InputStream inputStream) {
        String result = null;
        try {
            byte[] buffer = new byte[2048];
            int readBytes = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while ((readBytes = inputStream.read(buffer)) > 0) {
                stringBuilder.append(new String(buffer, 0, readBytes));
            }
            result = stringBuilder.toString();
        } catch (IOException e) {
           // logger.info("catch a empty xml stream");
        }
        return result;
    }


    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }
    public static String getFileName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length() - 1))) {
                return filename.substring(0,dot);
            }
        }
        return filename;
    }
}
