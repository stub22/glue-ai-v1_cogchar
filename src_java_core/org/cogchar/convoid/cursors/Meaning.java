package org.cogchar.convoid.cursors;

/**
 *
 * @author matt
 */
public class Meaning {
	private static double base = 2.5;
	private static double decay = 1.0;

	private Long myTimeRecieved;
	private String myMeaning;

	public Meaning(String m, long time){
		myMeaning = m;
		myTimeRecieved = time;
	}

	public Long getTimeRecieved(){
		return myTimeRecieved;
	}

	public String getMeaning(){
		return myMeaning;
	}

	public Double getStrengthAtTime(long time){
		Long timeDiff = time - myTimeRecieved;
		double minutesDiff = (double)timeDiff/(1000.0*60.0);
		double strength = Math.pow(base, -decay * minutesDiff);
		return strength;
	}

	public void updateAtTime(long time){
		//10 second bias for hearing a meaning again.
		myTimeRecieved = time + 10000;
	}

    public void updatePercAtTime(double perc, long time){
        long elapsed = time - myTimeRecieved;
        if(elapsed < 0){
            myTimeRecieved += 5000L;
            return;
        }
        myTimeRecieved += (long)(elapsed*perc);
    }
}
