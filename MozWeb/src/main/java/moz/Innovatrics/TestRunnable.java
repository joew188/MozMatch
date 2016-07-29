package moz.Innovatrics;

import com.innovatrics.afis.client.AbstractTypedResponseListener;
import com.innovatrics.afis.client.AfisClient;
import com.innovatrics.afis.client.EndpointInfo;
import com.innovatrics.afis.client.ExtendedApplicantBuilder;
import com.innovatrics.afis.client.config.MQClientConfiguration;
import com.innovatrics.afis.client.impl.MQClient;
import com.innovatrics.afis.model.Applicant;
import com.innovatrics.afis.model.Header;
import com.innovatrics.afis.model.biometrics.FingerPosition;
import com.innovatrics.afis.model.request.RegisterApplicantRequest;
import com.innovatrics.afis.model.request.RequestMessage;
import com.innovatrics.afis.model.response.ProcessingResult;
import com.innovatrics.afis.model.response.RegisterApplicantResponse;
import com.innovatrics.afis.model.response.ResponseMessage;
import com.innovatrics.afis.model.util.ModelUtils;
import com.innovatrics.afis.model.webafis.Hitlist;
import com.innovatrics.afis.model.webafis.HitlistItem;
import moz.common.FileHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Laxton-Joe on 2016/7/29.
 */
public class TestRunnable implements Runnable {
    private File _directory;
    CountDownLatch finishedLatch = new CountDownLatch(1);

    public TestRunnable(File dir) {
        _directory = dir;
    }

    @Override
    public void run() {
        try {


            ConsumerThread consumer = new ConsumerThread();
            consumer.start();


            Thread.sleep(2000);

            ProducerThread producer = new ProducerThread();
            producer.start();

            // the producer now starts sending the registrations

            // let's wait until producer sends all registrations
            producer.join();

            // and until consumer processes all responses (last one being user_120_2)
            consumer.join();

        } catch (Exception e) {
//            log.error("Exception occured during execution of sample", e);
            System.out.println("Exception occured during execution of sample");
        }
    }

    class ProducerThread extends Thread {
        String device = "Unknown fingerpring scanner";
        int imageResolution = 500;
        ProducerThread() {
            super("producer");
            setDaemon(true);
        }

        Applicant createApplicantRecord() throws IOException {
            ExtendedApplicantBuilder applicantBuilder = new ExtendedApplicantBuilder().externalId(_directory.getName());
            if (_directory.exists()) {

                String[] fileList = _directory.list();
                if (null != fileList) {
                    for (int i = 0; i < fileList.length; i++) {

                        File tempFile = new File(_directory.getPath(), fileList[i]);
                        if (tempFile.isDirectory()==false) {
                           //wsq picture

//                            InputStream is=new FileInputStream(tempFile);
//                            BufferedImage bi= ImageIO.read(is);
//                            Image im=(Image)bi;
//                            imageResolution=   im.getHeight(null)*im.getHeight(null);


                            applicantBuilder.addFingerprint(
                                    tempFile,
                                    device,
                                    imageResolution,
                                    Enum.valueOf(
                                            FingerPosition.class,
                                            FileHelper.getFileName( fileList[i]).toString().replace(' ','_').toUpperCase())
                            );
                        }
                    }
                }
            }
            return applicantBuilder.build();
        }

        public void run() {
            MQClientConfiguration config = AfisClientUtil.createConfiguration();

            // and configure it to producer only mode
            config.setProducerOnly(true);
            config.setPurgeInputQueueOnConnect(false);
            config.setCollectUnprocessedMessages(false);

            AfisClient client = null;

            try {
                // this is also a way to create MQ client without factory
                client = new MQClient(config);

                // create some applicants to register
                Applicant applicant = createApplicantRecord();


                client.registerAsync(applicant);
                // this will wait until all requests are sent, but not for the responses
                client.awaitAll();

                // after this we can finish the producer thread, all request messagess have been successfully sent to the MQ server
                // and the job for this component is done


            } catch (Exception e) {
                System.out.println("Error occured while registering applicants");
                finishedLatch.countDown();
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }
    }


    class ConsumerThread extends Thread {

        ConsumerThread() {
            super("consumer");
            setDaemon(true);
        }

        @Override
        public void run() {

            MQClientConfiguration config = AfisClientUtil.createConfiguration();
            config.setConsumerOnly(true);
            config.setPurgeInputQueueOnConnect(false);
            config.setEagerConnect(true);
            AfisClient client = null;
            try {
                client = new MQClient(config);
                client.addListener(new TestRunnable.ConsumerThread.ConsumerListener());
                finishedLatch.await();
                System.out.println("Listener thread finished");
            } catch (Exception e) {
                System.out.println("Error occured while registering applicants");
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        }

        /**
         * Listener that implements hooks that will handle various incoming events. The events will be handled on an internal MQ client consumer thread (not on
         * the "consumer" thread created by the sample code)
         */
        class ConsumerListener extends AbstractTypedResponseListener {

            // this will be called when we receive a new register applicant response
            public void messageReceived(RegisterApplicantResponse response, EndpointInfo endpoint, byte[] responseMessageRaw, ProcessingResult processingResult,
                                        Header header) {
                Applicant applicant = response.getApplicant();

                List<Long> matchingApplicants = new ArrayList<Long>();
                if (response.getHitlists() != null) {
                    for (Hitlist hl : response.getHitlists()) {
                        for (HitlistItem hli : hl) {
                            matchingApplicants.add(hli.getReferenceApplicantId());
                        }
                    }
                }


            }

            ;

            // handle failures (i.e. well formed XML responses with failure element)
            @Override
            public void failureReceived(EndpointInfo endpoint, byte[] responseMessageRaw, ResponseMessage responseMessage) {


                // failure is an unexpected event, let's finish the sample in this case
                finishedLatch.countDown();
            }

            // override to handle messages that are malformed or otherwise invalid response messages
            @Override
            public void invalidMessageReceived(EndpointInfo endpoint, byte[] responseMessageRaw, Throwable parseException) {

                finishedLatch.countDown();
            }
        }
    }
}



