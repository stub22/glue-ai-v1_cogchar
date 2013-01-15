/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.rk.aniconv.ui;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.osgi.ServiceClassListener;

/**
 *
 * @author Matthew Stevenson
 */
public class ConfigSelector extends ServiceClassListener<BoneRobotConfig>{
    private List<BoneRobotConfig> myConfigs;
    private Notifier<BoneRobotConfig> myAddNotifier;
    private Notifier<BoneRobotConfig> myRemoveNotifier;
    
    public ConfigSelector(BundleContext context){
        super(BoneRobotConfig.class, context, null);
        myConfigs = new ArrayList<BoneRobotConfig>();
        myAddNotifier = new DefaultNotifier<BoneRobotConfig>();
        myRemoveNotifier = new DefaultNotifier<BoneRobotConfig>();
    }

    @Override
    protected void addService(BoneRobotConfig t) {
        myConfigs.add(t);
        myAddNotifier.notifyListeners(t);
    }

    @Override
    protected void removeService(BoneRobotConfig t) {
        myConfigs.remove(t);
        myRemoveNotifier.notifyListeners(t);
    }
    
    public List<BoneRobotConfig> getAvailableConfigs(){
        return myConfigs;
    }
    
    public Notifier<BoneRobotConfig> getAddNotifier(){
        return myAddNotifier;
    }
    
    public Notifier<BoneRobotConfig> getRemoveNotifier(){
        return myRemoveNotifier;
    }
}
