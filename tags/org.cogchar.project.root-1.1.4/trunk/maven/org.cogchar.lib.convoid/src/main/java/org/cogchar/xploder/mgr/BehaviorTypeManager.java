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

package org.cogchar.xploder.mgr;

import org.cogchar.xploder.cursors.IConvoidCursor;
import org.cogchar.api.convoid.act.Category;
import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext;
import org.cogchar.convoid.player.SpeechPlayer;
import org.cogchar.convoid.player.IBehaviorPlayable;
import java.util.logging.Logger;
import org.cogchar.convoid.job.SpeechJob;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Matt Stevenson
 */
public class BehaviorTypeManager {
    private static Logger theLogger = Logger.getLogger(BehaviorTypeManager.class.getName());
	private Class				myJobClass;
	private CursorGroup         myGroup;
	private SpeechJobFactory	myFactory;
	private String              myBehaviorType;
	private Category			myCategory;
    private IConvoidCursor      myLastPlayed;
    private Boolean             myImplicitFetch;

	public BehaviorTypeManager(Category rootCat, String categoryName, SpeechJobFactory factory, Boolean fetch) {
        myCategory = rootCat.findSubCategory(categoryName);
		myFactory = factory;
		myBehaviorType = myFactory.getBehaviorType();
		myGroup = myFactory.buildCursorGroup(myCategory);
		myJobClass = myFactory.getJobClass();
        myImplicitFetch = fetch;
	}

	public CursorGroup getCursorGroup(){
		return myGroup;
	}

	public Class getJobClass(){
		return myJobClass;
	}

	public SpeechJobFactory getFactory(){
		return myFactory;
	}

	public String getBehaviorType(){
		return myBehaviorType;
	}

	public void setLastPlayed(SpeechJob job){
		myLastPlayed = job.getCategoryCursor();
	}

	public SpeechJob getLastPlayed(){
		return myGroup.getJobForCursor(myLastPlayed);
	}

    public BehaviorContext getMoreToSay(){
        if(myLastPlayed == null){
            return BehaviorContext.makeEmpty();
        }else if(moreToSay()){
            SpeechJob job = getLastPlayed();
            Step step = myLastPlayed.getBestStepAtTime(TimeUtils.currentTimeMillis());
            if(step != null){
				IBehaviorPlayable player = new SpeechPlayer(step, job);
				return new BehaviorContext().with(player).andActualType(myBehaviorType);
            }
        }
        return BehaviorContext.makeEmpty();
    }

    public boolean moreToSay(){
        return myLastPlayed.isPlayableAtTime(TimeUtils.currentTimeMillis());
    }

    public Boolean getImplicitFetch(){
        return myImplicitFetch;
    }
}
