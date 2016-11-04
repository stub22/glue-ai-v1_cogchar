package org.cogchar.api.scene;

public interface Guard extends CreatedFromSpec {
	public boolean isSatisfied(Scene bscene);
}
