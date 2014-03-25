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
import org.cogchar.app.puma.config.BodyConfigSpec;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.event.CommandEvent;
import org.cogchar.app.puma.event.Updater;
import org.cogchar.app.puma.boot.PumaAppContext;

public class VWorldMapperLifecycle extends BasicDebugger implements ServiceLifecycle<VWorldRegistry> {

    private final static String theClassLoader = "classLoader";
    private final static String theMediator = "pumaMediator";
    private final static String theBodyConfig = "bodyConfigSpec";
    private final static String theRegClient = "theRegistryClient";
    private final static String commandEvent="commandEvent";
    private final static String appContext="appContext";
    
    private final static String[] theClassNameArray = {
        VWorldRegistry.class.getName()
    };
    
    private final static ServiceDependency[] theDependencyArray = {
//        new ServiceDependency(theClassLoader, ClassLoader.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
//        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(theMediator, PumaContextMediator.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(theBodyConfig, ArrayList.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(theRegClient, PumaRegistryClient.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(commandEvent, CommandEvent.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
        new ServiceDependency(appContext, PumaAppContext.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP)
            
    };

    @Override
    public VWorldRegistry createService(Map<String, Object> dependencyMap) {
        VWorldRegistry vworldreg = new VWorldRegistry();
        vworldreg.setRegClient((PumaRegistryClient) dependencyMap.get(theRegClient));
        
        try {
            vworldreg.initVWorldUnsafe((PumaContextMediator) dependencyMap.get(theMediator));
        } catch (Throwable t) {
            getLogger().warn("%%%%%%%%%%%%%%%%%%%%%%% Error with VWorldMapper init %%%%%%%%%%%%%%%%%%%%%%%");
        }
        
        PumaContextCommandBox pCCB=new PumaContextCommandBox(vworldreg);
        pCCB.setAppContext((PumaAppContext)dependencyMap.get(appContext));
        CommandEvent ce=(CommandEvent)dependencyMap.get(commandEvent);
        ce.setUpdater((Updater)pCCB);
        vworldreg.setContextCommandBox(pCCB);
        pCCB.reloadCommandSpace();
        //code for connecting bodies
        ArrayList<BodyConfigSpec> bodyConfig = (ArrayList<BodyConfigSpec>) dependencyMap.get(theBodyConfig);

        for (BodyConfigSpec body : bodyConfig) {
            try {
                System.out.println("STATUS!!!!!!!!!!: ");
                if(body.getGraphIdentForBony()!=null)
                {
                    System.out.println("GraphIdentForBony is non-null: "+body.getGraphIdentForBony());
                }
                if(body.getRepoClient()!=null)
                {
                    System.out.println("REPOCLIENT FOUND: "+(body.getRepoClient()).toString());
                }
				Ident charID = body.getHumCfg().getFigureID();
                getLogger().info("Initializing Virtual World Humanoid for charID={}", charID);
                vworldreg.setCharID(charID);
				getLogger().info("Calling initVworldHumanoid for charID={}", charID);
                vworldreg.initVWorldHumanoid(body.getRepoClient(), body.getGraphIdentForBony(), body.getHumCfg());
				getLogger().info("Calling connnectBonyRobotToHumanoidFigure for charID={}", charID);
				vworldreg.connectBonyRobotToHumanoidFigure(body.getModelRobot());
            } catch (Throwable t) {
                getLogger().error("InitVWorldHumanoid failure");
            }
        }
        //end body connection code

//        vworldreg.initCinema(false, (ClassLoader) dependencyMap.get(theClassLoader));
        vworldreg.initCinema(false, null);  
        
        return vworldreg;
    }

    @Override
    public VWorldRegistry handleDependencyChange(VWorldRegistry client, String changeType, String dependencyName,
            Object dependency, Map<String, Object> availableDependencies) {
        return null;
    }

    @Override
    public String[] getServiceClassNames() {
        return theClassNameArray;
    }

    @Override
    public void disposeService(VWorldRegistry t, Map<String, Object> map) {

        t = null;

    }

    @Override
    public List<ServiceDependency> getDependencySpecs() {
        return Arrays.asList(theDependencyArray);
    }
}
