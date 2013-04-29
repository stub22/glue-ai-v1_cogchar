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
import org.cogchar.xploder.cursors.MeaningScoreKeeper;
import org.cogchar.api.convoid.act.Category;
import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext;
import org.cogchar.convoid.player.BehaviorContext.Detail;
import org.cogchar.convoid.player.SpeechPlayer;
import org.cogchar.convoid.player.IBehaviorPlayable;
import org.cogchar.xploder.mgr.CursorRequest.BackupOption;
import org.cogchar.xploder.mgr.CursorRequest.ResetMode;
import org.cogchar.xploder.mgr.CursorRequest.ScoreMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.cogchar.convoid.broker.RemoteResponseFacade;
import org.cogchar.convoid.job.ChatJob;
import org.cogchar.convoid.job.ExpositionJob;
import org.cogchar.convoid.job.SpeechJob;
import org.cogchar.convoid.job.TalkJob;
import org.cogchar.platform.util.CollectionUtils;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Matt Stevenson
 */
public class CursorManager {
    private static Logger theLogger = Logger.getLogger(CursorManager.class.getName());
	private static Random theRandomizer = new Random();
	private static long theTopicKeepAliveTime = 10000L;
	private Map<String, BehaviorTypeManager>	myTypeManagerTable;
	private List<BehaviorTypeManager>			myManagers;
	private List<IConvoidCursor>				myCursors;
	private SpeechJob							myLastSpeechJob;
	private SpeechJob							myCurrentSpeechJob;

	public CursorManager(Category rootCat) {
		myTypeManagerTable = new HashMap<String, BehaviorTypeManager>();
		myManagers = new ArrayList<BehaviorTypeManager>();
		myCursors = new ArrayList<IConvoidCursor>();
        buildAndRegisterManager(rootCat, "Expositions", new SpeechJobFactory("EXPOSITION", ExpositionJob.class, 0.9, 3000L), true);
        buildAndRegisterManager(rootCat, "Chat", new SpeechJobFactory("CHAT", ChatJob.class, 0.9, 0L), true);
		CursorGroup expo = myTypeManagerTable.get("EXPOSITION").getCursorGroup();
		CursorGroup chat = myTypeManagerTable.get("CHAT").getCursorGroup();
		expo.setBackupGroup(chat);
		chat.setBackupGroup(expo);
        loadExtraBehaviors(rootCat);
	}

    private void loadExtraBehaviors(Category rootCat){
        Category extra = null;
        for(Category c : rootCat.getSubCategories()){
            if(c.getName().equals("EXTRA_BEHAVIORS")){
                extra = c;
                break;
            }
        }
        if(extra == null){
            return;
        }
        for(Category c : extra.getSubCategories()){
            String n = c.getName();
            buildAndRegisterManager(extra, n, new SpeechJobFactory(n, TalkJob.class, 0.9, 15000L), false);
        }
    }

	public CursorGroup getCursorGroup(String type){
		if(!myTypeManagerTable.containsKey(type)){
			return null;
		}
		return myTypeManagerTable.get(type).getCursorGroup();
	}

	public BehaviorTypeManager getTypeManager(String type){
		return myTypeManagerTable.get(type);
	}

	public void setLastPlayed(SpeechJob job){
        if(job == null){
            theLogger.info("Cannot set last played job to null!");
            return;
        }
        String type = job.getCategoryCursor().getGroupType();
		if(!myTypeManagerTable.containsKey(type)){
            theLogger.info("Cannot set last played job with type: " + type);
			return;
		}
        theLogger.info("Setting Last Played:\nType:" + type + " Cat: " + job.getCategoryName());
		if(myCurrentSpeechJob != null){
			myLastSpeechJob = myCurrentSpeechJob;
		}
		myCurrentSpeechJob = job;
	}

    public SpeechJob getLastPlayed(){
		long time = TimeUtils.currentTimeMillis();
		SpeechJob job = myCurrentSpeechJob;
		if(myLastSpeechJob == null){
            return job;
        }
        long dead = time - myLastSpeechJob.getUpdateStampMsec();
        if(dead < theTopicKeepAliveTime){
            job = myLastSpeechJob;
        }
        return job;
    }

    public BehaviorContext getMoreToSay(){
		if(myCurrentSpeechJob == null){
			return BehaviorContext.makeEmpty();
		}
		long time = TimeUtils.currentTimeMillis();
		SpeechJob job = myCurrentSpeechJob;
		if(myLastSpeechJob != null){
            long dead = time - myLastSpeechJob.getUpdateStampMsec();
            if(dead < theTopicKeepAliveTime){
                job = myLastSpeechJob;
            }
        }
        theLogger.info("Last Played: " + job.getCategoryName());
        IConvoidCursor cc = job.getCategoryCursor();
        if(cc.isFinishedAtTime(time)){
            return BehaviorContext.makeEmpty().with(Detail.FROM_EXPO);
        }
        Step step = cc.getBestStepAtTime(time);
        if(step != null){
            IBehaviorPlayable player = new SpeechPlayer(step, job);
            return new BehaviorContext().with(player).andActualType(cc.getGroupType());
        }
        addMeaningsForJob(job, 0.75, time);
        return getOnTopicFromCursors(null, myCursors);
    }

	private BehaviorContext getOnTopicFromCursors(Map<String,Double> meanings, List<IConvoidCursor> cursors){
		long time = TimeUtils.currentTimeMillis();
		addMeaningsAtTime(meanings, 1.0, time);
		Map<IConvoidCursor, Double> scores = getScores(getCursorGroupTypes(), time);
        if(scores.isEmpty()){
            addMeaningAtTime("RANDOM", time);
            scores = getScores(getCursorGroupTypes(), time);
        }
		IConvoidCursor best = MeaningScoreKeeper.getRandomBestMatchFromMeanings(
                scores, cursors, meanings, time, 0.0);
		if(best == null){
			return BehaviorContext.makeEmpty();
		}
		SpeechJob job = null;
		for(BehaviorTypeManager btm : myManagers){
			job = btm.getCursorGroup().getJobForCursor(best);
			if(job == null){
				continue;
			}
			Step step = job.getCategoryCursor().getBestStep();
			if(step == null){
				return BehaviorContext.makeEmpty().withIntendedType(btm.getBehaviorType());
			}
			IBehaviorPlayable player = new SpeechPlayer(step, job);
			return new BehaviorContext().with(player).andActualType(btm.getBehaviorType())
                    .andIntendedType(btm.getBehaviorType());
		}
		return BehaviorContext.makeEmpty();
	}

    public BehaviorContext getBehaviorContext(CursorRequest request){
        Long time = request.getRequestTime();
        if(request.getScoreMode() != ScoreMode.IGNORE){
            addMeaningsAtTime(request.getMeanings(), 1.0, time);
            if(request.getRequiredMeanings() != null){
                for(String m : request.getRequiredMeanings()){
                    addMeaningAtTime(m, time);
                }
            }
        }
        FilteredCursorList cursorList = collectCursorsForRequest(request);
        Map<IConvoidCursor, Double> scores = getScores(request.getTypes(), time);
        scores = filterScores(scores, cursorList.getCursors(), request.getScoreThreshold());
        IConvoidCursor best = getRequestedCursor(request, cursorList.getPlayables(), scores);

        BehaviorContext context = getCursorBehavior(best, time);
        if(context.isEmptyBehavior() && request.getResetMode() != ResetMode.NONE){
            context = getBehaviorToReplay(request, cursorList);
        }
        if(context.isEmptyBehavior()){
            context = getBackupBehavior(request, cursorList);
        }
        return context;
    }

    private FilteredCursorList collectCursorsForRequest(CursorRequest request){
        List<IConvoidCursor> cursors = getCursorsForTypes(request.getTypes());
        return new FilteredCursorList(cursors, request);
    }

    private List<IConvoidCursor> getCursorsForTypes(List<String> types){
        List<IConvoidCursor> cursors = new ArrayList<IConvoidCursor>();
        if(types == null){
            return cursors;
        }
        for(String type : types){
            CursorGroup group = getCursorGroup(type);
            if(group != null){
                cursors.addAll(group.getCursors());
            }
        }
        return cursors;
    }

    private Map<IConvoidCursor, Double> filterScores(Map<IConvoidCursor, Double> scores,
                List<IConvoidCursor> cursors, Double scoreThreshold){
        if(scoreThreshold == null){
            scoreThreshold = 0.0;
        }
        Map<IConvoidCursor, Double> ret = new HashMap<IConvoidCursor, Double>();
        for(IConvoidCursor cc : cursors){
            if(scores.containsKey(cc)){
                double score = scores.get(cc);
                if(score > scoreThreshold){
                    ret.put(cc, scores.get(cc));
                }
            }
        }
        return ret;
    }

    private Map<IConvoidCursor, Double> getScores(Collection<String> types, long time){
		Map<IConvoidCursor, Double> scores = new HashMap<IConvoidCursor, Double>();
        for(String t : types){
            BehaviorTypeManager btm = getTypeManager(t);
            scores.putAll(btm.getCursorGroup().getScoreKeeper().getScoresAtTime(time));
        }
        return scores;
    }

    private IConvoidCursor getRequestedCursor(CursorRequest request, List<IConvoidCursor> cursors,
            Map<IConvoidCursor, Double> scores){
        List<Double> pscores = new ArrayList();
        Map<Double, List<IConvoidCursor>> passing = new HashMap();
		for(IConvoidCursor a : cursors){
            if(!scores.containsKey(a)){
                continue;
            }
			double score = scores.get(a);
            if(!passing.containsKey(score)){
                passing.put(score, new ArrayList());
            }
            passing.get(score).add(a);
            pscores.add(score);
		}
        ScoreMode mode = request.getScoreMode();
        if(mode == ScoreMode.HIGH || mode == ScoreMode.LOW){
            Collections.sort(pscores);
            if(mode == ScoreMode.HIGH){
                Collections.reverse(pscores);
            }
        }
        if(!pscores.isEmpty()){
            int max = 0;
            switch(mode){
                case IGNORE : max = pscores.size();
                break;
                case LOW :
                case HIGH :
                    max = request.getChoices();
                    break;
            }
            max = Math.min(pscores.size(), max);
            Integer rand = theRandomizer.nextInt(max);
            List<IConvoidCursor> selected = passing.get(pscores.get(rand));
            return selected.get(theRandomizer.nextInt(selected.size()));
        }
        return null;
    }
    
    private BehaviorContext getBehaviorToReplay(CursorRequest request, FilteredCursorList cursors){
        Long time = request.getRequestTime();
        ResetMode rmode = request.getResetMode();
        IConvoidCursor best = getBestCursorToReset(request, cursors);
        if(best == null){
            return BehaviorContext.makeEmpty();
        }
        Long elapsed = time - best.getLastAdvanceTime();
        if(rmode == ResetMode.TIMED && !pickBasedOnTime(elapsed)){
            return BehaviorContext.makeEmpty();
        }
        best.resetAtTime(time);
        return getCursorBehavior(best, time).with(Detail.RESET);
    }

    private IConvoidCursor getBestCursorToReset(CursorRequest request, FilteredCursorList cursors){
		IConvoidCursor best = null;
        double bestScore = 0.0;
		for(IConvoidCursor a : cursors.getResetList()){
            if(a.getLastAdvanceTime() == null){
                continue;
            }
            long elapsed = request.getRequestTime() - a.getLastAdvanceTime();
            Double newScore = 0.0;
            for(Entry<String,Double> e : request.getMeanings().entrySet()){
                if(a.getMeanings().contains(e.getKey())){
                    newScore += e.getValue();
                }
            }
            newScore *= elapsed;
            if(newScore > bestScore){
                bestScore = newScore;
                best = a;
            }
		}
        return best;
    }

    private boolean pickBasedOnTime(long timeSpan){
        //A logistic curve with 30 minutes at P(reset)=0.5
        double secs = 1800;

        double x = (double)(timeSpan/10.0);
        double a = 3.6/secs;
        double n = Math.pow(Math.E, (-a*(x-secs)));
        double score = 1/(1.0 + n);
        return theRandomizer.nextFloat() <= score;
    }

    private BehaviorContext getBackupBehavior(CursorRequest request, FilteredCursorList cursors){
        List<BackupOption> options = new ArrayList<BackupOption>(request.getBackupOptions());
        int len = options.size();
        if(options == null || options.isEmpty()){
            return BehaviorContext.makeEmpty();
        }
		BehaviorContext bc = BehaviorContext.makeEmpty();
        int count = 0;
		int offset = theRandomizer.nextInt(options.size());
		do{
			switch(options.get((count+offset)%len)){
				case REMOTE : bc = RemoteResponseFacade.getResponseBehavior();break;
				case RESET : bc = getBehaviorToReplay(request, cursors);break;
				case RANDOM :
                    CursorRequest br = getBackupRequest(request);
                    br.getRequiredMeanings().add("RANDOM");
                    bc = getBehaviorContext(br).with(Detail.RANDOM);break;
			}
			count++;
            if(count == len){
                return bc;
            }
		}while(bc.isEmptyBehavior());
        return bc;
    }

    public void killSpeechJob(SpeechJob job){
		getCursorGroup(job.getCategoryCursor().getGroupType()).killSpeechJob(job);
    }

	private void buildAndRegisterManager(Category cat, String name, SpeechJobFactory factory, Boolean fetch){
        if(myTypeManagerTable.containsKey(factory.getBehaviorType())){
            theLogger.severe("Duplicate Group Types: " + name);
            return;
        }
		BehaviorTypeManager manager = new BehaviorTypeManager(cat, name, factory, fetch);
		if(!myTypeManagerTable.containsKey(factory.getBehaviorType())){
			myTypeManagerTable.put(factory.getBehaviorType(), manager);
		}
		if(!myManagers.contains(manager)){
			myManagers.add(manager);
		}
		myCursors.addAll(manager.getCursorGroup().getScoreKeeper().getCursors());
	}

	public void addMeaningAtTime(String meaning, long time){
		if(meaning == null){
			return;
		}
		for(BehaviorTypeManager btm : myManagers){
			btm.getCursorGroup().getScoreKeeper().addMeaningAtTime(meaning, time);
		}
	}

	public void addMeaningsAtTime(Map<String, Double> meanings, double perc, long time){
		if(meanings == null || meanings.isEmpty()){
			return;
		}
		for(BehaviorTypeManager btm : myManagers){
			btm.getCursorGroup().getScoreKeeper().addMeaningsAtTime(meanings, perc, time);
		}
	}

    public void addMeaningsForJob(SpeechJob job, double perc, long time){
		if(job == null){
			return;
		}
        Map<String,Double> meanings = new HashMap();
        for(String m :job.getCategoryCursor().getActiveMeanings()){
            meanings.put(m, 1.0);
        }
        addMeaningsAtTime(meanings, perc, time);
    }

	public boolean hasMeaning(String meaning){
		for(BehaviorTypeManager btm : myManagers){
			if(btm.getCursorGroup().getScoreKeeper().hasMeaning(meaning)){
				return true;
			}
		}
		return false;
	}

    public boolean containsMeanings(Set<String> meanings){
        for(String m : meanings){
            if(hasMeaning(m)){
                return true;
            }
        }
        return false;
    }

    private Set<String> getCursorGroupTypes(){
        return myTypeManagerTable.keySet();
    }

    private BehaviorContext getCursorBehavior(IConvoidCursor cursor, long time){
        if(cursor == null){
            return BehaviorContext.makeEmpty();
        }
        CursorGroup cg = getCursorGroup(cursor.getGroupType());
        if(cg == null){
            return BehaviorContext.makeEmpty();
        }
        return cg.getCursorBehavior(cursor, time);
    }

    private CursorRequest getBackupRequest(CursorRequest orig){
        CursorRequest request = new CursorRequest(orig);
        request.setResetMode(ResetMode.NONE);
        request.getBackupOptions().retainAll(CollectionUtils.list(BackupOption.REMOTE));
        request.setMeanings(new HashMap<String, Double>());
        return request;
    }
}
