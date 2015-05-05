package org.cogchar.bundle.app.vworld.central;

/**
 *
 * @author Major Jacquote II
 */
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.cogchar.bundle.app.vworld.startup.PumaVirtualWorldMapper;
import org.appdapter.core.name.Ident;
import org.osgi.framework.BundleContext;
import org.cogchar.render.sys.module.RenderModule;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.app.puma.config.PumaConfigManager;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.ResourceFileCategory;

import org.cogchar.bind.mio.robot.model.ModelRobot;
import org.cogchar.bundle.app.puma.GruesomeTAProcessingFuncs;
import org.osgi.framework.Bundle;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.impl.thing.route.BasicThingActionRouter;
import org.cogchar.render.model.bony.FigureState;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;

import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.bundle.app.vworld.startup.ModelToFigureStateMappingFuncs;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


public class VWorldRegistry extends BasicDebugger {

    private CogcharRenderContext myCRC;
    private PumaVirtualWorldMapper myVWorldMapper;
    private boolean hasVWorldMapper = true;
    private PumaRegistryClient myRegClient;
	
	// This myCharID field looks quite suspicious.
   // private Ident myCharID;
    private PumaContextCommandBox pCCB;

    public VWorldRegistry() {
        myVWorldMapper = new PumaVirtualWorldMapper();
    }

	// Stu moved this from protected to public, pending further analysis.
    public PumaVirtualWorldMapper getVW()
    {
        return myVWorldMapper;
    }
/*
    public void setCharID(Ident id) {
        myCharID = id;
    }
*/
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
    //From AppUtil 
    //May Split off into it's own class or get rid of all together. 

    public void attachVWorldRenderModule(RenderModule rMod) {
        if (myVWorldMapper != null) {
            myVWorldMapper.attachRenderModule(rMod);
        } else {
            getLogger().error("Unable to attach Render Module.");
        }
    }

    //From PumaAppContext
    public void initVWorldUnsafe(PumaContextMediator mediator) throws Throwable {
        String panelKind = mediator.getPanelKind();

        HumanoidRenderContext hrc = myVWorldMapper.initHumanoidRenderContext(panelKind);
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
            myVWorldMapper.startOpenGLCanvas(allowJFrames, winLis);

        }

        hrc.runPostInitLaunchOnJmeThread();
    }

    protected void initCinema(boolean clearFirst, ClassLoader vizResCL) {

        if (hasVWorldMapper) {

            if (clearFirst) {
                myVWorldMapper.clearCinematicStuff();
            }
            CommandSpace cmdSpc = myRegClient.getCommandSpace(null);
            PumaConfigManager pcm = getConfigManager();
            BasicThingActionRouter router = GruesomeTAProcessingFuncs.getActionRouter();
            myVWorldMapper.initVirtualWorlds(cmdSpc, pcm, router);


            //ClassLoader vizResCL = getSingleClassLoaderOrNull(ResourceFileCategory.RESFILE_OPENGL_JME3_OGRE);
            myVWorldMapper.connectVisualizationResources(vizResCL);
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
        getLogger().warn("PumaBooter firing shutdown events.");
        fireShutdownEvents(bc);
        Bundle sysB = bc.getBundle(0);
        getLogger().warn("PumaBooter asking system bundle to stop(): {}", sysB);
        try {
            sysB.stop();
        } catch (Throwable t) {
            getLogger().error("PumaBooter caught exception during sys-bundle.stop() request", t);
        }
    }
    
    protected void fireShutdownEvents(BundleContext context){
        String filter = OSGiUtils.createFilter("RunnableType", "Shutdown");
        try{
            ServiceReference[] refs = context.getServiceReferences(Runnable.class.getName(), filter);
            if(refs == null){
                return;
            }
            for(ServiceReference ref : refs){
                Runnable r = OSGiUtils.getService(Runnable.class, context, ref);
                if(r != null){
                    r.run();
                }
            }
        }catch(InvalidSyntaxException ex){
        }
        
    }

    protected PumaConfigManager getConfigManager() {
        return myRegClient.getConfigMgr(null);
    }

    protected void stopAndReleaseAllHumanoids() {
        myVWorldMapper.detachAllHumanoidFigures();
    }

    protected void disconnectAllCharsAndMappers() throws Throwable {

        if (hasVWorldMapper) {
            myVWorldMapper.clearCinematicStuff();
        }

        stopAndReleaseAllHumanoids();
    }

    protected void startOpenGLCanvas(boolean wrapInJFrameFlag, java.awt.event.WindowListener optWinLis) throws Exception {
        if (hasVWorldMapper) {
            myVWorldMapper.startOpenGLCanvas(wrapInJFrameFlag, optWinLis);
        } else {
            getLogger().warn("Ignoring startOpenGLCanvas command - no vWorldMapper present");
        }
    }
//-----end PumaAppContext methods

    //From PumaBodyGateway
    protected HumanoidFigure getHumanoidFigure(Ident charID) {
        HumanoidFigure hf = null;
        if (myVWorldMapper != null) {
            HumanoidRenderContext hrc = myVWorldMapper.getHumanoidRenderContext();
            if (hrc != null) {
                hf = hrc.getHumanoidFigureManager().getHumanoidFigure(charID); // myCharID);
            }
        }
        return hf;
    }

    public boolean initVWorldHumanoid(RepoClient qi, final Ident boneConfGraphID, final FigureConfig figConf) throws Throwable {
		Ident figID = figConf.getFigureID();
		getLogger().info("Setting up figureID={} using boneConfigGraphID {}", figID, boneConfGraphID);
        if (myVWorldMapper != null) {
            HumanoidRenderContext hrc = myVWorldMapper.getHumanoidRenderContext();
            // New with "GlobalModes": we'll run hrc.setupHumanoidFigure from here now
            HumanoidFigure hf = hrc.getHumanoidFigureManager().setupHumanoidFigure(hrc, qi, figID, boneConfGraphID, figConf);
            return (hf != null);
        } else {
            getLogger().warn("initVWorldHumanoid doing nothing, because no VWorldMapper is assigned.");
            return false;
        }
    }

	public VWorldRoboPump setupRoboPump(final Ident pumpID, ModelRobot mr, HumanoidFigure hf) throws Exception { 
		VWorldRoboPump pump = new VWorldRoboPump(pumpID, mr, hf);
		pump.completeSetup();
		return pump;
	}
    public void connectBonyRobotToHumanoidFigure(ModelRobot mr, final Ident charID) throws Exception {
		getLogger().info("charID={}, ModelRobot={}, robotID={}", charID,  mr, mr.getRobotId());
        final ModelRobot br = mr; //getBonyRobot();
        if (br == null) {
            getLogger().warn("connection aborting due to missing ModelRobot, for char: {}", charID);
            return;
        }
        final HumanoidFigure hf = getHumanoidFigure(charID);
		getLogger().info("HumanoidFigure={}", hf);
        if (hf != null) {
			VWorldRoboPump pump = setupRoboPump(charID, mr, hf);
        } else {
			getLogger().warn("connection aborting due to missing HumanoidFigure, for charID={}",charID);
		}
    }
}
