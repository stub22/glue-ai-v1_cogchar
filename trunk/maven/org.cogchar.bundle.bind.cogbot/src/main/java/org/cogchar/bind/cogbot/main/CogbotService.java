/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.main;

import org.cogchar.bind.cogbot.cogsim.CogbotAvatar;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cogchar.bind.cogbot.cogsim.CogSimConf;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.*;



/**
 *
 * @author Administrator
 */
public class CogbotService {
    transient static PrintWriter stdoutput = new PrintWriter(System.err);
    private final static Logger theLogger = Logger.getLogger(CogbotService.class.getName());
    transient static CogbotService singleton = new CogbotService();
    final CogSimConf simConf = new CogSimConf();
    transient CogbotAvatar singleAvatar;
    transient boolean isCogbotAvailable = false;
    transient boolean isCogbotLocalChanging = false;
    transient boolean killCogbotLocalOnShutdown = false;
    transient Thread shutDownHook = null;
    transient Process localProcess;
    transient Thread localProcessThread = null;
    static boolean COGBOT_LOCAL_CHECKED = false;
    static boolean COGBOT_EC2_CHECKED = false;
    static boolean COGBOT_LOCAL_CHECKED_AFTER_START = false;

    public static void main(String args) {
    }

    private static CogbotService getSingleInstance() {
        return singleton;
    }

    public static CogbotAvatar getDefaultAvatar() {
        return getInstance().getAvatar();
    }

    private CogbotService() {
    }

    private static void echo(String string, Object object) {
        if (object instanceof Throwable) {
            Throwable e = (Throwable) object;
            e.printStackTrace(stdoutput);
            echo(string + e);
            return;
        }
        try {
            echo(string + object);
        } catch (Exception e) {
        }
    }

    private static void echo(String msg) {
        stdoutput.println(msg);
        stdoutput.flush();
    }

    synchronized void startLocalProcess() {
        if (isCogbotLocalChanging) {
            return;
        }
        if (isCogbotAvailable) {
            return;
        }
        shutDownHook = new Thread(new Runnable() {

            @Override
            public void run() {
                CogbotService.this.killLocalProcessNow();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutDownHook);
        localProcessThread = new Thread(new Runnable() {

            @Override
            public void run() {
                CogbotService.this.startLocalProcessNow();
            }
        });
        localProcessThread.start();
    }

    synchronized boolean isRunning() {
        if (isCogbotAvailable) {
            return true;
        }
        ensureAvailable();
        return isCogbotAvailable;
    }

    public static synchronized CogbotService getInstance() {
        CogbotService service = CogbotService.getSingleInstance();
        service.ensureAvailable();
        return service;
    }
    Thread ensureAvail = null;
    final Object starupShutdownLock = new Object();

    void ensureAvailable() {
        synchronized (starupShutdownLock) {
            if (ensureAvail != null) {
                return;
            }
            ensureAvail = new Thread(new Runnable() {

                @Override
                public void run() {
                    ensureAvailable0();
                    synchronized (starupShutdownLock) {
                        ensureAvail = null;
                    }
                }
            });
            ensureAvail.start();
        }
    }

    synchronized void ensureAvailable0() {
        if (isCogbotAvailable) {
            return;
        }
        if (!COGBOT_LOCAL_CHECKED) {
            COGBOT_LOCAL_CHECKED = true;
            if (cogbotPing()) {
                simConf.setIp(getValue(String.class, CONF_COGBOT_IP));
                isCogbotAvailable = true;
                return;
            }
        }
//        if (!COGBOT_EC2_CHECKED) {
//            String ip = config.getProperty(cogbot_url_local, "binabot.gotdns.org");
//            COGBOT_EC2_CHECKED = true;
//            if (cogbotPing()) {
//                simConf.setIp(ip);
//                isCogbotAvailable = true;
//                return;
//            }
//        }
//        startLocalProcess(localIp);
//
//        if (!COGBOT_LOCAL_CHECKED_AFTER_START) {
//            COGBOT_LOCAL_CHECKED_AFTER_START = true;
//            if (cogbotPing()) {
//                simConf.setIp(localIp);
//                isCogbotAvailable = true;
//                return;
//            }
//        }

        echo("NO COGBOT FOUND ANYWHERE? - install it to $hanson-root/cogbot/ ");
        echo("  or change cogbot_url_local in the $hanson-root/config/mene/config.properties");
        COGBOT_LOCAL_CHECKED = false;
//        COGBOT_EC2_CHECKED = false;
//        COGBOT_LOCAL_CHECKED_AFTER_START = false;
    }


    public CogbotResponse getCogbotResponse(CogbotAvatar av, String input, String userName, String botId) {
        return new CogbotResponse(av, stdoutput, input, userName, botId);
    }

    public CogbotResponse getCogbotResponse(CogbotAvatar av, PrintWriter servicePw, String input, String userName, String botName) {
        if (!isRunning()) {
        }
        return new CogbotResponse(av, servicePw, input,  userName, botName);
    }

    public CogbotResponse getCogbotResponse(CogbotAvatar cogbotAvatar, PrintWriter servicePw, String input, String theBotId) {
        return getCogbotResponse(cogbotAvatar, servicePw, input, null, theBotId);
    }

    public void setOutput(PrintWriter servicePw) {
        stdoutput = servicePw;
    }

    public boolean isOnline() {
        return true;
    }

    public void log(Level l, String string, Throwable t) {
        theLogger.log(l, string, t);
    }

    static public Logger getLogger() {
        return theLogger;
    }

    private synchronized CogbotAvatar getAvatar() {
        if (singleAvatar == null) {
            singleAvatar = new CogbotAvatar(this);
        }
        return singleAvatar;
    }

    public PrintWriter getLogPrintWriter() {
        return stdoutput;
    }

    private boolean cogbotPing() {
        try {
            String ip = getValue(String.class, CONF_COGBOT_IP);
            String port = getValue(String.class, CONF_COGBOT_PORT);
            URL url = new URL("http://" + ip + ":" + port + "/ping");
            try {
                url.openConnection().getInputStream().close();
                echo("COGBOT FOUND AT " + ip);
                return true;
            } catch (IOException ex) {
                echo("NO COGBOT FOUND AT " + ip);
                return false;
            }
        } catch (MalformedURLException ex) {
            theLogger.log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public CogSimConf getConf() {
        return simConf;
    }

    synchronized void startLocalProcessNow() {
        try {
            if (isCogbotLocalChanging) {
                return;
            }
            isCogbotLocalChanging = true;
            if (isCogbotAvailable) {
                return;
            }
            isCogbotAvailable = true;
            echo("// CWD = ", new java.io.File(".").getCanonicalPath());
            String dirString = getValue(String.class, OLD_CONF_COBOT_BIN_DIR);
            File dir = new File(dirString);
            if (!dir.exists()) {
                echo("!Exists dir = " + dir.getAbsolutePath());
                failLocalProcessNow();
                return;
            }
            String cmd = "ABuildStartup.exe"; //cmd.exe /c
            String exec = dir + "\\" + cmd;
            localProcess = Runtime.getRuntime().exec(exec, null, dir);
            isCogbotLocalChanging = false;
            isCogbotAvailable = true;
            simConf.setIp(getValue(String.class, CONF_COGBOT_IP));
            String line;

            BufferedReader input =
                    new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
            localProcess.waitFor();
            input.close();
            localProcess.destroy();
            isCogbotAvailable = false;
        } catch (Exception ex) {
            //ex.printStackTrace(stdoutput);
            stdoutput.println("ERROR Starting process " + ex.getMessage());
            Throwable why = ex.getCause();
            if (why != null) {
                stdoutput.println("BECAUSE " + why.getMessage());
            }
            theLogger.log(Level.SEVERE, null, ex);
            failLocalProcessNow();
        }
    }

    synchronized void killLocalProcessNow() {
        try {
            if (!killCogbotLocalOnShutdown) {
                return;
            }
            // so we dont change something
            isCogbotLocalChanging = true;
            isCogbotAvailable = false;
            if (localProcess == null) {
                return;
            }
            localProcess.destroy();
            if (localProcessThread != null) {
                localProcessThread.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace(stdoutput);
            isCogbotAvailable = false;
           theLogger.log(Level.SEVERE, null, ex);
        }
    }

    private void failLocalProcessNow() {
            try {
                localProcess.destroy();
            } catch (Exception e) {
            }
            isCogbotLocalChanging = false;
            isCogbotAvailable = false;
            localProcessThread = null;
            if (shutDownHook != null) {
                Runtime.getRuntime().removeShutdownHook(shutDownHook);
                shutDownHook = null;
            }
    }
}
