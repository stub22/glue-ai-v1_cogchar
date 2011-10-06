package org.cogchar.platform.stub;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * 
 * @author Stu B.
 */
public abstract class ThalamusBrokerStub implements PropertyChangeListener, ThalamentSpaceStub {
	private static Logger	theLogger = Logger.getLogger(ThalamusBrokerStub.class.getName());	
	static {
		// theLogger.setLevel(Level.ALL);
	}	
	/**
	 * 
	 */
	// protected		final	Thalamus							myThalamus;
	/**
	 * 
	 */
	//protected		final	StatefulKnowledgeSession			mySKS;

	/**
	 * 
	 * @param t
	 * @param sks
	 */
	public ThalamusBrokerStub() { // Thalamus t, StatefulKnowledgeSession sks) {
		/*
		 * 
		myThalamus 		= t;
		mySKS 			= sks;
		myThalamus.addPropertyChangeListener(this);		
		 * 
		 */
	}
	/**
	 * 
	 * @param globalName
	 */
	public void registerGlobalHandle(String globalName) {
		// mySKS.setGlobal(globalName, this);
	}
	
	 
	public synchronized List<FactHandleStub> getAllFactHandlesMatchingClass(Class clazz) {
		List<FactHandleStub> result = new ArrayList<FactHandleStub>();
		/*
		ClassObjectFilter filter = new ClassObjectFilter(clazz); 
//		Drools 4
//		Iterator<FactHandle> fhi = mySKS.iterateFactHandles(filter);
//		while (fhi.hasNext()) {
//			FactHandle fh = fhi.next();
		Collection<FactHandle> fhc = mySKS.getFactHandles(filter);
		for (FactHandle fh: fhc) {
			result.add(fh);
		}
		 */
		return result;
	}

	public synchronized <T extends Object> List<T> getAllFactsMatchingClass(Class<T> clazz) {
		List<T> result = new ArrayList<T>();
		/*
		 * List<FactHandle> handles = getAllFactHandlesMatchingClass(clazz);
		for (FactHandle fh: handles) {
			T fact = (T) getFactObjectFromHandle(fh);
			result.add(fact);
		}
		 * 
		 */
		return result;
	}
	public synchronized <T extends Object> T getSingleFactMatchingClass(Class<T> clazz) {
		List<T> matchedList = getAllFactsMatchingClass(clazz);
		int mls = matchedList.size();
		if (mls != 1) {
			throw new RuntimeException("Expected single fact of type " + clazz + " but found: " + mls);
		}
		return matchedList.get(0);
	}

	public Object getFactObjectFromHandle(FactHandleStub fh) {
		return fh; //  mySKS.getObject(fh);
	}
/*
	public boolean retractFactForObject(Object o) {
		FactHandle fh = mySKS.getFactHandle(o);
		if (fh != null) {
			retractFactForHandle(fh);
			return true;
		} else {
			theLogger.warning("Could not find handle in workingMemory for object: " + o);
			return false;
		}
	}	

	public void retractFactForHandle(FactHandle fh) {
		mySKS.retract(fh);
	}
	 */
	
	/**
	 * 
	 * @param t
	 */
	public void update(ThalamentStub t) {
		/*
		ThalamentStub fh = mySKS.getFactHandle(t);
		if (fh != null) {
			mySKS.update(fh, t);
		} else {
			theLogger.warning("Can't find factHandle for: " + t);
		}
		 * 
		 */
	}

    public void postFact(ThalamentStub t){
      //  mySKS.insert(t);
    }
}
