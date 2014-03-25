package org.cogchar.bundle.app.vworld.central;

/**
 *
 * @author Major Jacquote II <mjacquote@gmail.com>
 */
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.app.puma.config.BodyConfigSpec;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.event.CommandEvent;
import org.cogchar.app.puma.event.Updater;
import org.cogchar.app.puma.boot.PumaAppContext;

public class VWorldMapperLifecycle extends BasicDebugger implements ServiceLifecycle<VWorldRegistry> {

    private final static String REGKEY_ClassLoader = "classLoader";
    private final static String REGKEY_Mediator = "pumaMediator";
    private final static String REGKEY_BodyConfSpec = "bodyConfigSpec";
    private final static String REGKEY_PumaRegCli = "theRegistryClient";
    private final static String REGKEY_CommandEvent="commandEvent";
    private final static String REGKEY_AppContext="appContext";
    
    private final static String[] theClassNameArray = {
        VWorldRegistry.class.getName()
    };
    
    private final static ServiceDependency[] theDependencyArray = {
//        new ServiceDependency(theClassLoader, ClassLoader.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
//        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
		makeUnaryStaticServiceDep(REGKEY_Mediator, PumaContextMediator.class),
		makeUnaryStaticServiceDep(REGKEY_BodyConfSpec, ArrayList.class),
		makeUnaryStaticServiceDep(REGKEY_PumaRegCli, PumaRegistryClient.class),
		makeUnaryStaticServiceDep(REGKEY_CommandEvent, CommandEvent.class),
		makeUnaryStaticServiceDep(REGKEY_AppContext, PumaAppContext.class)

		/*
		 ABOVE 5 lines replaces the below 5 lines.   D.R.Y.
		 * 
        new ServiceDependency(REGKEY_Mediator, PumaContextMediator.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(REGKEY_BodyConfSpec, ArrayList.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(REGKEY_PumaRegCli, PumaRegistryClient.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(REGKEY_CommandEvent, CommandEvent.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(REGKEY_AppContext, PumaAppContext.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP)
		*/
            
    };
	
	private static ServiceDependency makeUnaryStaticServiceDep(String regKey, Class depClazz) { 
		return new ServiceDependency(regKey, depClazz.getName(),  ServiceDependency.Cardinality.MANDATORY_UNARY, 
				ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP);
	}

    @Override public VWorldRegistry createService(Map<String, Object> dependencyMap) {
        VWorldRegistry vworldreg = new VWorldRegistry();
        vworldreg.setRegClient((PumaRegistryClient) dependencyMap.get(REGKEY_PumaRegCli));
        
        try {
            vworldreg.initVWorldUnsafe((PumaContextMediator) dependencyMap.get(REGKEY_Mediator));
        } catch (Throwable t) {
            getLogger().warn("%%%%%%%%%%%%%%%%%%%%%%% Error with VWorldMapper init %%%%%%%%%%%%%%%%%%%%%%%");
        }
        PumaContextCommandBox pCCB=new PumaContextCommandBox(vworldreg);
        pCCB.setAppContext((PumaAppContext)dependencyMap.get(REGKEY_AppContext));
        CommandEvent ce=(CommandEvent)dependencyMap.get(REGKEY_CommandEvent);
        ce.setUpdater((Updater)pCCB);
        vworldreg.setContextCommandBox(pCCB);
        pCCB.reloadCommandSpace();
        //code for connecting bodies
        ArrayList<BodyConfigSpec> bodyConfig = (ArrayList<BodyConfigSpec>) dependencyMap.get(REGKEY_BodyConfSpec);

        for (BodyConfigSpec body : bodyConfig) {
            try {
                Ident		boneSrcGraphID = body.getBoneSrcGraphID();
				RepoClient	repoCli = body.getRepoClient();
				
                if (boneSrcGraphID != null) {
                    getLogger().debug("boneSrcGraphID is non-null {}", boneSrcGraphID);
                }
                if (repoCli !=null) {
                    getLogger().debug("REPOCLIENT FOUND: {}", repoCli);
                }
				FigureConfig humaFigCfg = body.getHumaFigureConfig();
				
				Ident charID = humaFigCfg.getFigureID();

				getLogger().info("Calling initVworldHumanoid for charID={}", charID);
                vworldreg.initVWorldHumanoid(body.getRepoClient(), boneSrcGraphID, humaFigCfg);
				getLogger().info("Calling connnectBonyRobotToHumanoidFigure for charID={}", charID);
				vworldreg.connectBonyRobotToHumanoidFigure(body.getModelRobot(), charID);
            } catch (Throwable t) {
                getLogger().error("InitVWorldHumanoid failure");
            }
        }
        //end body connection code

//        vworldreg.initCinema(false, (ClassLoader) dependencyMap.get(theClassLoader));
        vworldreg.initCinema(false, null);  
        
        return vworldreg;
    }

    @Override  public VWorldRegistry handleDependencyChange(VWorldRegistry client, String changeType, String dependencyName,
            Object dependency, Map<String, Object> availableDependencies) {
        return null;
    }

    @Override public String[] getServiceClassNames() {
        return theClassNameArray;
    }

    @Override public void disposeService(VWorldRegistry t, Map<String, Object> map) {
        t = null;
    }

    @Override public List<ServiceDependency> getDependencySpecs() {
        return Arrays.asList(theDependencyArray);
    }
}
