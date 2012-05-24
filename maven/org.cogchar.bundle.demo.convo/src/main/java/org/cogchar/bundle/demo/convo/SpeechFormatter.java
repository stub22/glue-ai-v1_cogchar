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

import org.jflux.api.core.util.Adapter;
import org.robokind.api.common.utils.TimeUtils;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.impl.speech.PortableSpeechRequest;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class SpeechFormatter implements Adapter<String, SpeechRequest> {
    private String mySourceId;
    private String myDestId;
    
    public SpeechFormatter(String sourceId, String destId){
        if(sourceId == null || destId == null){
            throw new NullPointerException();
        }
        mySourceId = sourceId;
        myDestId = destId;
    }
    
    @Override
    public SpeechRequest adapt(String a) {
        return new PortableSpeechRequest(
                        mySourceId, myDestId, TimeUtils.now(), a);
    }
    
}
