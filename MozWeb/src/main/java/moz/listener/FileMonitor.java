package moz.listener;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * Created by Laxton-Joe on 2016/7/15.
 */
public class FileMonitor {
    FileAlterationMonitor monitor = null;

    public FileMonitor(long interval) throws Exception {
        monitor = new FileAlterationMonitor(interval);
    }

    public void monitor(String path, FileAlterationListener listener) {
//        FileAlterationObserver observer = new FileAlterationObserver(new File(path),
//                FileFilterUtils.and(
//                        FileFilterUtils.fileFileFilter()
//                        ,FileFilterUtils.suffixFileFilter(".vre")
//                ));
        FileAlterationObserver observer = new FileAlterationObserver(new File(path));
        observer.addListener(listener);
        monitor.addObserver(observer);

    }

    public void stop() throws Exception {
        monitor.stop();
    }

    public void start() throws Exception {
        monitor.start();
    }
}