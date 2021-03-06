package org.cogchar.render.goody.basic;

import org.cogchar.render.app.entity.VWorldEntityReg;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.WantsThingAction.ConsumpStatus;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import java.awt.Dimension;

/**
 * Created by Stub22 on 5/14/2016.
 */
public interface BasicGoodyCtx {
	public RenderRegistryClient getRRC();

	public VWorldEntityReg getVWER();

	public Dimension getScreenDimension();
	public void applyNewScreenDimension(Dimension newDimension);

	public ConsumpStatus consumeAction(ThingActionSpec actionSpec);

	public void setupAsMainGoodyCtx(); // Bind our top jme-node as the presumed ancestor of all goodies in the scene
}
