package org.cogchar.render.goody.basic;

import org.cogchar.render.app.entity.VWorldEntityReg;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.WantsThingAction.ConsumpStatus;

import java.awt.Dimension;

/**
 * Created by Owner on 5/14/2016.
 */
public interface BasicGoodyCtx {
	public GoodyRenderRegistryClient getGRRC();

	public VWorldEntityReg getVWER();

	public Dimension getScreenDimension();
	public void applyNewScreenDimension(Dimension newDimension);

	public ConsumpStatus consumeAction(ThingActionSpec actionSpec);
}
