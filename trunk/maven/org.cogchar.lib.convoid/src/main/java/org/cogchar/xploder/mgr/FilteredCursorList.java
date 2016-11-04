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
import org.cogchar.xploder.mgr.CursorRequest.BackupOption;
import org.cogchar.xploder.mgr.CursorRequest.ResetMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cogchar.platform.util.CollectionUtils;

/**
 *
 * @author Matt Stevenson
 */
public class FilteredCursorList {
    private List<IConvoidCursor> myCursors;
    private List<IConvoidCursor> myPlayables;
    private List<IConvoidCursor> myResetList;

    public FilteredCursorList(List<IConvoidCursor> cursors, CursorRequest request){
        myCursors = new ArrayList<IConvoidCursor>();
        myPlayables = new ArrayList<IConvoidCursor>();
        myResetList = new ArrayList<IConvoidCursor>();
        filterCursors(cursors, request);
    }

    public List<IConvoidCursor> getCursors() {
        return myCursors;
    }

    public List<IConvoidCursor> getPlayables() {
        return myPlayables;
    }

    public List<IConvoidCursor> getResetList() {
        return myResetList;
    }

    private void filterCursors(List<IConvoidCursor> cursors, CursorRequest request){
        if(cursors == null || request == null){
            return;
        }
        boolean filterByMeanings = (request.getMeanings() != null && !request.getMeanings().isEmpty());
        boolean requiredMeanings =
                (request.getRequiredMeanings() != null && !request.getRequiredMeanings().isEmpty());
        
        myCursors.addAll(cursors);
        if(filterByMeanings){
            myCursors = filterByMeanings(myCursors, request.getMeanings().keySet());
        }
        if(requiredMeanings){
            for(String m : request.getRequiredMeanings()){
                myCursors = filterByMeanings(myCursors, CollectionUtils.list(m));
            }
        }
        boolean reset = request.getResetMode() != ResetMode.NONE;
        reset = reset || request.getBackupOptions().contains(BackupOption.RESET);
        Long time = request.getRequestTime();
        for(IConvoidCursor cc : myCursors){
            if(cc.isPlayableAtTime(time)){
                myPlayables.add(cc);
            }
            Long elapsed = 0L;
            if(cc.getLastAdvanceTime() != null){
                elapsed = time - cc.getLastAdvanceTime();
            }
            if(reset && elapsed > request.getResetTime()){
                myResetList.add(cc);
            }
        }
    }

    private List<IConvoidCursor> filterByMeanings(List<IConvoidCursor> cursors, Collection<String> meanings){
        List<IConvoidCursor> ret = new ArrayList<IConvoidCursor>();
        for(IConvoidCursor cc : cursors){
            if(ret.contains(cc)){
                continue;
            }
            for(String m : meanings){
                if(!ret.contains(cc) && cc.getMeanings().contains(m)){
                    ret.add(cc);
                }
            }
        }
        return ret;
    }
}
