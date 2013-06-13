/*
 * Copyright 2013 The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bind.rk.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.cogchar.impl.scene.SceneSpec;
import org.cogchar.bind.rk.behavior.ChannelBindingConfig.ChannelType;
import org.jflux.api.core.Listener;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.lifecycle.utils.SimpleLifecycle;
import org.robokind.api.common.osgi.OSGiUtils;
import org.robokind.api.common.osgi.lifecycle.OSGiComponent;
import org.robokind.api.speech.SpeechEvent;
import org.robokind.api.speech.SpeechEventList;
import org.robokind.api.speech.SpeechJob;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speech.SpeechService;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class SceneLifecycleDemo {
    public static void test(BundleContext context){
        Properties sceProps = new Properties();
        sceProps.put("ServiceChannelExtenderId", "svc_chan_ext_19");
        final ServiceChannelExtender sce = new ServiceChannelExtender(context, OSGiUtils.createFilter("ChannelBindingGroupId", "chan_bind_grp_19"), sceProps);
        Runnable sceRun = new Runnable() {
            @Override public void run() {
                sce.start();
            }
        };
        Properties sseProps = new Properties();
        sseProps.put("SceneSpecExtenderId", "scene_spec_ext_19");
        final SceneSpecExtender sse = new SceneSpecExtender(context, OSGiUtils.createFilter("SceneSpecGroupId", "scene_spec_grp_19"), sseProps);
        Runnable sseRun = new Runnable() {
            @Override public void run() {
                sse.start();
            }
        };
        
		/*
        final OSGiTheater theater = new OSGiTheater(context, null, null);
        Runnable theaterRun = new Runnable() {
            @Override public void run() {
                theater.start();
            }
        };
		* 
		*/
        
        ChannelBindingConfig bindingConfig = new ChannelBindingConfig();
		
        String speechTestFilterString = OSGiUtils.createFilter(SpeechService.PROP_ID, "testService");
        bindingConfig.initExplicitly(ChannelType.SPEECH_BLOCK_OUT, "fakeURI", speechTestFilterString);
				
        SpeechService speechService = getSpeechService();
        
        SceneSpec spec = new SceneSpec();
        
        //Showing that the start up order does not matter by shuffling the order
        List<Runnable> runnables = new ArrayList<Runnable>();
        runnables.add(sseRun);
        runnables.add(sceRun);
        runnables.add(getRegistrationRunnable(context, SpeechService.class, speechService, SpeechService.PROP_ID, "testService"));
        runnables.add(getRegistrationRunnable(context, ChannelBindingConfig.class, bindingConfig, "ChannelBindingGroupId", "chan_bind_grp_19"));
        runnables.add(getRegistrationRunnable(context, SceneSpec.class, spec, "SceneSpecGroupId", "scene_spec_grp_19"));
       // runnables.add(theaterRun);
        Collections.shuffle(runnables);
        
        for(Runnable run : runnables){
            run.run();
        }
    }
    public static Runnable makeChanBindConfRegRunnable(BundleContext context, ChannelBindingConfig cbc, 
				final String key, final String val) {
		return getRegistrationRunnable(context, ChannelBindingConfig.class, cbc, key, val);
	}
    public static Runnable makeSceneSpecRegRunnable(BundleContext context, SceneSpec scnSpec, 
				final String key, final String val) {
		return getRegistrationRunnable(context, SceneSpec.class, scnSpec, key, val);
	}

	
	
    public static Runnable getRegistrationRunnable(
            final BundleContext context, final Class clazz, final Object obj, final String key, final String val){
        return new Runnable() {
            @Override public void run() {
                Properties props = null;
                if(key != null){
                    props = new Properties();
                    props.put(key, val);
                }
                new OSGiComponent(context, new SimpleLifecycle(obj, clazz, props)).start();
            }
        };
    }
    
    private static SpeechService getSpeechService(){
        return new SpeechService() {
            String name;
            @Override public String getSpeechServiceId() {
                return "";
            }

            @Override public void start() throws Exception { }
            @Override public SpeechJob speak(String string) {
                System.out.println(string);
                return null;
            }

            @Override public void cancelSpeech() {}
            @Override public void stop() {}
            @Override public void addRequestListener(Listener<SpeechRequest> ll) {}
            @Override public void removeRequestListener(Listener<SpeechRequest> ll) {}
            @Override public void addSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> ll) {}
            @Override public void removeSpeechEventListener(Listener<SpeechEventList<SpeechEvent>> ll) {}
        };
    }
}
