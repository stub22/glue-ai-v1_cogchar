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
import org.cogchar.bind.mio.robot.model.ModelRobot;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.app.puma.registry.PumaRegistryClient;

public class VWorldMapperLifecycle extends BasicDebugger implements ServiceLifecycle<VWorldRegistry> {

    private final static String theClassLoader = "classLoader";
    private final static String theMediator = "pumaMediator";
    private final static String theBodyConfig = "bodyConfigSpec";
    private final static String theRegClient = "theRegistryClient";
    
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

        //code for connecting bodies
        ArrayList<BodyConfigSpec> bodyConfig = (ArrayList<BodyConfigSpec>) dependencyMap.get(theBodyConfig);

        for (BodyConfigSpec body : bodyConfig) {
            try {
                getLogger().info("Going through Body Configs");
                vworldreg.setCharID(body.getHumCfg().getFigureID());
                vworldreg.initVWorldHumanoid(body.getRepoClient(), body.getGraphIdentForBony(), body.getHumCfg());
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
