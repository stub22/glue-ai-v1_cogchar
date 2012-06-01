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
import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.Repeater;
import org.robokind.api.common.utils.TimeUtils;
import org.robokind.api.speechrec.SpeechRecEvent;
import org.robokind.api.speechrec.SpeechRecEventList;
import org.robokind.api.speechrec.SpeechRecService;
import org.robokind.impl.speechrec.PortableSpeechRecEvent;
import org.robokind.impl.speechrec.PortableSpeechRecEventList;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
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
        SpeechRecEvent e = 
                new PortableSpeechRecEvent("rec", text, 1.0, TimeUtils.now());
        List<SpeechRecEvent> list = new ArrayList<SpeechRecEvent>();
        list.add(e);
        SpeechRecEventList l = 
                new PortableSpeechRecEventList(
                        "source", "dest", list, TimeUtils.now());
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
