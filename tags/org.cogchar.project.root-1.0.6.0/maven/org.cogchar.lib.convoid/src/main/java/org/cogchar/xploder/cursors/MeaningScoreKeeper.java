package org.cogchar.xploder.cursors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author matt
 */
public class MeaningScoreKeeper {
	private static Logger	theLogger = Logger.getLogger(MeaningScoreKeeper.class.getName());
	private static Random								theRandomizer = new Random();
    private Map<String, Meaning>                        myMeaningTable;
	private Map<String, List<IConvoidCursor>>           myPlayableTable;
	private List<IConvoidCursor>						myCursors;
	private List<String>								myMeanings;
    protected Long                                      myResetTimespan;
    private Long                                        myCreatedTime;

    public MeaningScoreKeeper(List<IConvoidCursor> cursors, Double thresh, Long reset){
        myCreatedTime = TimeUtils.currentTimeMillis();
        myMeaningTable = new HashMap<String, Meaning>();
        myPlayableTable = new HashMap<String, List<IConvoidCursor>>();
        populateTables(cursors);
        myResetTimespan = reset;
    }
    
    private void populateTables(List<IConvoidCursor> playables){
        myCursors = playables;
        if(playables == null || playables.isEmpty()){
            theLogger.severe("MeaningScoreKeeper initialized with no cursors.");
            return;
        }
        for(IConvoidCursor cp : myCursors){
            for(String m : cp.getMeanings()){
                if(!myPlayableTable.containsKey(m)){
                    myPlayableTable.put(m, new ArrayList<IConvoidCursor>());
                }
                myPlayableTable.get(m).add(cp);
            }
        }
		myMeanings = new ArrayList(myPlayableTable.keySet());
        initializeWithRandomMeanings(5);
    }
    
    private void initializeWithRandomMeanings(int num){
        int count = myMeanings.size();
        while(num-- > 0){
            String meaning = myMeanings.get(theRandomizer.nextInt(count));
            addMeaningAtTime(meaning, myCreatedTime);
        }
    }

	public void addMeaningsAtTime(Map<String,Double> meanings, double perc, long time){
		for(Entry<String, Double> entry : meanings.entrySet()){
            Double score = perc*entry.getValue();
			addMeaningPercAtTime(entry.getKey(), score, time);
		}
	}

	public void addMeaningAtTime(String m, long time){
        addMeaningPercAtTime(m, 1.0, time);
	}

	public void addMeaningPercAtTime(String m, double perc, long time){
        if(!myMeanings.contains(m)){
            return;
        }
		if(myMeaningTable.containsKey(m)){
			myMeaningTable.get(m).updatePercAtTime(perc, time);
			return;
		}
        long alive = time - myCreatedTime;
        long scoreTime = myCreatedTime + (long)perc*alive;
		myMeaningTable.put(m, new Meaning(m, scoreTime));
	}

    public static IConvoidCursor getRandomMatchFromScoresAtTime(Map<IConvoidCursor, Double> scores,
            List<IConvoidCursor> cursors, String meaning, long time, double threshold){
        theLogger.finest("Getting random from best for meaning: " + meaning);
        List<Double> pscores = new ArrayList();
        Map<Double, IConvoidCursor> passing = new HashMap();
		for(IConvoidCursor a : cursors){
            if(!a.isPlayableAtTime(time) || !scores.containsKey(a)){
                continue;
            }
			double score = scores.get(a);
			if(score > threshold && (meaning == null || a.getMeanings().contains(meaning))){
                theLogger.finest("Found matching playable cursor: " + a);
				passing.put(score,a);
                pscores.add(score);
			}
		}
        Collections.sort(pscores);
        Collections.reverse(pscores);
        if(!pscores.isEmpty()){
            int max = Math.min(pscores.size(), 3);
            return passing.get(pscores.get(theRandomizer.nextInt(max)));
        }
        return null;
    }

    public static IConvoidCursor getRandomBestMatchFromMeanings(
            Map<IConvoidCursor, Double> scores, List<IConvoidCursor> cursors,
            Map<String,Double> meanings, long time, double threshold){
        if(meanings != null){
            String matchStr = "";
            for(Entry<String,Double> e : meanings.entrySet()){
                matchStr += e.getKey() + "(" + e.getValue() + ")" + ", ";
            }
            theLogger.finest("Getting random from best for meanings: " + matchStr);
        }
        List<Double> pscores = new ArrayList();
        Map<IConvoidCursor, Double> passing = new HashMap();
		for(IConvoidCursor a : cursors){
            if(!a.isPlayableAtTime(time) || !scores.containsKey(a)){
                continue;
            }
			double score = scores.get(a);
			if(score > threshold){
				passing.put(a,score);
			}
		}
        adjustCursorScoresForMeanings(passing, meanings);
        Map<Double, List<IConvoidCursor>> scoredCursors = new HashMap();
        for(Entry<IConvoidCursor, Double> e : passing.entrySet()){
            Double score = e.getValue();
            if(!scoredCursors.containsKey(score)){
                scoredCursors.put(score, new ArrayList<IConvoidCursor>());
            }
            scoredCursors.get(score).add(e.getKey());
            pscores.add(score);
        }
        Collections.sort(pscores);
        Collections.reverse(pscores);
        if(!pscores.isEmpty()){
            int max = Math.min(pscores.size(), 3);
            Double score = pscores.get(theRandomizer.nextInt(max));
            List<IConvoidCursor> bestCursors = scoredCursors.get(score);
            return bestCursors.get(theRandomizer.nextInt(bestCursors.size()));
        }
        return null;
    }
    
    public static IConvoidCursor getBestMatchFromMeanings(
            Map<IConvoidCursor, Double> scores, List<IConvoidCursor> cursors,
            Map<String,Double> meanings, long time, double threshold){
        String matchStr = "";
        for(Entry<String,Double> e : meanings.entrySet()){
            matchStr += e.getKey() + "(" + e.getValue() + ")" + ", ";
        }
        theLogger.finest("Getting best match for meanings: " + matchStr);
        Double best = threshold;
        IConvoidCursor passing = null;
        adjustCursorScoresForMeanings(scores, meanings);
		for(IConvoidCursor a : cursors){
            if(!a.isPlayableAtTime(time) || !scores.containsKey(a)){
                continue;
            }
			double score = scores.get(a);
			if(score > best){
				passing = a;
                best = score;
			}
		}
        return passing;
    }

    private static void adjustCursorScoresForMeanings(
            Map<IConvoidCursor, Double> cursors, Map<String,Double> meanings){
        if(meanings == null){
            return;
        }
        for(Entry<IConvoidCursor, Double> e : cursors.entrySet()){
            Double score = 0.0;
            for(String m : e.getKey().getMeanings()){
                if(meanings.containsKey(m)){
                    score += meanings.get(m);
                }
            }
            score *= e.getValue();
            e.setValue(score);
        }
    }

    public IConvoidCursor getBestMatchFromTimestampsAtTime(Map<IConvoidCursor, Double> scores,
            String meaning, long time, long resetTime, double threshold){
		IConvoidCursor best = null;
		long bestTime = time;
		for(IConvoidCursor a : scores.keySet()){
			double score = scores.get(a);
            if(a.getLastAdvanceTime() == null){
                continue;
            }
            long elapsed = time - a.getLastAdvanceTime();
			if(score > threshold && a.getLastAdvanceTime() < bestTime && 
                    elapsed > resetTime &&
                    (meaning == null || a.getMeanings().contains(meaning))){
				bestTime = a.getLastAdvanceTime();
				best = a;
			}
		}
        return best;
    }

    public static IConvoidCursor getBestToReset(Map<IConvoidCursor, Double> scores,
            Map<String,Double> meanings, long time, long resetTime, double threshold){
		IConvoidCursor best = null;
        double bestScore = 0.0;
		for(IConvoidCursor a : scores.keySet()){
			double score = scores.get(a);
            if(a.getLastAdvanceTime() == null){
                continue;
            }
            long elapsed = time - a.getLastAdvanceTime();
			if(score > threshold && elapsed > resetTime){
                Double newScore = 0.0;
                for(Entry<String,Double> e : meanings.entrySet()){
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
		}
        return best;
    }

	public Map<IConvoidCursor, Double> getScoresAtTime(long time){
		Map<IConvoidCursor, Double> scores = new HashMap<IConvoidCursor, Double>();

		for(Meaning meaning : myMeaningTable.values()){
			String m = meaning.getMeaning();
			if(myPlayableTable.containsKey(m)){
				for(IConvoidCursor playable : myPlayableTable.get(m)){
					if(scores.containsKey(playable)){
						scores.put(playable, scores.get(playable) + meaning.getStrengthAtTime(time));
					}else{
						scores.put(playable, meaning.getStrengthAtTime(time));
					}
				}
			}
		}
		return scores;
	}

	public IConvoidCursor getRandomAtTime(Set<IConvoidCursor> playables, long time){
		List<IConvoidCursor> safeList = new ArrayList<IConvoidCursor>();
		if(playables != null){
			for(IConvoidCursor p : playables){
				if(p.isPlayableAtTime(time) && p.isRandom())
					safeList.add(p);
			}
		}
		if(safeList.size() == 0){
			for(IConvoidCursor p : myCursors){
				if(p.isPlayableAtTime(time) && p.isRandom())
					safeList.add(p);
			}
		}
		if(safeList.size() == 0){
			for(IConvoidCursor p : myCursors){
                Long last = p.getLastAdvanceTime();
                long elapsed;
                if(last == null){
                    elapsed = myResetTimespan + 1;
                }else{
                    elapsed = time - last;
                }
				if(elapsed > myResetTimespan && p.isRandom())
					safeList.add(p);
			}
		}
		if(safeList.size() == 0)
			return null;

		int i = theRandomizer.nextInt(safeList.size());
		return safeList.get(i);
	}

    public Meaning getMeaning(String m){
        if(!myMeaningTable.containsKey(m)){
            return null;
        }
        return myMeaningTable.get(m);
    }
    public Double getMeaningScoreAtTime(String m, long time){
        if(m == null || !hasMeaning(m)){
            return null;
        }
        Meaning meaning = getMeaning(m);
        if(meaning == null ){
            return 0.0;
        }
        return meaning.getStrengthAtTime(time);
    }

	public boolean hasMeaning(String meaning){
		return myMeanings.contains(meaning);
	}
    public List<String> getMeanings(){
        return myMeanings;
    }
    public List<IConvoidCursor> getCursors(){
        return myCursors;
    }
    public Long getResetTimespan(){
        return myResetTimespan;
    }
    public static void setRandomizer(Random rand){
        if(rand != null){
            theRandomizer = rand;
        }
    }
    public Integer getMeaningScoreCount(){
        return myMeaningTable.size();
    }
}