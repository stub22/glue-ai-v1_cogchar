/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.convoid.broker;

import org.cogchar.convoid.output.exec.context.BehaviorContext;
import org.cogchar.convoid.output.exec.context.BehaviorContext.Detail;
import org.cogchar.convoid.output.exec.context.IBehaviorPlayable;
import org.cogchar.convoid.output.exec.context.StepPlayer;
import java.util.logging.Logger;

/**
 *
 * @author Matt Stevenson
 */
public class RemoteResponseFacade {
    private static Logger theLogger = Logger.getLogger(RemoteResponseFacade.class.getName());
    private static IRemoteResponseInterface	theRemoteResponseIterface;

	public static void setInterface(IRemoteResponseInterface remote){
		theRemoteResponseIterface = remote;
	}

	public static String getResponse(){
		if(theRemoteResponseIterface == null){
			return "";
		}
		return theRemoteResponseIterface.getResponse();
	}

	public static BehaviorContext getResponseBehavior(){
		String resp = getResponse();
        if(resp == null){
            resp = "";
        }
        resp = resp.trim();
        theLogger.severe("Got response from Messaging Nexus: (" + resp + ")");
		if(resp.isEmpty() || resp.equals("null")){
			return BehaviorContext.makeEmpty().and(Detail.REMOTE);
		}
        resp = "<sapi>" + resp + "</sapi>";
        theLogger.severe("Sending Response: (" + resp + ")");
		IBehaviorPlayable player = new StepPlayer(resp);
		return new BehaviorContext().with(player).andActualType("REMOTE_RESPONSE").and(Detail.REMOTE);
	}
}
