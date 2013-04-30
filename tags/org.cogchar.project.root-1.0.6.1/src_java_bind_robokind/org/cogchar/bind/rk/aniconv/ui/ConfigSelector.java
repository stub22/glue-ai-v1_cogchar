/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.rk.aniconv.ui;

import java.util.ArrayList;
import java.util.List;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.osgi.ServiceClassListener;

/**
 *
 * @author Matthew Stevenson
 */
public class ConfigSelector<T> extends ServiceClassListener<T>{
    private List<T> myConfigs;
    private Notifier<T> myAddNotifier;
    private Notifier<T> myRemoveNotifier;
	private Class<T> myType;
    
	// The obnoxious type parameter in the constructor is required for the super(type, context, null) call below due to
	// generic type erasure
    public ConfigSelector(BundleContext context, Class<T> type){
        super(type, context, null);
		myType = type;
        myConfigs = new ArrayList<T>();
        myAddNotifier = new DefaultNotifier<T>();
        myRemoveNotifier = new DefaultNotifier<T>();
    }
	
	public Class<T> getType() {
		return myType;
	}

    @Override
    protected void addService(T t) {
        myConfigs.add(t);
        myAddNotifier.notifyListeners(t);
    }

    @Override
    protected void removeService(T t) {
        myConfigs.remove(t);
        myRemoveNotifier.notifyListeners(t);
    }
    
    public List<T> getAvailableConfigs(){
        return myConfigs;
    }
    
    public Notifier<T> getAddNotifier(){
        return myAddNotifier;
    }
    
    public Notifier<T> getRemoveNotifier(){
        return myRemoveNotifier;
    }
}
