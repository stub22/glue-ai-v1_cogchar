package org.cogchar.bind.cogbot.osgi;

import org.cogchar.bind.cogbot.main.CogbotSpeechDemo;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
		System.out.println("******************************************************************************");
		System.out.println("Cogbot binding Activator says Hi!");
		System.out.println("******************************************************************************");
        CogbotSpeechDemo.main(new String[]{"192.168.0.100"});
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

}
