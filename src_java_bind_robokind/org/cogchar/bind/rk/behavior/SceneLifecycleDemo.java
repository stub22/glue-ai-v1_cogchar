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

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.cogchar.bind.rk.behavior.ChannelBindingConfig.ChannelType;
import org.jflux.api.core.Listener;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.osgi.OSGiUtils;
import org.robokind.api.speech.SpeechEvent;
import org.robokind.api.speech.SpeechRequest;
import org.robokind.api.speech.SpeechService;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class SceneLifecycleDemo {
    public static void test(BundleContext context){
        final ServiceChannelExtender sce = new ServiceChannelExtender(context, null, null);
        Runnable sceRun = new Runnable() {
            @Override public void run() {
                sce.start();
            }
        };
        final SceneSpecExtender sse = new SceneSpecExtender(context, null, null);
        Runnable sseRun = new Runnable() {
            @Override public void run() {
                sse.start();
            }
        };
        
        final OSGiTheater theater = new OSGiTheater(context, null, null);
        Runnable theaterRun = new Runnable() {
            @Override public void run() {
                theater.start();
            }
        };
        
        ChannelBindingConfig bindingConfig = new ChannelBindingConfig();
        bindingConfig.myChannelType = ChannelType.SPEECH;
        bindingConfig.myChannelURI = "fakeURI";
        bindingConfig.myOSGiFilterString = OSGiUtils.createFilter(SpeechService.PROP_ID, "testService");
        
        SpeechService speechService = getSpeechService();
        
//        SceneSpec spec = new SceneSpec();
        
        List<Runnable> runnables = new ArrayList<Runnable>();
        runnables.add(sseRun);
        runnables.add(sceRun);
        runnables.add(getRegistrationRunnable(context, SpeechService.class, speechService, SpeechService.PROP_ID, "testService"));
        runnables.add(getRegistrationRunnable(context, ChannelBindingConfig.class, bindingConfig, null, null));
        runnables.add(theaterRun);
        
        Collections.shuffle(runnables);
        
        for(Runnable run : runnables){
            run.run();
        }
    }
    
    private static Runnable getRegistrationRunnable(
            final BundleContext context, final Class clazz, final Object obj, final String key, final String val){
        return new Runnable() {
            @Override public void run() {
                Properties props = null;
                if(key != null){
                    props = new Properties();
                    props.put(key, val);
                }
                context.registerService(clazz.getName(), obj, props);
            }
        };
    }
    
    private static SpeechService getSpeechService(){
        return new SpeechService() {
            @Override public String getSpeechServiceId() {
                return "";
            }

            @Override public void start() throws Exception { }
            @Override public void speak(String string) {
                System.out.println(string);
            }

            @Override public void cancelSpeech() {}
            @Override public void stop() {}
            @Override public void addRequestListener(Listener<SpeechRequest> ll) {}
            @Override public void removeRequestListener(Listener<SpeechRequest> ll) {}
            @Override public void addSpeechEventListener(Listener<SpeechEvent> ll) {}
            @Override public void removeSpeechEventListener(Listener<SpeechEvent> ll) {}
        };
    }
}
