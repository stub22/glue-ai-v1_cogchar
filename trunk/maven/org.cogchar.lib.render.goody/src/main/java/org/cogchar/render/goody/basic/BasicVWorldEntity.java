package org.cogchar.render.goody.basic;

import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.VWorldEntity;

/**
 * Created by Owner on 5/14/2016.
 */
abstract public class BasicVWorldEntity extends VWorldEntity {
	private BasicGoodyCtx myGoodyCtx;

	protected BasicGoodyCtx getGoodyCtx() {
		return myGoodyCtx;
	}

	protected BasicVWorldEntity(BasicGoodyCtx bgc, Ident uri) {
		super(bgc.getRRC(), uri);
		myGoodyCtx = bgc;
	}
}