package org.cogchar.convoid.output.speech;

import org.cogchar.convoid.cursors.ActCursor;
import org.cogchar.convoid.cursors.ActSequenceCursor;
import org.cogchar.convoid.cursors.CategoryCursor;
import org.cogchar.convoid.cursors.IConvoidCursor;
import org.cogchar.convoid.cursors.MeaningScoreKeeper;
import org.cogchar.convoid.output.config.Category;
import org.cogchar.convoid.output.config.Step;
import org.cogchar.convoid.output.exec.context.BehaviorContext;
import org.cogchar.convoid.output.exec.context.BehaviorContext.Detail;
import org.cogchar.convoid.output.exec.context.IBehaviorPlayable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import org.cogchar.convoid.broker.RemoteResponseFacade;
import org.cogchar.convoid.output.exec.SpeechJob;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author matt
 */
public class CursorGroup {
    private static Logger theLogger = Logger.getLogger(CursorGroup.class.getName());
    private static Random theRandomizer = new Random();
    private Map<IConvoidCursor, SpeechJob>          myCursorJobTable;
    private Map<Category, SpeechJob>                myCategoryJobTable;
    private Map<IConvoidCursor, Category>           myCursorCategoryMap;
    private Map<SpeechJob, List<IConvoidCursor>>    myJobCursorTable;

    private CursorGroup             myBackupGroup;

    private List<IConvoidCursor>    myCursors;
    private String                  myBehaviorType;
    private SpeechJobFactory        myFactory;
    private MeaningScoreKeeper      myScoreKeeper;
    
    public CursorGroup(List<IConvoidCursor> cursors, Double thresh, Long reset, SpeechJobFactory factory){
        myCursors = cursors;
        myScoreKeeper = new MeaningScoreKeeper(cursors, thresh, reset);
        myFactory = factory;
        myBehaviorType = factory.getBehaviorType();
        initializeJobs();
    }

    public void initializeJobs(){
        myCategoryJobTable = new HashMap<Category, SpeechJob>();
        myCursorCategoryMap = new HashMap<IConvoidCursor, Category>();
        myCursorJobTable = new HashMap<IConvoidCursor, SpeechJob>();
        myJobCursorTable = new HashMap<SpeechJob, List<IConvoidCursor>>();
        for(IConvoidCursor cc : myScoreKeeper.getCursors()){
            if(CategoryCursor.class.isAssignableFrom(cc.getClass())){
                Category c = ((CategoryCursor)cc).getCategory();
                SpeechJob job = myFactory.buildJob((CategoryCursor)cc);
                myCategoryJobTable.put(c, job);
                myCursorCategoryMap.put(cc, c);
            }else if(ActSequenceCursor.class.isAssignableFrom(cc.getClass())){
                Category c = ((ActSequenceCursor)cc).getCategoryCursor().getCategory();
                myCursorCategoryMap.put(cc, c);
            }
        }
        for(IConvoidCursor cc : myScoreKeeper.getCursors()){
            Category c = myCursorCategoryMap.get(cc);
            SpeechJob job = myCategoryJobTable.get(c);
            myCursorJobTable.put(cc, job);
            if(!myJobCursorTable.containsKey(job)){
                myJobCursorTable.put(job, new ArrayList<IConvoidCursor>());
            }
            myJobCursorTable.get(job).add(cc);
        }
    }

    public SpeechJob getJobForCursor(IConvoidCursor cursor){
        return myCursorJobTable.get(cursor);
    }

    public BehaviorContext getCursorBehavior(IConvoidCursor cursor, long time){
        if(cursor == null){
            return BehaviorContext.makeEmpty();
        }
        Step step = cursor.getBestStepAtTime(time);
        if(step == null){
            return BehaviorContext.makeEmpty();
        }
        SpeechJob job = getJobForCursor(cursor);
        IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, job);
        return new BehaviorContext().with(player).andActualType(myBehaviorType);
    }

    public BehaviorContext getBestJobAtTime(Map<String,Double> meanings, long time){
        theLogger.finest("Getting BestJob from " + myFactory.getBehaviorType() + " Group");
        if(meanings != null && !meanings.isEmpty()){
            myScoreKeeper.addMeaningsAtTime(meanings, 1.0, time);
        }
        Map scores = myScoreKeeper.getScoresAtTime(time);
        BehaviorContext best = getBestMatchedJob(scores, meanings, time);
        if(!best.isEmptyBehavior()){
            theLogger.finest("Found good match from " + myFactory.getBehaviorType() + " Group");
            return best;
        }
        best = getBackupBehavior(scores, meanings, time);
        if(!best.isEmptyBehavior()){
            theLogger.finest("Found good backup match from " + myFactory.getBehaviorType() + " Group");
            return best;
        }
        return getResetOrLastResort(scores, meanings, time);
    }

    public BehaviorContext getBestMatchedJob(Map scores, Map<String,Double> meanings, long time){
        theLogger.info("Getting BestMatch from " + myFactory.getBehaviorType() + " Group");
        IConvoidCursor cursor = MeaningScoreKeeper.getRandomBestMatchFromMeanings(
                scores, myCursors, meanings, time, 0.8);
        if(cursor != null){
            Step step = cursor.getBestStepAtTime(time);
            if(step != null){
                theLogger.info("Found BestMatch from " + myFactory.getBehaviorType() + " Group");
                IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, getJobForCursor(cursor));
                return new BehaviorContext().with(player).andActualType(myBehaviorType);
            }
        }
        return BehaviorContext.makeEmpty();
    }

    public BehaviorContext getOrResetBestWithMeanings(Map<IConvoidCursor, Double> scores, Map meanings, long time){
        theLogger.info("Getting BestMatch with meaning from " + myFactory.getBehaviorType() + " Group");
        String c = "";
        for(IConvoidCursor cc : scores.keySet()){
            c += cc.getName() + ", ";
        }
        theLogger.info("Cursors: " + c);
        IConvoidCursor cursor = MeaningScoreKeeper.getBestMatchFromMeanings(scores, new ArrayList(scores.keySet()), meanings, time, 0.2);
        if(cursor != null){
            Step step = cursor.getBestStepAtTime(time);
            if(step == null){
                cursor.resetAtTime(time);
                step = cursor.getBestStepAtTime(time);
            }
            if(step != null){
                theLogger.info("Found BestMatch from " + myFactory.getBehaviorType() + " Group");
                IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, getJobForCursor(cursor));
                return new BehaviorContext().with(player).andActualType(myBehaviorType);
            }
        }
        return getBestToReset(scores, meanings, time);
    }

    public BehaviorContext getBackupBehavior(Map scores, Map<String,Double> meanings, long time){
        if(myBackupGroup == null){
            theLogger.fine("Getting Backup from " + myFactory.getBehaviorType() + " Group");
            return BehaviorContext.makeEmpty().with(Detail.BACKUP);
        }
        return myBackupGroup.getBestMatchedJob(scores, meanings, time).with(Detail.BACKUP);
    }

    public BehaviorContext getBestLastResort(Map scores, Map<String,Double> meanings, long time){
        theLogger.fine("Getting LastResort from " + myFactory.getBehaviorType() + " Group");
		BehaviorContext bc;
		Integer rand = theRandomizer.nextInt(5);
		do{
			switch(rand){
				case 0 : bc = getRemoteResponse();break;
				case 1 : bc = getBestRandomAtTime(time).with(Detail.RANDOM);break;
				case 2 : bc = getBestToReset(scores, meanings, time);break;
				case 3 : bc = getBestMatchedJob(scores, null, time).with(Detail.RANDOM);break;
				default: return BehaviorContext.makeEmpty().with(Detail.BACKUP);
			}
			rand++;
		}while(bc.isEmptyBehavior());
		return bc;
    }

	public BehaviorContext getRemoteResponse(){
        theLogger.fine("Getting RemoteResponse from " + myFactory.getBehaviorType() + " Group");
		return RemoteResponseFacade.getResponseBehavior();
	}

    public BehaviorContext getResetOrLastResort(Map scores, Map<String,Double> meanings, long time){
        IConvoidCursor cursor = MeaningScoreKeeper.getBestToReset(scores, meanings, time, 0L, 0.9);
        boolean reset = false;
        if(cursor != null){
            long elapsed = time;
            if(cursor.getLastAdvanceTime() != null){
                elapsed = time - cursor.getLastAdvanceTime();
            }
            reset = pickBasedOnTime(elapsed);
        }

        if(reset){
            Step step = cursor.getBestStepAtTime(time);
			if(step == null){
				cursor.resetAtTime(time);
				step = cursor.getBestStepAtTime(time);
			}
            if(step != null){
                theLogger.fine("Found BestToReset from " + myFactory.getBehaviorType() + " Group");
                IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, getJobForCursor(cursor));
                return new BehaviorContext().with(player).andActualType(myBehaviorType).and(Detail.RESET);
            }
        }
        return getBestLastResort(scores, meanings, time);
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

    public BehaviorContext getBestToReset(Map scores, Map<String,Double> meanings, long time){
        theLogger.fine("Getting BestToReset from " + myFactory.getBehaviorType() + " Group");
        IConvoidCursor cursor = MeaningScoreKeeper.getBestToReset(scores, meanings, time, 0L, 0.9);
        if(cursor != null){
            Step step = cursor.getBestStepAtTime(time);
			if(step == null){
				cursor.resetAtTime(time);
				step = cursor.getBestStepAtTime(time);
			}
            if(step != null){
                theLogger.fine("Found BestToReset from " + myFactory.getBehaviorType() + " Group");
                IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, getJobForCursor(cursor));
                return new BehaviorContext().with(player).andActualType(myBehaviorType).and(Detail.RESET);
            }else{
                theLogger.fine("Could not find cursor to reset for " + myFactory.getBehaviorType() + " Group");
            }
        }
        return BehaviorContext.makeEmpty().and(Detail.RESET);
    }

	public BehaviorContext getBestRandomAtTime(long time){
        theLogger.fine("Getting BestRandom from " + myFactory.getBehaviorType() + " Group");
		myScoreKeeper.addMeaningAtTime("RANDOM", time);
        Map scores = myScoreKeeper.getScoresAtTime(time);
        Map<String, Double> meanings = new HashMap();
        meanings.put("RANDOM", 1.0);
        BehaviorContext best = getBestMatchedJob(scores, meanings, time);
        if(!best.isEmptyBehavior()){
            theLogger.fine("Found BestRandom from " + myFactory.getBehaviorType() + " Group");
            return best;
        }
        return getRandomJobAtTime(time);
	}

    public BehaviorContext getRandomJobAtTime(long time){
        theLogger.fine("Getting Random from " + myFactory.getBehaviorType() + " Group");
        IConvoidCursor cursor = myScoreKeeper.getRandomAtTime(null, time);
        if(cursor != null){
            Step step = cursor.getBestStepAtTime(time);
            if(step != null){
                theLogger.fine("Found Random from " + myFactory.getBehaviorType() + " Group");
                IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, getJobForCursor(cursor));
                return new BehaviorContext().with(player).andActualType(myBehaviorType).and(Detail.RANDOM);
            }
            if(step == null){
                cursor.resetAtTime(time);
                step = cursor.getBestStepAtTime(time);
                if(step != null){
                    theLogger.fine("Found RandomToReset from " + myFactory.getBehaviorType() + " Group");
                    IBehaviorPlayable player = myFactory.buildPlayer(step, cursor, getJobForCursor(cursor));
                    return new BehaviorContext().with(player).andActualType(myBehaviorType)
                            .and(Detail.RANDOM).and(Detail.RESET);
                }
            }
        }
        return BehaviorContext.makeEmpty().and(Detail.RANDOM);
    }

    @SuppressWarnings("empty-statement")
	public void killSpeechJob(SpeechJob job){
        if(!myJobCursorTable.containsKey(job)){
            return;
        }
        theLogger.info("Killing job " + job.getCategoryName() + " from " + myFactory.getBehaviorType() + " Group");
        job.markCanceled();
        Long time = TimeUtils.currentTimeMillis();

        //We want to find the closest previous start act
        //Kill that act and all acts until the end of the sequence
        CategoryCursor cat = job.getCategoryCursor();
        Integer actCount = cat.getActCount();

        Integer cur = cat.getCurrentIndex();
        if(cur == null){
            cur = 0;
        }
        ActCursor ac = cat.getActCursors().get(cur);
        while(!(ac.isStartAct() && ac.isPlayableAtTime(time)) && cur > 0){
            cur--;
            ac = cat.getActCursors().get(cur);
        }
        do{
            ac = cat.getActCursors().get(cur);
            while(ac.getBestStepAtTime(time) != null);
            cur++;
        }while(!ac.isNextRandom() && cur < actCount);
	}

    public void setBackupGroup(CursorGroup cq){
        myBackupGroup = cq;
    }

    public CursorGroup getBackupGroup(){
        return myBackupGroup;
    }

    public static void setRandomizer(Random rand){
        if(rand != null){
            theRandomizer = rand;
        }
    }

    public MeaningScoreKeeper getScoreKeeper(){
        return myScoreKeeper;
    }

	public String getBehaviorType(){
		return myFactory.getBehaviorType();
	}

    public List<IConvoidCursor> getCursors(){
        if(myCursors == null){
            return new ArrayList<IConvoidCursor>();
        }
        return myCursors;
    }
}