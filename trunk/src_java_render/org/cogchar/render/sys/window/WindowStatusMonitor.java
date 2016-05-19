package org.cogchar.render.sys.window;

import java.awt.*;

/**
 * Created by Owner on 5/19/2016.
 */
public interface WindowStatusMonitor {
	public Dimension getWindowSize();
	public void addListener(WantsWindowStatus wws);
}
