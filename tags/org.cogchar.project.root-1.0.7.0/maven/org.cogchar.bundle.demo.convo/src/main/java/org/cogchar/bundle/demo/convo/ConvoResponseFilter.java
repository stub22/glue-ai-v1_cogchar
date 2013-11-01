/*
 * Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

import java.util.ArrayList;
import java.util.List;
import org.cogchar.bind.cogbot.main.GenRespWithConf;
import org.jflux.api.core.Adapter;
import org.jflux.api.core.Listener;
import org.robokind.api.messaging.services.ServiceCommand;
import org.robokind.api.messaging.services.ServiceCommandFactory;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ConvoResponseFilter<R extends GenRespWithConf> implements 
        Adapter<R, R> {
    Listener<ServiceCommand> myCommandListener;
    ServiceCommandFactory myCommandFactory;
    Listener<ServiceCommand> myAnimPromptListener;
    
    public ConvoResponseFilter(Listener<ServiceCommand> commandListener, 
            ServiceCommandFactory commandFactory,
            Listener<ServiceCommand> animPromptListener){
        myCommandListener = commandListener;
        myCommandFactory = commandFactory;
        myAnimPromptListener = animPromptListener;
    }
    
    
    @Override
    public R adapt(R in) {
        if(in == null){
            return null;
        }
        String resp = in.getResponse();
        if(resp.contains("INTERRUPT_OUTPUT")){
            if(myCommandListener != null){
                ServiceCommand cancelSpeech = myCommandFactory.create(
                        "filter", "tts", "cancelSpeech");
                myCommandListener.handleEvent(cancelSpeech);
            }
            return null;
        }
        List<String> animPrompts = extractAnimations(resp);
        if(myAnimPromptListener != null && myCommandFactory != null){
            if(animPrompts.isEmpty()){
                ServiceCommand cmd = 
                        myCommandFactory.create("tts", "anim", "NONE");
                if(cmd != null){
                    myAnimPromptListener.handleEvent(cmd);
                }
            }else{
                for(String prompt : animPrompts){
                    ServiceCommand cmd = 
                            myCommandFactory.create("tts", "anim", prompt);
                    if(cmd == null){
                        continue;
                    }
                    myAnimPromptListener.handleEvent(cmd);
                }
            }
        }
        resp = resp.replaceAll("\\[ANIM_PROMPT\\:([^\\]]*)\\]", "");
        
        resp = resp.replaceAll("unknown partner,", "");
        resp = resp.replaceAll("menevalue= 11", "");
        resp = resp.replaceAll("STOPALLOUTPUT", "");
        resp = resp.replaceAll("RESPONSE_DELIMITER", "");
        
        resp = resp.replaceAll("(\\d+)\\s*-\\s*(\\d+)", "$1 to $2");
        resp = resp.replaceAll("(\\$\\d+\\.\\d\\d)\\d+", "$1");
        
        resp = resp.replaceAll("[Rr][Oo][Bb][Oo][Kk][Ii][Nn][Dd]", "Robo-kind");
        resp = resp.replaceAll("[Aa]utism", "aw-tisum");
        resp = resp.replaceAll("([a-zA-Z0-9]):", "$1, ");
        
        resp = trimPannousSource(resp);
        
        in.setResponse(resp.trim());
        return in;
    }
    
    public static String trimPannousSource(String resp){
        return resp.replaceFirst("\\s*\\([a-zA-Z0-9]+\\.(com|net|org)\\)(\\s*\\.\\s*)?$", "");
    }
    
    private final static String ANIM_PROMPT = "[ANIM_PROMPT:";
    private List<String> extractAnimations(String resp){
        List<String> anims = new ArrayList<String>();
        int j = 0;
        int i = resp.indexOf(ANIM_PROMPT, j);
        while(i >= 0){
            int k = resp.indexOf("]", i);
            if(k == -1){
                break;
            }
            int a = i+ANIM_PROMPT.length();
            String anim = resp.substring(a,k);
            anims.add(anim);
            j=k;
            i = resp.indexOf(ANIM_PROMPT, j);
        }
        return anims;
    }
}
