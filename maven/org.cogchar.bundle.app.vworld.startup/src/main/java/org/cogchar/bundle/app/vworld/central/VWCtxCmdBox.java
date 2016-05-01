package org.cogchar.bundle.app.vworld.central;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.boot.PumaAppContext;
import org.cogchar.bundle.app.vworld.startup.PumaVirtualWorldMapper;
import org.cogchar.app.puma.event.Updater;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;

import org.cogchar.render.model.humanoid.HumanoidFigure_SinbadTest;
import org.cogchar.render.model.humanoid.HumanoidFigureManager;
import org.cogchar.render.scene.goody.PathMgr;
import org.cogchar.render.scene.goody.SpatialAnimMgr;
import org.cogchar.render.sys.goody.GoodyGameFeatureAdapter;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.config.TriggerConfig;
import org.cogchar.bundle.app.vworld.busker.TriggerItems;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.platform.trigger.BoxSpace;

/**
 *  2016-04-26 discovered duplicate (in name) of PumaContextCommandBox.
 *  This one is used only from VWorld, while the other is used only in headless/robot deploys.
 *
 * This impl depends on having a PumaAppContext for several operations, which in turn
 * currently implies that we *must* be running in an OSGi context (although the latter point
 * is not directly relied upon by this impl) - note that BundleContext is a constructor arg
 * to
 *
 */
public class VWCtxCmdBox extends CogcharScreenBox implements Updater {

    private ExecutorService myExecService;
    private VWorldRegistry vwr;
    private PumaAppContext myPAC;
    private PumaRegistryClient myRegClient;
    private BoxSpace box;

    protected VWCtxCmdBox(VWorldRegistry vr, PumaRegistryClient reg, Ident ctxID) {

        vwr = vr;
        box=reg.getTargetBoxSpace(null);
        
      
        box.addBox(ctxID,this);

    }

    protected void setVWorldRegistry(VWorldRegistry registry) {
        vwr = registry;
    }

    public PumaVirtualWorldMapper getVirtualWorld() {
        if (vwr == null) {
            return null;
        }

        return vwr.getVWM();

    }

    public void setAppContext(PumaAppContext pac) {
        myPAC = pac;
    }

    public boolean triggerStartAnimation(Ident uri) {
        boolean result;

        result = getPathMgr().controlAnimationByName(uri, PathMgr.ControlAction.PLAY);

        if (!result) {
            result = getThingAnimMgr().controlAnimationByName(uri, SpatialAnimMgr.ControlAction.PLAY);
        }

        return result;
    }

    public boolean triggerStopAnimation(Ident uri) {
        boolean result;

        result = getPathMgr().controlAnimationByName(uri, PathMgr.ControlAction.STOP);

        if (!result) {
            result = getThingAnimMgr().controlAnimationByName(uri, SpatialAnimMgr.ControlAction.STOP);
        }

        return result;
    }

    public boolean databallUpdate(String action, String text) {
        return DataballGoodyBuilder.getTheBallBuilder().performAction(action, text);
    }

    public void processUpdate(String Request, boolean forceFreshDefaultRepo) {
            processUpdateRequestAsync(Request, forceFreshDefaultRepo);
    }

    protected HumanoidRenderContext getHRC() {
        return getVirtualWorld().getHumanoidRenderContext();
    }

    public GoodyGameFeatureAdapter getGameFeatureAdapter() {
        return getHRC().getGameFeatureAdapter();
    }

    public HumanoidFigureManager getFigureManager() {
        return getHRC().getHumanoidFigureManager();
    }

    public PathMgr getPathMgr() {
        return getHRC().getGoodyRenderRegistryClient().getScenePathFacade(null);
    }

    public SpatialAnimMgr getThingAnimMgr() {
        return getHRC().getGoodyRenderRegistryClient().getSceneAnimFacade(null);
    }

    public void resetMainCameraLocation() {
        getHRC().setDefaultCameraLocation();
    }

    public HumanoidFigure_SinbadTest getSinbad() {
        BonyRenderContext brc = getHRC();
        RenderConfigEmitter bce = brc.getConfigEmitter();
        HumanoidFigureManager hfm = getFigureManager();
        
        //Major 3-25-2014
        //This check was put into place because we were getting ClassCastExceptions when trying to 
        //Cast a HumanoidFigure down to a HumanoidFigure_SinbadTest.  
       if(hfm.getHumanoidFigure(bce.SINBAD_CHAR_IDENT()) instanceof HumanoidFigure_SinbadTest){
        return (HumanoidFigure_SinbadTest) hfm.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
       }
        getLogger().error("Could not cast HumanoidFigure to HumanoidFigure_SinbadTest");
        return null;
    }
    
    public void reloadCommandSpace()
    {
        TriggerConfig ti=myPAC.reloadCommandSpace();
        
        TriggerItems.populateCommandSpace(ti.getRepoClient(), ti.getCommandSpace(), ti.getBoxSpace());
    }
    
    private ExecutorService getExecService() {
        if (myExecService == null) {
            myExecService = Executors.newSingleThreadExecutor();
        }
        return myExecService;
    }

    final public static String WORLD_CONFIG = "worldconfig";
    final public static String BONE_ROBOT_CONFIG = "bonerobotconfig";
    final public static String MANAGED_GCS = "managedglobalconfigservice";
    final public static String ALL_HUMANOID_CONFIG = "allhumanoidconfig";
    final public static String THING_ACTIONS = "thingactions";

	// Here "update" is exceptional "update system wiring" request.
    // Currently used from TriggerItems (3 places), and from VWCtxCmdBox.processUpdate.

    public Future<Boolean> processUpdateRequestAsync(final String request, final boolean resetMainConfigFlag) {
        // Do the actual updates on a new thread. That way we don't block the render thread. Much less intrusive, plus this way things
        // we need to enqueue on main render thread will actually complete -  it must not be blocked during some of the update operations!
        // This brings up an interesting point: we are probably doing far too much on the main jME thread!
        logInfo("Requesting async-processing of kind: " + request);
        // boolean success = true;
        ExecutorService execSvc = getExecService();

        Callable<Boolean> c = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return processUpdateRequestNow(request, resetMainConfigFlag);
            }
        };
        Future<Boolean> resultFuture = execSvc.submit(c);
        return resultFuture;

    }

    /**	This simply forwards calls to myPAC, which is a PumaAppContext, currently (2016-04-27) required to be osgi-wired.
     * Called only indirectly after scheduling by processUpdateRequestAsync() above
     * above.
     *
     * @param request
     * @param resetMainConfigFlag
     * @return
     */
    private boolean processUpdateRequestNow(String request, final boolean resetMainConfigFlag) {
        boolean successFlag = true;
        if (WORLD_CONFIG.equals(request.toLowerCase())) {
            vwr.initCinema(false, null);
        } else if (BONE_ROBOT_CONFIG.equals(request.toLowerCase())) {
            myPAC.reloadBoneRobotConfig();
        } else if (MANAGED_GCS.equals(request.toLowerCase())) {
            final PumaConfigManager pcm = myPAC.getConfigManager();
            pcm.clearOSGiComps();
            myPAC.reloadGlobalConfig();
        } else if (ALL_HUMANOID_CONFIG.equals(request.toLowerCase())) {
            // This also calls initCinema
            myPAC.reloadAll(resetMainConfigFlag);
            vwr.initCinema(false, null);
        } else if (THING_ACTIONS.equals(request.toLowerCase())) {
            myPAC.resetMainConfigAndCheckThingActions();
        } else {
            getLogger().warn("PumaAppContext did not recognize the config update to be performed: {}", request);
            successFlag = false;
        }
        return successFlag;
    }

    public RepoClient getMainConfigRepoClient() {
        PumaConfigManager pcm = myPAC.getConfigManager();
        return pcm.getMainConfigRepoClient();
    }
}
