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
package org.cogchar.bind.mio.behavior;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.speech.SpeechService;

import org.cogchar.impl.channel.FancyChannelSpec;
import org.cogchar.blob.emit.GraphChannelNames;
import org.cogchar.impl.perform.PerfChannelNames;

import org.appdapter.fancy.rclient.RepoClient;

/**
 * Current approach requires us to define, for each "Type" of channel:
 * An ChannelType-enum value yielding a build-time URI with a Java service class handle.
   Then each actual ChannelBindingConfig refers to such a type, and binds a flexible
 * "channelID" uri, plus an OSGi filter string.
 * 
 * 
 * 
 * @author Matthew Stevenson <www.cogchar.org> 
 * @author Stub22
 */
public class ChannelBindingConfig {
    private Ident		myChannelID;
    private ChannelType myChannelType;
    private String		myOSGiFilterString;

	public void initFromChannelSpec(FancyChannelSpec cspec) { 
		myChannelID = cspec.getChannelID();
		myChannelType = ChannelType.getByTypeID(cspec.getChannelTypeID());
		myOSGiFilterString = cspec.getOSGiFilterString();
	}
	public void initExplicitly(ChannelType chanType, String chanUri, String osgiFilter) {
		myChannelType = chanType;
		myChannelID = new FreeIdent(chanUri);
		myOSGiFilterString = osgiFilter;
	}
	public String getChannelURI() { 
		return myChannelID.getAbsUriString();
	}
    protected Ident getChannelIdent() { 
		return myChannelID;
	}	
	public String getOSGiFilterString() {
		return myOSGiFilterString;
	}
	public ChannelType getChannelType() {
		return myChannelType;
	}
    @Override public boolean equals(Object obj) {
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
        if ((this.myChannelID == null) ? (other.myChannelID != null) : !this.myChannelID.equals(other.myChannelID)) {
            return false;
        }
        return true;
    }
	@Override public String toString() {
		return getClass().getSimpleName() + "[ id=" + myChannelID + ", type=" + myChannelType + ", filter=" + myOSGiFilterString;
	}
	

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.myChannelType != null ? this.myChannelType.hashCode() : 0);
        hash = 83 * hash + (this.myOSGiFilterString != null ? this.myOSGiFilterString.hashCode() : 0);
        hash = 83 * hash + (this.myChannelID != null ? this.myChannelID.hashCode() : 0);
        return hash;
    }
	
	private static Ident	CHANTYPE_SPEECH_OUT = PerfChannelNames.getOutChanIdent_SpeechMain();
	private static Ident	CHANTYPE_ANIM_OUT = PerfChannelNames.getOutChanIdent_AnimBest();
	private static Ident	CHANTYPE_ANIM_CLC = PerfChannelNames.getOutChanIdent_AnimPerm();
	
	private static Ident	CHANTYPE_WEB_PAGE_OUT = PerfChannelNames.getOutChanIdent_WebPage();
	private static Ident	CHANTYPE_WEB_VIDEO_OUT = PerfChannelNames.getOutChanIdent_WebVideo();
	
	// This one needs to evolve towards being a GraphChanel
	private static Ident	CHANTYPE_WEB_ACTION_IN = PerfChannelNames.getInChanIdent_WebAction();
	
	private static Ident	CHANTYPE_GRAPH_PRIMARY = GraphChannelNames.getChanTypeID_graphPrimary();
	private static Ident	CHANTYPE_GRAPH_SECONDARY = GraphChannelNames.getChanTypeID_graphSecondary();
    
	/* This enum binding is the lynchpin-workaround of our interim approach to channel lifecycle management
	 * 
	 * Another important workaround is the WorkaroundRepoClient transmitted via CCRK_ServiceChannelFactory
	 */
    public static enum ChannelType{
        SPEECH_BLOCK_OUT(CHANTYPE_SPEECH_OUT, SpeechService.class), 
        ANIMATION_PLAYER(CHANTYPE_ANIM_OUT, AnimationPlayer.class),
		ANIMATION_CACHE(CHANTYPE_ANIM_CLC, AnimationPlayer.class),
		// TODO:  Plug in good classes here:
        WEB_PAGE_OUT(CHANTYPE_WEB_PAGE_OUT, AnimationPlayer.class), 
		WEB_VIDEO_OUT(CHANTYPE_WEB_VIDEO_OUT, AnimationPlayer.class), 
		WEB_ACTION_IN(CHANTYPE_WEB_ACTION_IN, AnimationPlayer.class),
		
		GRAPH_PRIMARY(CHANTYPE_GRAPH_PRIMARY, RepoClient.class),
		GRAPH_SECONDARY(CHANTYPE_GRAPH_SECONDARY, RepoClient.class);
				
        
        private	Ident	myChannelTypeID;
        private Class myChannelTypeClass;
        
        private ChannelType(Ident typeID, Class serviceClass){
            myChannelTypeID = typeID;
            myChannelTypeClass = serviceClass;
        }
        public Ident	getTypeID() { 
			return myChannelTypeID;
		}
        public String getURI(){
            return myChannelTypeID.getAbsUriString(); //  myChannelTypeURI;
        }
        
        public Class getServiceClass(){
            return myChannelTypeClass;
        }
        public static ChannelType getByTypeID(Ident channelTypeURI){
            for(ChannelType t : values()){
                if(t.getTypeID().equals(channelTypeURI)){
                    return t;
                }
            }
            return null;
        }        
        public static ChannelType getByURI(String channelTypeURI){
			Ident queryID = new FreeIdent(channelTypeURI);
			return getByTypeID(queryID);
        }
    }
}