package moz.Innovatrics;


import com.innovatrics.afis.client.AfisClient;
import com.innovatrics.afis.client.AfisClientFactory;
import com.innovatrics.afis.client.ExtendedApplicantBuilder;
import com.innovatrics.afis.client.config.MQClientConfiguration;

import java.io.File;

/**
 * Created by Laxton-Joe on 2016/7/21.
 */
public class AfisClientUtil {

    public static AfisClient createClient() {
        return new AfisClientFactory(createConfiguration()).createClient();
    }
    public static MQClientConfiguration createConfiguration() {
        return MQClientConfiguration.load(getClientConfigurationFile());
    }
    private static File getClientConfigurationFile() {
        String propertyFilePath = "D:\\code\\client\\java\\sample\\client.properties";
//        if (propertyFilePath == null) {
//            throw new IllegalStateException("Please specify system property " + PROP_CLIENT_PROPERTIES_FILE);
//        }
        File propertyFile = new File(propertyFilePath);
        if (!propertyFile.exists() && !propertyFile.isFile()) {
            throw new IllegalStateException("Property file " + propertyFilePath + " doesn't exist or is not a file");
        }
        return propertyFile;
    }
}
