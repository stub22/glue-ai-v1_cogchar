/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.convoid.cursors;

import java.util.List;
import org.cogchar.convoid.output.config.Step;

/**
 *
 * @author matt
 */
public interface IConvoidCursor {
    public void     resetAtTime(long time);
    public Integer  getCurrentIndex();
    public Long     getLastAdvanceTime();
    public Step     getBestStepAtTime(long time);
    public long     getTimeoutLength();
    public boolean  isTimedOutAtTime(long time);
    public boolean  isFinishedAtTime(long time);
    public boolean  isPlayableAtTime(long time);
    public boolean  isActive();
    public boolean  isCurrentActFinishedAtTime(long time);

    public List<String> getMeanings();
    public List<String> getActiveMeanings();
    public void setMeanings(List<String> meanings);
    public boolean isRandom();

    public String  getName();
    public String getGroupType();
}
