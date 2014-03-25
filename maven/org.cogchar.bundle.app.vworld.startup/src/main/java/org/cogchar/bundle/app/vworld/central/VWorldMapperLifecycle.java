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
import java.util.HashMap;
import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.app.puma.config.BodyHandleRecord;
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
import org.jflux.api.registry.basic.BasicDescriptor;
import org.jflux.api.service.binding.ServiceBinding;

public class VWorldMapperLifecycle extends BasicDebugger implements ServiceLifecycle<VWorldRegistry> {

    private final static String REGKEY_ClassLoader		= "classLoader";
    private final static String DEPKEY_Mediator			= "puma-mediator";
    private final static String DEPKEY_BodyHandleRec	= "body-handle-rec";
    private final static String DEPKEY_PumaRegCli		= "puma-reg-client";
    private final static String DEPKEY_CommandEvent		= "command-event";
    private final static String DEPKEY_AppContext		= "app-context";
    
    private final static String[] theClassNameArray = {
        VWorldRegistry.class.getName()
    };
    
    private final static ServiceDependency[] theDependencyArray = {
//        new ServiceDependency(theClassLoader, ClassLoader.class.getName(), ServiceDependency.Cardinality.MANDATORY_UNARY,
//        ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP),
		makeUnaryStaticServiceDep(DEPKEY_Mediator, PumaContextMediator.class),
		makeUnaryStaticServiceDep(DEPKEY_BodyHandleRec, ArrayList.class),
		makeUnaryStaticServiceDep(DEPKEY_PumaRegCli, PumaRegistryClient.class),
		makeUnaryStaticServiceDep(DEPKEY_CommandEvent, CommandEvent.class),
		makeUnaryStaticServiceDep(DEPKEY_AppContext, PumaAppContext.class)
    };
	
	private static ServiceDependency makeUnaryStaticServiceDep(String regKey, Class depClazz) { 
		return new ServiceDependency(regKey, depClazz.getName(),  ServiceDependency.Cardinality.MANDATORY_UNARY, 
				ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP);
	}

    @Override public VWorldRegistry createService(Map<String, Object> dependencyMap) {
        VWorldRegistry vworldreg = new VWorldRegistry();
        vworldreg.setRegClient((PumaRegistryClient) dependencyMap.get(DEPKEY_PumaRegCli));
        
        try {
            vworldreg.initVWorldUnsafe((PumaContextMediator) dependencyMap.get(DEPKEY_Mediator));
        } catch (Throwable t) {
            getLogger().warn("%%%%%%%%%%%%%%%%%%%%%%% Error with VWorldMapper init %%%%%%%%%%%%%%%%%%%%%%%");
        }
        PumaContextCommandBox pCCB=new PumaContextCommandBox(vworldreg);
        pCCB.setAppContext((PumaAppContext)dependencyMap.get(DEPKEY_AppContext));
        CommandEvent ce=(CommandEvent)dependencyMap.get(DEPKEY_CommandEvent);
        ce.setUpdater((Updater)pCCB);
        vworldreg.setContextCommandBox(pCCB);
        pCCB.reloadCommandSpace();
        //code for connecting bodies
        ArrayList<BodyHandleRecord> bodyHandleRecList = (ArrayList<BodyHandleRecord>) dependencyMap.get(DEPKEY_BodyHandleRec);

        for (BodyHandleRecord body : bodyHandleRecList) {
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
    protected static Map getBindings(Map<String, ServiceBinding> bindings, ServiceLifecycle l) {

//        Map<String, String> clProps = new HashMap<String, String>();
//        clProps.put("classLoader", "classLoader");
//        BasicDescriptor clDescriptor =
//                new BasicDescriptor(
//                ClassLoader.class.getName(),
//                clProps);
//
//        ServiceBinding clBinding = new ServiceBinding(
//                (ServiceDependency) l.getDependencySpecs().get(0),
//                clDescriptor,
//                ServiceBinding.BindingStrategy.LAZY);
//        
//        System.out.println("clBinding info: "+clBinding.toString());
//
//        bindings.put("classLoader", clBinding);

        Map<String, String> configProps = new HashMap<String, String>();
     //   configProps.put(DEPKEY_BodyHandleRec, DEPKEY_BodyHandleRec);
        BasicDescriptor configDepDescriptor =
                new BasicDescriptor(
                ArrayList.class.getName(),
                configProps);

        ServiceBinding configBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(1),
                configDepDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put(DEPKEY_BodyHandleRec, configBinding);

        Map<String, String> mediatorProps = new HashMap<String, String>();
	//		mediatorProps.put(DEPKEY_Mediator, DEPKEY_Mediator);
        BasicDescriptor mediatorDescriptor =
                new BasicDescriptor(
                PumaContextMediator.class.getName(),
                mediatorProps);

        ServiceBinding mediatorBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(0),
                mediatorDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put(DEPKEY_Mediator, mediatorBinding);


        Map<String, String> regrProps = new HashMap<String, String>();
     //    regrProps.put(DEPKEY_PumaRegCli, DEPKEY_PumaRegCli);
        BasicDescriptor regDescriptor =
                new BasicDescriptor(
                PumaRegistryClient.class.getName(),
                regrProps);

        ServiceBinding regBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(2),
                regDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put(DEPKEY_PumaRegCli, regBinding);


        Map<String, String> appContextProps = new HashMap<String, String>();
       // appContextProps.put(DEPKEY_AppContext, PumaAppContext.class.getName());
        BasicDescriptor appDescriptor =
                new BasicDescriptor(
                PumaAppContext.class.getName(),
                appContextProps);

        ServiceBinding appContextBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(4),
                appDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put(DEPKEY_AppContext, appContextBinding);

        Map<String, String> commandEventProps = new HashMap<String, String>();
        // commandEventProps.put(DEPKEY_CommandEvent, CommandEvent.class.getName());
        BasicDescriptor commandEventDescriptor =
                new BasicDescriptor(
                CommandEvent.class.getName(),
                commandEventProps);

        ServiceBinding commandEventBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(3),
                commandEventDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put(DEPKEY_CommandEvent, commandEventBinding);

        return bindings;
    }	
}
