/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.unused;

import org.cogchar.bind.cogbot.cogsim.CogbotAvatar;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cogchar.bind.cogbot.main.CogbotResponse;
import org.cogchar.bind.cogbot.main.CogbotService;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.*;

/**
 *
 * @author Stu Baurmann
 */
public class CogbotLoadTest {
    private final static Logger theLogger = Logger.getLogger(CogbotLoadTest.class.getName());
    PrintWriter myServicePW;
    CogbotAvatar myAvatar;

    public static void main(String args[]) {
        theLogger.info("Starting Cogbot Load Test");
        try {
            setValue(Boolean.class, CONF_COGSIM_JMX_ENABLED, false);
            CogbotLoadTest test = new CogbotLoadTest();
            test.start();
        } catch (Throwable t) {
            theLogger.log(Level.SEVERE, "main caught exception", t);
        }
        theLogger.info("Finished Cogbot Load Test startup");
    }

    public CogbotLoadTest() {
        myServicePW = makePrintWriter();
        CogbotAvatar csa = CogbotService.getDefaultAvatar();
        myAvatar = csa;
    }

    public void start() {
        Thread loadThread1 = new Thread(makeRunnable("thr1", 2));
        // Thread loadThread2 = new Thread(makeRunnable(csa, "thr2", 100));
        loadThread1.start();
        // loadThread2.start();
    }

    public Runnable makeRunnable(final String uniqueLabel, final int loopCnt) {
        return new Runnable() {

            @Override
            public void run() {
                try {
                    testDialogLoop(uniqueLabel, loopCnt);
                } catch (Throwable t) {
                    theLogger.log(Level.SEVERE, 
                            "Thread with label " + uniqueLabel 
                            + " caught exception", t);
                }
            }
        };
    }

    public void testDialogLoop(String uniqueLabel, int loopCnt) {
        for (int i = 1; i <= loopCnt; i++) {
            String label = uniqueLabel + "-iter-" + i;
            long iterStartStamp = System.currentTimeMillis();
            testDialog("LoaderA " + uniqueLabel, label + "A");
            testDialog("LoaderB " + uniqueLabel, label + "B");
            testDialog("LoaderC " + uniqueLabel, label + "C");
            long iterLengthStamp = System.currentTimeMillis() - iterStartStamp;
            theLogger.log(Level.INFO, "iter {0} took {1} msec", 
                    new Object[]{i, iterLengthStamp});
        }
    }

    public void testDialog(String userName, String label) {
        CogbotAvatar csa = myAvatar;
        myAvatar.setLookingAt(userName);
        testResponse("hello", label);
        testResponse("who am i", label);
        testResponse("howdy", label);
        testResponse("who was napoleon", label);
        testResponse("what is my name", label);
        testResponse("what is the topic", label);
        testResponse("goodbye", label);
    }

    public void testResponse(String input, String label) {
        CogbotAvatar csa = myAvatar;
        CogbotService cs = csa.service;
        CogbotResponse elRes = cs.getCogbotResponse(
                csa, myServicePW, input,
                getValue(String.class, OLD_CONF_COGBOT_NAME));
        theLogger.log(Level.INFO, "Response[input=''{0}'', label=''{1}'']: {2}",
                new Object[]{input, label, elRes.toString()});
    }

    public static PrintWriter makePrintWriter() {
        PrintWriter csPW = new PrintWriter(new Writer() {

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                theLogger.info(new String(cbuf, off, len));
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        });
        return csPW;
    }
}
