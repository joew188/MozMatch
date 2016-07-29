package moz.listener;

import moz.common.SysConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * Created by Laxton-Joe on 2016/7/15.
 */
public class FileListenerService {

    private static Logger logger = LoggerFactory.getLogger(FileListenerService.class);


    @PostConstruct
    public void  init(){
      //  logger.error(SysConfigUtil.getNowString() +  "Start FileListenerService");
        Thread t = new Thread(new FileListenerRunnable());
        t.setDaemon(true);  //后台线程
        t.start();
    }

    @PreDestroy
    public void  dostory(){

       // logger.error(SysConfigUtil.getNowString() + "Destroy FileListenerService");
    }
}
