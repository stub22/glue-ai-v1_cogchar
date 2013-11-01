package org.cogchar.api.perform;

import org.slf4j.Logger;

/**
 * This trait sets up the monitorModule, using features from our abstract types.
 */
public interface FancyPerfChan<OutJob> {
	//val		myJobsByPerf = new scala.collection.mutable.HashMap[FancyPerformance, OutJob]()

	public void registerOutJobForPerf(FancyPerformance perf, OutJob oj);

	public void markPerfStoppedAndForget(FancyPerformance perf);

	public OutJob getOutJobOrNull(FancyPerformance perf);

	public void requestOutJobCancel(OutJob woj);

	public Logger getMyLogger();

	public void updatePerfStatusQuickly(FancyPerformance perf);

	public void requestOutJobCancelForPerf(FancyPerformance perf);

}
