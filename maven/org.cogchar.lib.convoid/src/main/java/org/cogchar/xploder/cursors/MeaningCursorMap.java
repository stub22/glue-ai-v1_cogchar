package org.cogchar.xploder.cursors;

import java.util.HashMap;
import java.util.Map;
import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext;
import org.cogchar.convoid.player.IBehaviorPlayable;
import org.cogchar.convoid.player.StepPlayer;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Matt Stevenson
 */
public class MeaningCursorMap {
    private Map<String, IConvoidCursor>  myCursorTable;

    public MeaningCursorMap(){
        myCursorTable = new HashMap<String, IConvoidCursor>();
    }

    public Map<String, IConvoidCursor> getCursorTable(){
        return myCursorTable;
    }

    public void put(String meaning, IConvoidCursor cursor){
        if(meaning != null && cursor != null){
            myCursorTable.put(meaning, cursor);
        }
    }

    public IConvoidCursor getCursor(String meaning){
        if(meaning == null){
            return null;
        }
        return myCursorTable.get(meaning);
    }

    public BehaviorContext getResponseBehavior(String meaning){
        IConvoidCursor cursor = getCursor(meaning);
        if(cursor == null){
            return BehaviorContext.makeEmpty()
                    .withIntendedType("RESPONSE").andPrompt(meaning);
        }
        long time = TimeUtils.currentTimeMillis();
        if(!cursor.isPlayableAtTime(time)){
            cursor.resetAtTime(time);
        }
        Step step = cursor.getBestStepAtTime(time);
        if(step == null){
            cursor.resetAtTime(time);
            step = cursor.getBestStepAtTime(time);
            if(step == null){
                return BehaviorContext.makeEmpty()
                        .withIntendedType("RESPONSE").andPrompt(meaning);
            }
        }
        IBehaviorPlayable player = new StepPlayer(step);
        return new BehaviorContext().with(player).andIntendedType("RESPONSE")
                .andActualType("RESPONSE").andPrompt(meaning);
    }
}
