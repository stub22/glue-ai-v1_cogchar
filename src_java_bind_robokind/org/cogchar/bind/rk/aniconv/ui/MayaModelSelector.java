/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.bind.rk.aniconv.ui;

import java.util.ArrayList;
import java.util.List;
import org.cogchar.bind.rk.aniconv.MayaModelMap;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.osgi.ServiceClassListener;

/**
 *
 * @author Ryan Biggs
 */
public class MayaModelSelector extends ServiceClassListener<MayaModelMap>{
    private List<MayaModelMap> myConfigs;
    private Notifier<MayaModelMap> myAddNotifier;
    private Notifier<MayaModelMap> myRemoveNotifier;
    
    public MayaModelSelector(BundleContext context){
        super(MayaModelMap.class, context, null);
        myConfigs = new ArrayList<MayaModelMap>();
        myAddNotifier = new DefaultNotifier<MayaModelMap>();
        myRemoveNotifier = new DefaultNotifier<MayaModelMap>();
    }

    @Override
    protected void addService(MayaModelMap t) {
        myConfigs.add(t);
        myAddNotifier.notifyListeners(t);
    }

    @Override
    protected void removeService(MayaModelMap t) {
        myConfigs.remove(t);
        myRemoveNotifier.notifyListeners(t);
    }
	
	/*
	@Override
    public void serviceChanged(ServiceEvent se) {
		super.serviceChanged(se);
        ServiceReference ref = se.getServiceReference();
		System.out.println("Wanting to change service..."); // TEST ONLY
		//BoneRobotConfig t = (BoneRobotConfig)myContext.getService(ref);

    }
	*/
    
    public List<MayaModelMap> getAvailableConfigs(){
        return myConfigs;
    }
    
    public Notifier<MayaModelMap> getAddNotifier(){
        return myAddNotifier;
    }
    
    public Notifier<MayaModelMap> getRemoveNotifier(){
        return myRemoveNotifier;
    }
}
