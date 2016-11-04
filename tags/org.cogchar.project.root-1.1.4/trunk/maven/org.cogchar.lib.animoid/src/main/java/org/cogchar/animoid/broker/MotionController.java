package org.cogchar.animoid.broker;

public interface MotionController {
	public enum Component {
		FACE_ATTENTION,
		GAZE_PLAN,
	}
	public void setComponentState(Component c, boolean activationState);
	public void setComponentValue(Component c, String value);
	public void setComponentValue(Component c, Integer value);	
	
	public void setGazeState(boolean enabled);
	public void setGazePlanName(String planName);
	public void setGazeSightNumber(Integer sightNumber);
}
