package moz.Innovatrics;

import com.innovatrics.afis.client.AbstractTypedResponseListener;
import com.innovatrics.afis.client.AfisClient;
import com.innovatrics.afis.client.EndpointInfo;
import com.innovatrics.afis.client.ExtendedApplicantBuilder;
import com.innovatrics.afis.client.config.MQClientConfiguration;
import com.innovatrics.afis.client.impl.MQClient;
import com.innovatrics.afis.model.Applicant;
import com.innovatrics.afis.model.Header;
import com.innovatrics.afis.model.biometrics.TemplateFormat;
import com.innovatrics.afis.model.request.RegisterApplicantRequest;
import com.innovatrics.afis.model.request.RequestMessage;
import com.innovatrics.afis.model.response.ProcessingResult;
import com.innovatrics.afis.model.response.RegisterApplicantResponse;
import com.innovatrics.afis.model.response.ResponseMessage;
import com.innovatrics.afis.model.util.ModelUtils;
import com.innovatrics.afis.model.webafis.Hitlist;
import com.innovatrics.afis.model.webafis.HitlistItem;
import moz.common.FileHelper;
import moz.common.SqlDBHelper;
import moz.common.SysConfigUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Laxton-Joe on 2016/7/21.
 */
public class RegisterRunnable implements Runnable {
    private File _file;
    CountDownLatch finishedLatch = new CountDownLatch(1);

    public RegisterRunnable(File file) {
        _file = file;
    }

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

        ProducerThread() {
            super("producer");
            setDaemon(true);
        }

        private Applicant createApplicant(String externalId) throws IOException {
            ExtendedApplicantBuilder applicantBuilder = new ExtendedApplicantBuilder().externalId(externalId);
            InputStream inputStream = new FileInputStream(_file);
            byte[] fileBytes = FileHelper.input2byte(inputStream);
            inputStream.close();
            applicantBuilder.addFingerprint(fileBytes, TemplateFormat.ICS);
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
                Applicant applicant = createApplicant(_file.getName());


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
                client.addListener(new ConsumerListener());
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

        class ConsumerListener extends AbstractTypedResponseListener {

            // this will be called when we receive a new register applicant response
            public void messageReceived(RegisterApplicantResponse response,
                                        EndpointInfo endpoint,
                                        byte[] responseMessageRaw,
                                        ProcessingResult processingResult,
                                        Header header) {
                Applicant applicant = response.getApplicant();

                StringBuilder sqlBuilder = new StringBuilder();
                //     List<Long> matchingApplicants = new ArrayList<Long>();
                // ArrayList<Object> paramList = new ArrayList<Object>();


                if (response.getHitlists() != null) {
                    for (Hitlist hl : response.getHitlists()) {
                        for (HitlistItem hli : hl) {
                            //   matchingApplicants.add(hli.getReferenceApplicantId());
                            sqlBuilder.append("INSERT INTO AFIS_Result (PId,ProbeId,RId,ReferenceId,Score,IsActive) VALUES("
                                    + String.valueOf(hli.getProbeApplicantId()) + ",'"
                                    + hli.getProbeApplicant().getExternalId() + "',"
                                    + String.valueOf(hli.getReferenceApplicantId()) + ",'"
                                    + hli.getReferenceApplicant().getExternalId() + "',"
                                    + String.valueOf(hli.getScore()) + ",0);");

//                            sqlBuilder.append("INSERT INTO AFIS_Result (PId,ProbeId,ReferenceId,Score,IsActive) VALUES(?,?,?,0);"
//                                    +String.valueOf(hli.getScore())+",0);");

                        }
                    }
                }
                if (sqlBuilder.length() > 0) {
                    SqlDBHelper.UpdateData(sqlBuilder.toString(), new ArrayList<Object>().toArray());
                }
            }

            ;

            // handle failures (i.e. well formed XML responses with failure element)
            @Override
            public void failureReceived(EndpointInfo endpoint, byte[] responseMessageRaw, ResponseMessage responseMessage) {
                System.out.println("Failure received from " + endpoint);

                ProcessingResult processingResult = responseMessage.getProcessingResult();
                System.out.println("Result code: " + processingResult.getResultCode());
                System.out.println("Result code description: " + processingResult.getResultCodeDescription());
                System.out.println("Result message: " + processingResult.getResultMessage());

                // stack trace is an internal information, but very usefull, when asking for support from Innovatrics
                System.out.println("Server stacktrace: " + processingResult.getStacktrace());

                // this should be a full copy of the original request message
                String originalMessageXML = processingResult.getOriginalMessage();
                try {
                    // we could even parse the message to it's object model representation
                    RequestMessage originalMessage = ModelUtils.parseRequest(originalMessageXML.getBytes("UTF-8"));
                    System.out.println("Original message type: " + originalMessage.getHeader().getType());

                    RegisterApplicantRequest request = originalMessage.getRegisterApplicant();
                    if (request != null) {
                        Applicant requestApplicant = request.getApplicant();
                        System.out.println("External ID used in request: " + requestApplicant.getExternalId());
                    }
                } catch (Exception e) {
                    System.out.println("Error while parsing the original message");
                }

                // failure is an unexpected event, let's finish the sample in this case
                finishedLatch.countDown();
            }

            // override to handle messages that are malformed or otherwise invalid response messages
            @Override
            public void invalidMessageReceived(EndpointInfo endpoint, byte[] responseMessageRaw, Throwable parseException) {
                System.out.println("Invalid message received from " + endpoint);

                // failure is an unexpected event, let's finish the sample in this case
                finishedLatch.countDown();
            }
        }
    }

}
