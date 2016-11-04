package org.cogchar.bundle.app.vworld.central;

import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.bundle.app.puma.GruesomeTAProcessingFuncs;
import org.cogchar.bundle.app.vworld.startup.PumaVirtualWorldMapper;

import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.api.thing.WantsThingAction;
import org.cogchar.impl.thing.route.BasicThingActionRouter;

/**
 * Created by Owner on 3/29/2016.
 */
public class GruesomeVWorldRegistry extends StatefulVWorldRegistry {

	private WantsThingAction 	myRouter = null;
	public WantsThingAction getRouter() {
		return myRouter;
	}

	@Override protected void initCinema(boolean clearFirst, ClassLoader vizResCL) {

		if (hasVWorldMapper()) {
			PumaVirtualWorldMapper vwm = getVWM();
			if (clearFirst) {
				vwm.clearCinematicStuff();
			}

			CommandSpace cmdSpc = getRegClient().getCommandSpace(null);
			PumaConfigManager pcm = getConfigManager();
			BasicThingActionRouter router = GruesomeTAProcessingFuncs.getActionRouter();
			myRouter = router;
			vwm.initVirtualWorlds(cmdSpc, pcm, router);


			//ClassLoader vizResCL = getSingleClassLoaderOrNull(ResourceFileCategory.RESFILE_OPENGL_JME3_OGRE);
			vwm.connectVisualizationResources(vizResCL);
		} else {
			getLogger().warn("Ignoring initCinema command - no vWorldMapper present");
		}
	}

}
