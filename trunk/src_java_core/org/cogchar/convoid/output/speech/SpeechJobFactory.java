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

package org.cogchar.convoid.output.speech;

import org.cogchar.convoid.cursors.CategoryCursor;
import org.cogchar.convoid.cursors.CursorFactory;
import org.cogchar.convoid.cursors.IConvoidCursor;
import org.cogchar.convoid.output.config.Category;
import org.cogchar.convoid.output.config.Step;
import org.cogchar.convoid.output.exec.context.SpeechPlayer;
import org.cogchar.convoid.output.exec.context.IBehaviorPlayable;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Logger;
import org.cogchar.convoid.output.exec.SpeechJob;

/**
 *
 * @author Matt Stevenson
 */
public class SpeechJobFactory {
    private static Logger theLogger = Logger.getLogger(SpeechJobFactory.class.getName());
    private String                          myBehaviorType;
    private Class<? extends SpeechJob>      myJobClass;
    private Double                          myThreshold;
    private Long                            myResetTime;

    public SpeechJobFactory(String type, Class<? extends SpeechJob> clss, Double thresh, Long reset){
        myBehaviorType = type;
        myJobClass = clss;
        myThreshold = thresh;
        myResetTime = reset;
    }

    public SpeechJob buildJob(CategoryCursor cc) {
        try{
            Constructor cons = getJobClass().getConstructor(CategoryCursor.class);
            return (SpeechJob)cons.newInstance(cc);
        }catch(Throwable t){
            theLogger.warning("Unable to build a job for the given category: " + cc.getName());
        }
        return null;
    }

    public IBehaviorPlayable buildPlayer(Step step, IConvoidCursor cc, SpeechJob job){
        if(job.getClass() != getJobClass()){
            throw new IllegalArgumentException(this.getClass().getSimpleName() +
                    " can only take " + getJobClass().getSimpleName() + ", it was given: "
                    + job.getClass());
        }
        job.setCurrentCursor(cc);
        return new SpeechPlayer(step, job);
    }

    public String getBehaviorType(){
        return myBehaviorType;
    }

    public CursorGroup buildCursorGroup(Category rootCat){
        return createCursorGroup(rootCat, myThreshold, myResetTime);
    }

    public Class <? extends SpeechJob> getJobClass(){
        return myJobClass;
    }

    protected CursorGroup createCursorGroup(Category root, double thresh, long reset){
        if(root == null){
            throw new IllegalArgumentException("Root category cannot be null");
        }
        List<IConvoidCursor> cursors = CursorFactory.buildAllCursorsForCategory(root, myBehaviorType);
        CursorGroup g = new CursorGroup(cursors, thresh, reset, this);
        g.initializeJobs();
        return g;
    }
}
