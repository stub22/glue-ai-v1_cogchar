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

package org.cogchar.api.platform.cues;

import org.cogchar.platform.stub.JobStub.Status;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Matt Stevenson
 */
public class TimerCue extends NamedCue {
    private Long    myEndTime;
    private Status  myStatus;
    private Integer myDuration;

    public TimerCue(String name, Integer duration){
        super(name);
        myEndTime = TimeUtils.currentTimeMillis() + duration*1000;
        myStatus = Status.RUNNING;
        myDuration = duration;
    }

    public void update(){
        if(TimeUtils.currentTimeMillis() > myEndTime){
            myStatus = Status.COMPLETED;
        }
        markUpdatedNow();
    }

    public Status getStatus(){
        return myStatus;
    }

    public Integer getDuration(){
        return myDuration;
    }

    public Integer getRemaining(){
        Long elapsed = 0L;
        if(myStatus == Status.RUNNING){
            elapsed = myEndTime - TimeUtils.currentTimeMillis();
        }
        Integer remaining = elapsed.intValue()/1000;
        return remaining <= 0 ? 0 : remaining;
    }

    public void extend(Integer duration){
        if(myStatus == Status.RUNNING){
            myEndTime += duration*1000;
            myDuration += duration;
        }else if(myStatus == Status.COMPLETED){
            myEndTime = TimeUtils.currentTimeMillis() + duration*1000;
            myStatus = Status.RUNNING;
            myDuration = duration;
        }
        markUpdatedNow();
    }

    @Override
	public String getContentSummaryString() {
        Long elapsed = 0L;
        if(myStatus == Status.RUNNING){
            elapsed = myEndTime - TimeUtils.currentTimeMillis();
        }
        Integer remaining = elapsed.intValue()/1000;
        remaining = remaining <= 0 ? 0 : remaining;
		return "(" + remaining + ")" + getName();
	}
}
