package org.cogchar.joswrap;

import java.util.HashSet;
import java.util.Set;
import org.appdapter.bind.rdf.jena.query.SPARQL_Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Stu B.
 */


public class RepoUpdateCallbackAdapter {
	private static Logger 		theLogger = LoggerFactory.getLogger(RepoUpdateCallbackAdapter.class );
	private static Set<Callback>	theCallbacks = new HashSet<Callback>();
	
	public static interface Callback {
		public void repoUpdateCompleted();
	}
	public static void registerCallback(Callback c) { 
		theCallbacks.add(c);
	}
	public static void notifyCallbacks() { 
		theLogger.info("Sending SPARQL-Update callback notices to {} listeners: {}", theCallbacks.size(), theCallbacks);
		for (Callback c : theCallbacks) {
			c.repoUpdateCompleted();
		}
	}
}
