/*
 * Copyright 2012 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bundle.demo.convo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.core.Adapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.robokind.api.common.utils.TimeUtils;

/**
 *
 * @author Matthew Stevenson
 */
public class PannousProcessor implements Adapter<String, ConvoResponse>{
    private final static Logger theLogger = Logger.getLogger(PannousProcessor.class.getName());
    private String myLocation;
    private String myLanguage;
    private String myHashedId;
    
    protected int myTimeout = 5000;
    
    public PannousProcessor(String location, int timeout){
        myTimeout = timeout;
        myLocation = location == null ? "" : location;
    }
    
    @Override
    public ConvoResponse adapt(String input) {
        String requestURL = buildRequestURL(input);
        long start = TimeUtils.now();
        String resultStr = makeRequest(requestURL);
        long end = TimeUtils.now();
        String resp = getResponseString(resultStr);
        return new ConvoResponse(input, resp, 9, start, end);
    }
    
    public String getResponseString(String resultStr) {
        try {
            if (resultStr == null || resultStr.length() == 0) {
                theLogger.log(Level.INFO, "Pannous returned empty response.");
                return "";
            }

            JSONArray outputJson = new JSONObject(resultStr).getJSONArray("output");
            if (outputJson.length() == 0) {
                theLogger.log(Level.INFO, "Pannous returned empty json response.");
                return "";
            }

            JSONObject firstHandler = outputJson.getJSONObject(0);
            if (firstHandler.has("errorMessage") 
                    && firstHandler.getString("errorMessage").length() > 0) {
                theLogger.log(Level.WARNING, "Server side error: {0}", 
                        firstHandler.getString("errorMessage"));
                return null;
            }
            String text = getResponseText(firstHandler);
            int end = text.lastIndexOf("..");
            if(end >= 0){
                text = text.substring(0, end);
            }
            theLogger.log(Level.INFO, "Received text:{0}", text);
            return text;
        } catch (Exception ex) {
            theLogger.log(Level.WARNING, "Problem while parsing json.", ex);
            return "";
        }
    }

    private String getResponseText(JSONObject firstHandler) throws JSONException {
        JSONObject actions = firstHandler.getJSONObject("actions");
        if(!actions.has("say")) {
            return null;
        }
        Object obj = actions.get("say");
        if (!(obj instanceof JSONObject)){
            return obj.toString();
        }
        JSONObject sObj = (JSONObject) obj;
        String text = sObj.getString("text");
        text += getMoreText(sObj);
        return text;
    }

    private String getMoreText(JSONObject sObj) {
        if(sObj == null){
            return "";
        }
        if(!sObj.has("moreText")){
            return "";
        }
        try{
            StringBuilder sb = new StringBuilder();
            JSONArray arr = sObj.getJSONArray("moreText");
            if(arr == null){
                return "";
            }
            for (int i = 0; i < arr.length(); i++) {
                if(sb.length() > 0){
                    sb.append(" ");
                }
                String s = arr.getString(i);
                if(s == null){
                    continue;
                }
                sb.append(s);
            }
            return sb.toString();
        }catch(JSONException ex){
            theLogger.log(Level.WARNING, "JSON Parse Exception for: " + sObj, ex);
            return "";
        }
    }

    private String buildRequestURL(String input) {
        try {
            input = URLEncoder.encode(input, "UTF-8");
        } catch (Exception ex) {
        }
        int timeZoneInMinutes = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000 / 60;
        // TODO add ping https://weannie.pannous.com/ping
        // TODO add more client features: open-url, reminder, ...
        // see API Documentation & demo at https://weannie.pannous.com/demo/
        String voiceActionsUrl = "https://weannie.pannous.com/api?input=" + input
                + "&clientFeatures=say"
                //
                + "&locale=" + myLanguage
                //
                + "&timeZone=" + timeZoneInMinutes
                //
                + "&location=" + myLocation
                // TODO use your production key here!
                + "&login=test-user";
        return voiceActionsUrl;
    }
    
    private String makeRequest(String url){
        try {
            theLogger.log(Level.INFO, 
                    "Sending Pannous Request URL: {0}", url);
            URLConnection conn = new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(myTimeout);
            conn.setConnectTimeout(myTimeout);
            conn.setRequestProperty("Set-Cookie", "id=" + myHashedId
                    + ";Domain=.pannous.com;Path=/;Secure");
            return streamToString(conn.getInputStream(), "UTF-8");
        } catch (Exception ex) {
            theLogger.log(Level.WARNING, "Unable request URL.", ex);
            return null;
        }
    }

    public static String streamToString(InputStream is, String encoding)
            throws IOException {
        try {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(
                    buffer.length);
            int numRead;
            while ((numRead = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, numRead);
            }
            return outStream.toString(encoding);
        } finally {
            is.close();
        }
    }

    private List<String> parseImageURLs(JSONObject actions) throws JSONException {
        if (!actions.has("show") 
                || !actions.getJSONObject("show").has("images")){
            return Collections.EMPTY_LIST;
        }
        List<String> imageUrls = new ArrayList<String>();
        JSONArray arr = actions.getJSONObject("show").getJSONArray("images");
        for (int i = 0; i < arr.length(); i++) {
            imageUrls.add(arr.getString(i));
        }
        return imageUrls;
    }
}
