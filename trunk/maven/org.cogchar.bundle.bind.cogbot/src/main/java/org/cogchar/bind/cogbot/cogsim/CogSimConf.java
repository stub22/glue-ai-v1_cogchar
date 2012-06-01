/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.cogsim;

//import com.hansonrobotics.mene.Communicator;
//import com.hansonrobotics.mene.config.MeneConfig;
import java.io.Serializable;
import java.util.logging.Logger;
import static org.cogchar.bind.cogbot.osgi.CogbotConfigUtils.*;

/**
 *
 * @author Stu B.
 */
public class CogSimConf implements Serializable {
    private final static Logger theLogger = Logger.getLogger(CogSimConf.class.getName());
    private boolean waiting = true;

    public enum Op {

        GET_SAID,
        GET_HEARD,
        DO_ACTION
    }
    boolean isWaiting() {
        return waiting;
    }
    
    void setWaiting( boolean w) {
        waiting = w;
    }

    /**
     * @return the urlRoot
     */
    public String getUrlRoot() {
        String ip = getValue(String.class, CONF_COGBOT_IP);
        String port = getValue(String.class, CONF_COGBOT_PORT);
        return "http://"+ ip+":" + port;
    }

        /**
     * @return the urlRoot
     */
    public String getChatUrl() {
        return getUrlRoot() + "/?";
    }

    public void setIp(String string) {
        setValue(String.class, CONF_COGBOT_IP, string);
        waiting = false;
    }

    public String findOpURL(Op op) {
        String url = getUrlRoot();
        if (url == null) {
            // return null;
            throw new RuntimeException("Cannot find URL becuase urlRoot=" +url);
        }
        switch (op) {
            case GET_HEARD:
                return url + getValue(String.class, CONF_COGSIM_URL_HEARD_TAIL);
            case GET_SAID:
                return url + getValue(String.class, CONF_COGSIM_URL_SAID_TAIL);
            case DO_ACTION:
                return url + getValue(String.class, CONF_COGSIM_URL_ACTION_TAIL);
            default:
                return "NO_OP_FOR_" + op;
        }
    }

    public boolean isSet(String val) {
        return ((val != null) && (val.length() > 0));
    }

    public boolean isConfigured() {
        return isSet(getUrlRoot()) 
                && isSet(getValue(String.class, CONF_COGSIM_URL_HEARD_TAIL)) 
                && isSet(getValue(String.class, CONF_COGSIM_URL_SAID_TAIL));
    }
}
