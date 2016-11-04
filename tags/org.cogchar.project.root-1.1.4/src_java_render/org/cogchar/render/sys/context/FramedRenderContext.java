package org.cogchar.render.sys.context;

import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.window.WantsWindowStatus;
import org.cogchar.render.sys.window.WindowStatusMonitor;
import org.cogchar.render.sys.window.WindowStatusReader;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Owner on 5/19/2016.
 */
public class FramedRenderContext extends CogcharRenderContext implements WindowStatusMonitor {
	private Dimension myStoredWindowSize = null;
	private WindowStatusReader	myReader = null;
	private Set<WantsWindowStatus> myListeners = new HashSet<WantsWindowStatus>();

	public FramedRenderContext(RenderRegistryClient rrc) {
		super(rrc);
	}

	@Override public Dimension getWindowSize() {
		return myStoredWindowSize;
	}

	@Override public void addListener(WantsWindowStatus wws) {
		myListeners.add(wws);
	}
	public void storeWindowSize(Dimension ws) {
		myStoredWindowSize = ws;
		for(WantsWindowStatus listener : myListeners) {
			listener.notifyWindowSize(ws);
		}
	}
	private Dimension myCurrSizeBuffer = null;
	@Override public void doUpdate(float tpf) {
		if (myReader != null) {
			myCurrSizeBuffer = myReader.getSize(myCurrSizeBuffer);
			if (myCurrSizeBuffer != null) {
				if ((myStoredWindowSize == null) || !myStoredWindowSize.equals(myCurrSizeBuffer)) {
					Dimension copiedVal = (Dimension) myCurrSizeBuffer.clone();
					storeWindowSize(copiedVal);
				}
			}
		}
		super.doUpdate(tpf);
	}

	public void setWindowStatusReader(WindowStatusReader wsr) {
		myReader = wsr;
	}
}
