package org.cogchar.bind.cogbot.main;



import java.net.*;
import java.io.*;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CogbotCommunicator { // implements INexusService {
    private final static Logger theLogger = Logger.getLogger(CogbotCommunicator.class.getName());
    // this is only called by get getID
    //private String lastKnownUserId = null;
    private String theBotId = "";
    private CogbotService cogbotService;
    static PrintWriter servicePw = new PrintWriter(new Writer() {

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
    private final Properties myProperties;
	CogbotAvatar cogbotAvatar;


    public CogbotCommunicator(String cogbotUrl) {
        myProperties =  new Properties();
        try {
            myProperties.load(new FileReader("./resources/config.properties"));
        } catch (IOException ex) {
            Logger.getLogger(CogbotCommunicator.class.getName()).log(Level.SEVERE, null, ex);
        }
        theBotId = sanitizeId(myProperties.getProperty("robot_fullname","Bina 48"));
     //   lastKnownUserId = sanitizeId(myProperties.getProperty("default_username","UNKNOWN_PARTNER"));
        setBotProperty(CogbotService.cogbot_url_local, cogbotUrl);
        HttpURLConnection.setFollowRedirects(true);
        cogbotService = CogbotService.getInstance(myProperties);
        cogbotService.setOutput(servicePw);
        cogbotAvatar = cogbotService.getDefaultAvatar(myProperties);
    }

    public GenRespWithConf getResponse(String input) {
        CogbotResponse cobotresp = getParsedResponse(input, false, 0);
        String resp = cobotresp == null ? null : cobotresp.getResponse();
        if (resp == null || resp.isEmpty()
                || resp.equals("No response to: " + input + ".")
                || resp.equals(CogbotResponse.NO_RESPONSE)) {
            return new GenRespWithConf("", -1);
        }
        int i9 =  cobotresp.getScore(1,12);
        return new GenRespWithConf(resp, i9);
    }

    private CogbotResponse getParsedResponse(String input, boolean clearingAssoc, int count) {
        CogbotResponse elRes = null;
        try {
            input = sanatizeInput(input);
            elRes = cogbotService.getCogbotResponse(cogbotAvatar, servicePw, myProperties, input, theBotId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (elRes != null) {
            theBotId = sanitizeId(elRes.getBotId());
            /*
            if (lastKnownUserId == null || lastKnownUserId.isEmpty()) {
                lastKnownUserId = sanitizeId(elRes.getUserId());
            }*/
            //Utils.println("Id = " + id + ", logId = " + logId);
            if (!clearingAssoc) {
            }
        }
        return elRes;
    }
    
    protected String sanatizeInput(String rawInput){
        //return myConfig.getFormatter("input").format(input);
        return rawInput;
    }

    private String getResponseString(String input, boolean clearingAssoc, int count) {
        String resp = "";
        String animation = "";
        CogbotResponse elRes = getParsedResponse(input, clearingAssoc, count);
        if (elRes == null) {
            return "";
        }
        theBotId = sanitizeId(elRes.getBotId());
        resp = sanitizeResponse(elRes.getResponse());
        animation = sanitizeAnimation(elRes.getAnimation());
        /*
        if (lastKnownUserId == null || lastKnownUserId.isEmpty()) {
            lastKnownUserId = sanitizeId(elRes.getUserId());
        }*/
        //Utils.println("Id = " + id + ", logId = " + logId);
        if (!clearingAssoc) {
            resp = clearWordAssoc(input, resp, count);
        }
        resp = animation + " " + resp;
        return resp;
    }

    private String sanitizeResponse(String response) {
        response = confSanitizeResponse(response);
        String temp = "";
        boolean modified = false;
        int start = 0, end = 0;
        for (int i = 0; i < response.length(); i++) {
            if (response.charAt(i) == '<') {
                start = i;
                modified = true;
            }
            if (response.charAt(i) == '>') {
                end = i;
                temp = response.substring(start, end + 1);
            }
        }


        if (modified) {
            response = response.replaceAll(temp, "");
        }
        return response;
    }
    
    protected String confSanitizeResponse(String resp){
        //resp = myConfig.getFormatter("str").format(resp);
        //resp = myConfig.getFormatter("partner").format(resp);
        return resp;
    }

    private String sanitizeId(String tempId) {
        if (tempId==null) return "Bina Daxeline";
        tempId = tempId.trim();
        if (tempId.length()==0) tempId = "Bina Daxeline";
        tempId = confSanitizeId(tempId);
        for (int i = 0; i < tempId.length(); i++) {
            if (tempId.charAt(i) == (char) '\"') {
                tempId = tempId.substring(i);
                break;
            }
        }
        return tempId;
    }
    
    protected String confSanitizeId(String id){
        //id = myConfig.getFormatter("id").format(id);
        return id;
    }

    private String sanitizeAnimation(String animation) {
        animation = sanitizeAnim(animation);
        for (int k = 0; k < animation.length(); k++) {
            if (animation.charAt(k) == (char) '\"') {
                animation = animation.substring(0, k);
                break;
            }
        }
        animation = sanitizeAnimList(animation);
        animation = animation.trim();
        if (animation.length() > 0) {
            animation = "<bookmark mark=\"anim:" + animation + "\" />";
        }

        return animation;
    }
    
    protected String sanitizeAnim(String anim){
        //anim = myConfig.getFormatter("animation").format(anim);
        return anim;
    }
    
    protected String sanitizeAnimList(String anim){
        //anim = myConfig.getFormatter("animation_list").format(anim);
        return anim;
    }

    private String clearWordAssoc(String input, String resp, int count) {
        if (true) {
            return resp;
        }
        if (resp.matches(".*[Ww]ord associat.*")
                || resp.matches("^'[A-Za-z.\\- ]*'$")) {
            theLogger.info("Stopping Word Association");
            if (count >= 3) {
                resp = "I could not understand what you said.";
            }
            getResponseString("stop playing word association", true, 0);
            getResponseString("change the subject", true, 0);
            resp = getResponseString(input.replaceAll("[Ww]ord%20associat(e|ion)", ""), false, count + 1);
        }
        return resp;
    }

    public String getServiceName() {
        return "COGBOT";
    }
/*
    public List<INexusService> getChildServices() {
        return new ArrayList<INexusService>();
    }
*/
    public boolean ignoreBatchRequest() {
        return false;
    }

    /*
    public String getId() {
        return lastKnownUserId;
    }*/

    public void setBotProperty(String name, String value) {
        myProperties.setProperty(name, value);
    }

    public void log(Throwable e) {
        e.printStackTrace(servicePw);
    }
}