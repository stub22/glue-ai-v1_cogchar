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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.cogbot.main.GenRespWithConf;
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Adapter;
import org.jflux.api.core.Listener;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class CogbotProcessor implements Adapter<String, ConvoResponse>{
    private final static String STOP_SCHED_PROMPT = "STOP_ZS_SCHEDULE";
    
    private CogbotCommunicator myCogbotComm;
    private ScheduledExecutorService mySchedule;
    private Listener<String> myInputListener;
    
    public CogbotProcessor(String cogbotUrl){
        myCogbotComm = new CogbotCommunicator(cogbotUrl);
        mySchedule = new ScheduledThreadPoolExecutor(1);
    }
    
    public void setInputListener(Listener<String> inputListener){
        myInputListener = inputListener;
    }
    
    @Override
    public ConvoResponse adapt(String a) {
        if(a == null){
            return null;
        }
        long start = TimeUtils.now();
        if(myCogbotComm == null){
            return null;
        }
        GenRespWithConf resp = myCogbotComm.getResponse(a);
        if(STOP_SCHED_PROMPT.equals(resp.getResponse())){
            mySchedule.shutdownNow();
            mySchedule = new ScheduledThreadPoolExecutor(1);
            resp.setResponse("INTERRUPT_OUTPUT");
        }
        resp.setResponse(trySchedule(resp.getResponse()));
        return new ConvoResponse(
                a, resp.getResponse(), 0, start, TimeUtils.now());
    }
    
    private final static String NEXT_PROMPT = "{NEXT:";
    private String trySchedule(String resp){
        int h = resp.indexOf(NEXT_PROMPT, 0);
        int i = h + NEXT_PROMPT.length();
        int j = resp.indexOf("::", i);
        int k = resp.indexOf("}", j);
        if(h == -1 || j == -1 || k == -1){
            return resp;
        }
        final String prompt = resp.substring(i,j);
        String timeStr = resp.substring(j+2,k);
        long sleep = Long.parseLong(timeStr);
        mySchedule.schedule(
                new Runnable() {
                    @Override public void run() {
                        if(myInputListener == null){
                            return;
                        }
                        myInputListener.handleEvent(prompt);
                    }
                }, sleep, TimeUnit.MILLISECONDS);
        return resp.substring(0,h) + resp.substring(k+1);
    }
}
