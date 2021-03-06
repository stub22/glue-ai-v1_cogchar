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
import org.jflux.api.common.rk.utils.TimeUtils;
import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.Repeater;
import org.mechio.api.speechrec.SpeechRecEventList;
import org.mechio.api.speechrec.SpeechRecService;
import org.mechio.impl.speechrec.SpeechRecEventListRecord;
import org.mechio.impl.speechrec.SpeechRecEventRecord;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class SpeechRecEngine implements Notifier<SpeechRecEventList>{
    private SpeechRecService mySpeechRec;
    private Repeater myRepeater;
    
    public SpeechRecEngine(){
        myRepeater = new Repeater();
    }

    public void setService(SpeechRecService service){
        if(mySpeechRec != null){
            mySpeechRec.removeSpeechRecListener(myRepeater);
        }
        mySpeechRec = service;
        if(mySpeechRec != null){
            mySpeechRec.addSpeechRecListener(myRepeater);
        }
    }
    
    public void test(String text){
        SpeechRecEventRecord e = new SpeechRecEventRecord();
        e.setRecognizerId("rec");
        e.setRecognizedText(text);
        e.setConfidence(1.0);
        e.setTimestampMillisecUTC(TimeUtils.now());
        
        List<SpeechRecEventRecord> list = new ArrayList<SpeechRecEventRecord>();
        list.add(e);
        
        SpeechRecEventListRecord l = new SpeechRecEventListRecord();
        l.setSpeechRecServiceId("source");
        l.setEventDestinationId("dest");
        l.setSpeechRecEvents(list);
        l.setTimestampMillisecUTC(TimeUtils.now());

        notifyListeners(l);
    }

    @Override
    public void addListener(Listener<SpeechRecEventList> listener) {
        myRepeater.addListener(listener);
    }

    @Override
    public void removeListener(Listener<SpeechRecEventList> listener) {
        myRepeater.removeListener(listener);
    }

    @Override
    public void notifyListeners(SpeechRecEventList e) {
        myRepeater.notifyListeners(e);
    }
}
