package moz.listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Created by Laxton-Joe on 2016/7/15.
 */
public class FileListenerService {
    private String  message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @PostConstruct
    public void  init(){
        Thread t = new Thread(new FileListenerRunnable());
        t.setDaemon(true);  //后台线程
        t.start();
    }

    @PreDestroy
    public void  dostory(){
        System.out.println("I'm  destory method  using  @PreDestroy....."+message);
    }
}
