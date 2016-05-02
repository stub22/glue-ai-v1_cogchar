package org.cogchar.bundle.app.vworld.central;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.boot.PumaSysCtx;
import org.cogchar.app.puma.config.BodyHandleRecord;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.event.CommandEvent;
import org.cogchar.app.puma.event.Updater;
import org.cogchar.app.puma.registry.PumaRegistryClient;

import org.cogchar.api.humanoid.FigureConfig;

import java.util.ArrayList;

/**
 * Created by Owner on 4/4/2016.
 */
public class VWorldInitHelper extends BasicDebugger {
	// Called to do the real work of VWorldMapperLifecycle.createService
	public void connectRegistry(StatefulVWorldRegistry vworldreg, PumaContextMediator pcMediator,
								PumaSysCtx psctx, CommandEvent ce, PumaRegistryClient pumaRegCli,
								ArrayList<BodyHandleRecord> bodyHandleRecList) {

		vworldreg.setRegClient(pumaRegCli);

		String ctxURI = pcMediator.getSysContextRootURI();
		Ident ctxID=new FreeIdent(ctxURI);
		try {
			vworldreg.initVWorldUnsafe(pcMediator);
		} catch (Throwable t) {
			getLogger().warn("%%%%%%%%%%%%%%%%%%%%%%% Error with VWorldMapper init %%%%%%%%%%%%%%%%%%%%%%%");
		}
		VWCtxCmdBox vwccb = new VWCtxCmdBox(vworldreg, pumaRegCli, ctxID);
		vwccb.setAppContext(psctx);

		ce.setUpdater((Updater)vwccb);
		vworldreg.setContextCommandBox(vwccb);
		vwccb.reloadCommandSpace();
		//code for connecting bodies

		for (BodyHandleRecord body : bodyHandleRecList) {
			try {
				Ident		boneSrcGraphID = body.getBoneSrcGraphID();
				RepoClient repoCli = body.getRepoClient();

				if (boneSrcGraphID != null) {
					getLogger().debug("boneSrcGraphID is non-null {}", boneSrcGraphID);
				}
				if (repoCli !=null) {
					getLogger().debug("REPOCLIENT FOUND: {}", repoCli);
				}
				FigureConfig humaFigCfg = body.getHumaFigureConfig();

				Ident figureID = humaFigCfg.getFigureID();

				getLogger().info("Calling initVworldHumanoid for charID={} and boneSrcGraphID={}", figureID, boneSrcGraphID);
				vworldreg.initVWorldHumanoid(body.getRepoClient(), boneSrcGraphID, humaFigCfg);
				getLogger().info("Calling connnectBonyRobotToHumanoidFigure for charID={}", figureID);
				vworldreg.connectBonyRobotToHumanoidFigure(body.getModelRobot(), figureID);
			} catch (Throwable t) {
				getLogger().error("InitVWorldHumanoid failure");
			}
		}
		//end body connection code

//        vworldreg.initCinema(false, (ClassLoader) dependencyMap.get(theClassLoader));
		vworldreg.initCinema(false, null);

	}
}
