package org.cogchar.bundle.app.vworld.central;

/**
 *
 * @author Major Jacquote II
 */
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import org.cogchar.bundle.app.vworld.startup.PumaVirtualWorldMapper;
import org.appdapter.core.name.Ident;
import org.osgi.framework.BundleContext;
import org.cogchar.render.sys.module.RenderModule;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.ResourceFileCategory;
import org.cogchar.bind.mio.robot.model.ModelJoint;
import org.cogchar.bind.mio.robot.model.ModelRobot;
import org.cogchar.bundle.app.puma.GruesomeTAProcessingFuncs;
import org.osgi.framework.Bundle;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.impl.thing.basic.BasicThingActionRouter;
import org.cogchar.render.model.bony.FigureState;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.bundle.app.vworld.startup.ModelToFigureStateMappingFuncs;

public class VWorldRegistry extends BasicDebugger {

    private CogcharRenderContext myCRC;
    private PumaVirtualWorldMapper vworld;
    private boolean hasVWorldMapper = true;
    private PumaRegistryClient myRegClient;
    private Ident myCharID;

    public VWorldRegistry() {
        vworld = new PumaVirtualWorldMapper();
    }

    public void setCharID(Ident id) {
        myCharID = id;
    }

    public void setRegClient(PumaRegistryClient rc) {
        myRegClient = rc;
    }
    //From AppUtil 
    //May Split off into it's own class or get rid of all together. 

    public void attachVWorldRenderModule(BundleContext bundleCtx, RenderModule rMod, Ident optVWorldSpecID) {
        if (vworld != null) {
            vworld.attachRenderModule(rMod);
        } else {
            getLogger().error("Unable to attach Render Module.");
        }
    }

    //From PumaAppContext
    public void initVWorldUnsafe(PumaContextMediator mediator) throws Throwable {
        String panelKind = mediator.getPanelKind();

        HumanoidRenderContext hrc = vworld.initHumanoidRenderContext(panelKind);
        boolean allowJFrames = mediator.getFlagAllowJFrames();
        if (allowJFrames) {
            WindowAdapter winLis = new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    getLogger().warn("PumaBooter caught window CLOSED event for OpenGL frame:  {}", e);
                    notifyVWorldWindowClosed();
                }
            };
            getLogger().debug("%%%%%%%%%%%%%%%%%%% Calling startOpenGLCanvas");
            vworld.startOpenGLCanvas(allowJFrames, winLis);

        }

        hrc.runPostInitLaunchOnJmeThread();
    }

    protected void initCinema(boolean clearFirst, ClassLoader vizResCL) {

        if (hasVWorldMapper) {

            if (clearFirst) {
                vworld.clearCinematicStuff();
            }
            CommandSpace cmdSpc = myRegClient.getCommandSpace(null);
            PumaConfigManager pcm = getConfigManager();
            BasicThingActionRouter router = GruesomeTAProcessingFuncs.getActionRouter();
            vworld.initVirtualWorlds(cmdSpc, pcm, router);


            //ClassLoader vizResCL = getSingleClassLoaderOrNull(ResourceFileCategory.RESFILE_OPENGL_JME3_OGRE);
            vworld.connectVisualizationResources(vizResCL);
        } else {
            getLogger().warn("Ignoring initCinema command - no vWorldMapper present");
        }
    }

    protected void notifyVWorldWindowClosed() {
        Bundle anyB = org.osgi.framework.FrameworkUtil.getBundle(getClass());
        BundleContext anyBC = anyB.getBundleContext();
        shutdownOSGiContainer(anyBC);
    }

    protected void shutdownOSGiContainer(BundleContext bc) {
        Bundle sysB = bc.getBundle(0);
        getLogger().warn("PumaBooter asking system bundle to stop(): {}", sysB);
        try {
            sysB.stop();
        } catch (Throwable t) {
            getLogger().error("PumaBooter caught exception during sys-bundle.stop() request", t);
        }
    }

    protected PumaConfigManager getConfigManager() {
        return myRegClient.getConfigMgr(null);
    }

    protected void stopAndReleaseAllHumanoids() {
        vworld.detachAllHumanoidFigures();
    }

    protected void disconnectAllCharsAndMappers() throws Throwable {

        if (hasVWorldMapper) {
            vworld.clearCinematicStuff();
        }

        stopAndReleaseAllHumanoids();
    }

    protected void startOpenGLCanvas(boolean wrapInJFrameFlag, java.awt.event.WindowListener optWinLis) throws Exception {
        if (hasVWorldMapper) {
            vworld.startOpenGLCanvas(wrapInJFrameFlag, optWinLis);
        } else {
            getLogger().warn("Ignoring startOpenGLCanvas command - no vWorldMapper present");
        }
    }
//-----end PumaAppContext methods

    //From PumaBodyGateway
    protected HumanoidFigure getHumanoidFigure() {
        HumanoidFigure hf = null;
        if (vworld != null) {
            HumanoidRenderContext hrc = vworld.getHumanoidRenderContext();
            if (hrc != null) {
                hf = hrc.getHumanoidFigureManager().getHumanoidFigure(myCharID);
            }
        }
        return hf;
    }

    private FigureState setupFigureState(ModelRobot br) {

        FigureState fs = new FigureState();
        List<ModelJoint> allJoints = br.getJointList();
        for (ModelJoint mJoint : allJoints) {
            for (BoneProjectionRange bpr : mJoint.getBoneRotationRanges()) {
                String boneName = bpr.getBoneName();
                // BoneState is returned, but ignored here.
                fs.obtainBoneState(boneName);
            }
        }
        return fs;
    }

    public boolean initVWorldHumanoid(RepoClient qi, final Ident qGraph,
            final FigureConfig hc) throws Throwable {
        if (vworld != null) {
            HumanoidRenderContext hrc = vworld.getHumanoidRenderContext();
            // New with "GlobalModes": we'll run hrc.setupHumanoidFigure from here now
            HumanoidFigure hf = hrc.getHumanoidFigureManager().setupHumanoidFigure(hrc, qi, myCharID, qGraph, hc);
            return (hf != null);
        } else {
            getLogger().warn("initVWorldHumanoid doing nothing, because no VWorldMapper is assigned.");
            return false;
        }
    }

    public void connectBonyRobotToHumanoidFigure(ModelRobot mr) throws Exception {
        final ModelRobot br = mr; //getBonyRobot();
        if (br == null) {
            getLogger().warn("connectToVirtualChar() aborting due to missing ModelRobot, for char: {}", myCharID);
            return;
        }
        final HumanoidFigure hf = getHumanoidFigure();
        if (hf != null) {
            // It is optional to create this state object if there is no humanoid figure to animate.
            // It could be used for some other programming purpose.
            FigureState fs = setupFigureState(br);
            hf.setFigureState(fs);
            br.registerMoveListener(new ModelRobot.MoveListener() {
                @Override
                public void notifyBonyRobotMoved(ModelRobot br) {
                    HumanoidFigure hf = getHumanoidFigure();
                    if (hf != null) {
                        ModelToFigureStateMappingFuncs.propagateState(br, hf);
                    }
                }
            });
        }
    }
}
