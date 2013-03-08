/*
 * Copyright 2013 The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.behavior;

import org.cogchar.api.perform.Channel;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.speech.SpeechService;


/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ChannelBindingConfig {
    public ChannelType myChannelType;
    public String myOSGiFilterString;
    public String myChannelURI;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChannelBindingConfig other = (ChannelBindingConfig) obj;
        if (this.myChannelType != other.myChannelType) {
            return false;
        }
        if ((this.myOSGiFilterString == null) ? (other.myOSGiFilterString != null) : !this.myOSGiFilterString.equals(other.myOSGiFilterString)) {
            return false;
        }
        if ((this.myChannelURI == null) ? (other.myChannelURI != null) : !this.myChannelURI.equals(other.myChannelURI)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.myChannelType != null ? this.myChannelType.hashCode() : 0);
        hash = 83 * hash + (this.myOSGiFilterString != null ? this.myOSGiFilterString.hashCode() : 0);
        hash = 83 * hash + (this.myChannelURI != null ? this.myChannelURI.hashCode() : 0);
        return hash;
    }
    
    public static enum ChannelType{
        SPEECH("fakeSpeechURI", SpeechService.class), 
        ANIMATION("fakeAnimationURI", AnimationPlayer.class);
        
        private String myChannelTypeURI;
        private Class myChannelTypeClass;
        
        private ChannelType(String uri, Class serviceClass){
            myChannelTypeURI = uri;
            myChannelTypeClass = serviceClass;
        }
        
        public String getURI(){
            return myChannelTypeURI;
        }
        
        public Class getServiceClass(){
            return myChannelTypeClass;
        }
    }
}