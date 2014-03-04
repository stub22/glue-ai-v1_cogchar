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
package org.cogchar.app.puma.web;

import java.util.Properties;
import java.util.concurrent.Future;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.boot.PumaContextCommandBox;

import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.app.puma.event.CommandEvent;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.osgi.framework.BundleContext;
//import org.cogchar.render.goody.basic.DataballGoodyBuilder;
//import org.cogchar.render.scene.goody.PathMgr;
//import org.cogchar.render.scene.goody.SpatialAnimMgr;
///**

/**
 * @author Stu B. <www.texpedient.com>
 */
class CommandTargetForUseFromWeb extends BasicDebugger implements LiftAmbassador.LiftAppInterface {

    private PumaContextCommandBox myPCCB;
    private final PumaWebMapper myWebMapper;
    private CommandEvent ce;

    @Override
    public String queryCogbot(String query, String url) {
        // See CogbotWebClientWrapper for old impl, now disabled to prevent dragging HTTP-client deps with PUMA.
        return null;
    }

    public CommandTargetForUseFromWeb(BundleContext bundleCtx, final PumaWebMapper outer) {
        this.myWebMapper = outer;
        
        ce = new CommandEvent();
        
         ServiceLifecycleProvider<CommandEvent> lifecycle =
                new SimpleLifecycle<CommandEvent>(ce, CommandEvent.class.getName());
        Properties props = new Properties();
        props.put("commandEventDep", CommandEvent.class.getName());

        ManagedService<CommandEvent> ms = new OSGiComponent<CommandEvent>(bundleCtx, lifecycle, props);
        ms.start();
    }

    @Override
    public boolean triggerAnimation(Ident uri) {
//		getLogger().info("Got request to trigger animation {}", uri);
//		boolean success = myPCCB.getPathMgr().controlAnimationByName(uri, PathMgr.ControlAction.PLAY);
//		if (!success) {
//			success = myPCCB.getThingAnimMgr().controlAnimationByName(uri, SpatialAnimMgr.ControlAction.PLAY);
//		}
        boolean result = ce.update(CommandEvent.EventType.START_ANIMATION, uri, null, null, false);
        return result; //success;
    }

    @Override
    public boolean stopAnimation(Ident uri) {
//		getLogger().info("Got request to stop animation {}", uri);
//		boolean success = myPCCB.getPathMgr().controlAnimationByName(uri, PathMgr.ControlAction.STOP);
//		if (!success) {
//			success = myPCCB.getThingAnimMgr().controlAnimationByName(uri, SpatialAnimMgr.ControlAction.STOP);
//		}
        boolean result = ce.update(CommandEvent.EventType.STOP_ANIMATION, uri, null, null, false);
        return result; //success;
    }

    @Override
    public boolean performDataballAction(String action, String text) {
        
        return ce.update(CommandEvent.EventType.DATABALL_GOODY, null, action, text, false);
        //return true; //DataballGoodyBuilder.getTheBallBuilder().performAction(action, text);
    }

    @Override
    public boolean performUpdate(String request) {
        boolean forceFreshDefaultRepo = false;
        boolean success = false;
        if (ce.checkUpdater()) {
            ce.update(CommandEvent.EventType.UPDATE, null, request, request, forceFreshDefaultRepo);
            //Future<Boolean> resultFuture = myPCCB.processUpdateRequestAsync(request, forceFreshDefaultRepo);
            
            //Future<Boolean> resultFuture =null;
        } else {
            getLogger().warn("Update requested, but PumaWebMapper cannot find PumaAppContext for RQ=" + request);
        }
        return success;
    }
}
