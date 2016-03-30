package org.cogchar.bundle.app.vworld.central;

import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.bundle.app.vworld.startup.PumaVirtualWorldMapper;

/**
 * Created by Owner on 3/29/2016.
 */
public abstract class StatefulVWorldRegistry extends VWorldRegistry {
	private PumaVirtualWorldMapper myVWorldMapper;
	// protected boolean hasVWorldMapper = true;
	private PumaRegistryClient myRegClient;
	private PumaContextCommandBox pCCB;

	protected PumaVirtualWorldMapper makeVWMapper() {
		return new PumaVirtualWorldMapper();
	}

	@Override  public PumaVirtualWorldMapper getVWM() {
		if (myVWorldMapper == null) {
			myVWorldMapper = makeVWMapper();
		}
		return myVWorldMapper;
	}
	@Override protected boolean hasVWorldMapper() {
		PumaVirtualWorldMapper vw = getVWM();
		return (vw != null);
	}
	@Override protected PumaRegistryClient getRegClient() {
		return myRegClient;
	}

	public void setRegClient(PumaRegistryClient rc) {
		myRegClient = rc;
	}

	public void setContextCommandBox(PumaContextCommandBox pCCB)
	{
		this.pCCB=pCCB;
	}

	public PumaContextCommandBox getContextCommnandBox()
	{
		return pCCB;
	}

}
