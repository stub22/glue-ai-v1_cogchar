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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Matt Stevenson
 */
public class CursorRequest {
    public enum ScoreMode{
        HIGH,
        LOW,
        IGNORE
    }
    public enum ResetMode{
        NONE,
        RESET,
        TIMED,
    }
    public enum BackupOption{
        REMOTE,
        RESET,
        RANDOM
    }
    private List<String>        myTypes;
    private Map<String, Double> myMeanings;
    private ScoreMode           myScoreMode;
    private ResetMode           myResetMode;
    private List<BackupOption>  myBackupOptions;
    private List<String>        myRequiredMeanings;
    private Double              myScoreThreshold;
    private Long                myResetTime;
    private Long                myRequestTime;
    private Integer             myChoices;

    public CursorRequest(Long time, Long reset, Double scoreThreshold, Integer choices){
        myRequestTime = time;
        myResetTime = reset;
        myScoreThreshold = scoreThreshold;
        myScoreMode = ScoreMode.HIGH;
        myResetMode = ResetMode.NONE;
        myBackupOptions = new ArrayList<BackupOption>();
        myChoices = choices;
        myTypes = new ArrayList<String>();
        myMeanings = new HashMap<String, Double>();
        myRequiredMeanings = new ArrayList<String>();
    }

    public CursorRequest(CursorRequest r){
        myTypes = new ArrayList(r.myTypes);
        myMeanings = new HashMap(r.myMeanings);
        myScoreMode = r.myScoreMode;
        myResetMode = r.myResetMode;
        myBackupOptions = new ArrayList(r.myBackupOptions);
        myRequiredMeanings = new ArrayList(r.myRequiredMeanings);
        myScoreThreshold = r.myScoreThreshold;
        myResetTime = r.myResetTime;
        myRequestTime = r.myRequestTime;
        myChoices = r.myChoices;
    }

    public List<BackupOption> getBackupOptions() {
        return myBackupOptions;
    }

    public void setBackupOptions(List<BackupOption> myBackupOptions) {
        if(myBackupOptions == null){
            return;
        }
        this.myBackupOptions = myBackupOptions;
    }

    public Map<String, Double> getMeanings() {
        return myMeanings;
    }

    public void setMeanings(Map<String, Double> myMeanings) {
        this.myMeanings = myMeanings;
    }

    public List<String> getRequiredMeanings() {
        return myRequiredMeanings;
    }

    public void setRequiredMeanings(List<String> myRequiredMeanings) {
        this.myRequiredMeanings = myRequiredMeanings;
    }

    public ResetMode getResetMode() {
        return myResetMode;
    }

    public void setResetMode(ResetMode myResetMode) {
        this.myResetMode = myResetMode;
    }

    public Long getResetTime() {
        return myResetTime;
    }

    public void setResetTime(Long myResetTime) {
        this.myResetTime = myResetTime;
    }

    public ScoreMode getScoreMode() {
        return myScoreMode;
    }

    public void setScoreMode(ScoreMode myScoreMode) {
        this.myScoreMode = myScoreMode;
    }

    public Double getScoreThreshold() {
        return myScoreThreshold;
    }

    public void setScoreThreshold(Double myScoreThreshhold) {
        this.myScoreThreshold = myScoreThreshhold;
    }

    public List<String> getTypes() {
        return myTypes;
    }

    public void setTypes(List<String> myTypes) {
        this.myTypes = myTypes;
    }

    public Long getRequestTime() {
        return myRequestTime;
    }

    public void setRequestTime(Long myRequestTime) {
        this.myRequestTime = myRequestTime;
    }

    public void setChoices(Integer x){
        myChoices = x;
    }

    public Integer getChoices(){
        return myChoices;
    }

    @Override public String toString(){
        String ret = "Types" + arStr(myTypes) +
                ", Req" + arStr(myRequiredMeanings) +
                ", Meanings" + arStr(myMeanings.keySet());
        return ret;
    }

    public String arStr(Collection l){
        return Arrays.toString(l.toArray());
    }
}
