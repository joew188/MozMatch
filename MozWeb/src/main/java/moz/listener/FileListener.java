package moz.listener;

import moz.Innovatrics.RegisterRunnable;
import moz.Innovatrics.TestRunnable;
import moz.common.SqlDBHelper;
import moz.common.SysConfigUtil;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Laxton-Joe on 2016/7/15.
 */
public class FileListener implements FileAlterationListener {
 static final ExecutorService threadPool = Executors.newFixedThreadPool(Integer.parseInt(SysConfigUtil.getSetting("import-thread-num")));

 // @Resource
  //  QueueSender queueSender;
    @Override
    public void onStart(FileAlterationObserver observer) {

    }

    @Override
    public void onDirectoryCreate(File directory) {

          //  threadPool.execute(new RegisterRunnable(file));
          threadPool.execute(new TestRunnable(directory));

    }

    @Override
    public void onDirectoryChange(File directory) {

    }

    @Override
    public void onDirectoryDelete(File directory) {

    }

    @Override
    public void onFileCreate(File file) {
//            if (file.exists()) {
//              threadPool.execute(new RegisterRunnable(file));
//             //   threadPool.execute(new TestRunnable(file));
//            }
    }

    @Override
    public void onFileChange(File file) {

    }

    @Override
    public void onFileDelete(File file) {

    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }
}
