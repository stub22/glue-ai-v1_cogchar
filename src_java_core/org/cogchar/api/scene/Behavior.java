package org.cogchar.api.scene;

import org.appdapter.api.module.Module;

public interface Behavior<Ctx extends Scene> extends Module<Ctx> {
	public long getMillsecSinceStart();
}
