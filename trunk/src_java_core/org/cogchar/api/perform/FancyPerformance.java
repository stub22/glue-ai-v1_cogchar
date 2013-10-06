package org.cogchar.api.perform;

/**
 * This trait sets up the monitorModule, using features from our abstract types.
 */
public interface FancyPerformance { //  extends Performance[_, _, _ <: FancyTime] {
	public Performance.State getFancyPerfState();

	public void syncWithFancyPerfChanNow();

	public void markFancyState(Performance.State s);

	// We cannot narrow the type of this type param in the overrides.  We can only widen it!
	// So, this def winds up being no better than passing in Object as the argument.
	// def markFancyCursor[Cur](c : Cur, notify : Boolean) : Unit

	public FancyPerfChan getFancyPerfChan();

	public void requestOutputJobCancel();
}
