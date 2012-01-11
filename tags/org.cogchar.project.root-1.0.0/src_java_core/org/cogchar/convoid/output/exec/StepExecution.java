/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.convoid.output.exec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cogchar.convoid.output.config.Step;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public abstract class StepExecution {
	

	public enum Status {CREATED, STARTED, FINISHED, STOPPED};

	// Add Unique ID?
	// Track history?
	// Pause and resume?
	
	private Step						myStep;
	private Status						myStatus;
	private List<StepProgressListener>	myListeners;
	private Map<String, String>			myConfigMap;

    private static Map<String, String> HACKED_REPLACEMENTS;
	private static StepExecutionFactory theDefaultStepExecutionFactory;
	private static Map<String, StepExecutionFactory> theFactoryMap = new HashMap<String, StepExecutionFactory>();

	public abstract void connect();
	public abstract void start();
	public abstract void stop();
	public abstract void disconnect();
	
	public StepExecution(Step s, Map<String, String> configMap) {
		myListeners = new ArrayList<StepProgressListener>();
		myStep = s;
		myStatus = Status.CREATED;
		myConfigMap = configMap;
	}
	public Step getStep() {
		return myStep;
	}
	public Status 	checkStatus() {
		return myStatus;
	}
	protected void setStatus(Status s) {
		myStatus = s;
		notifyStepListeners();
	}
	protected Map<String, String> getConfigMap() {
		return this.myConfigMap;
	}
	private static Pattern varSubstPattern = Pattern.compile("\\$\\{(\\w+)\\}");
	public String substituteConfigVariables(String inputText) {
		Matcher m = varSubstPattern.matcher(inputText);
	    m.reset();
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String varName = m.group(1);
			System.out.println("Found variable to replace: " + varName);
			String varVal = null;
			if (myConfigMap != null) {
				varVal = myConfigMap.get(varName);
			}
			if (varVal == null) {
				varVal = "";
			}
			m.appendReplacement(sb, varVal);   // copies everything before the match, and replaces it
		}
	    m.appendTail(sb);     // copy remainder
        String ret = THIS_IS_A_HACK_replacePronunciation(sb.toString());
		return ret;
	}

    private String THIS_IS_A_HACK_replacePronunciation(String str){
        for(Entry<String, String> e : GET_HACKED_REPLACEMENTS().entrySet()){
            str = str.replace(e.getKey(), e.getValue());
        }
        return str;
    }

    private static Map<String,String> GET_HACKED_REPLACEMENTS(){
        if(HACKED_REPLACEMENTS == null){
            HACKED_REPLACEMENTS = new HashMap<String, String>();
            HACKED_REPLACEMENTS.put("Rothblatt", "Rothpblatt");
            HACKED_REPLACEMENTS.put("Terasem", "Terassem");
        }
        return HACKED_REPLACEMENTS;
    }
	protected void notifyStepListeners() {
		for (StepProgressListener spl : myListeners) {
			spl.handleStepProgress(this);
		}
	}
	public void 	registerProgressListener(StepProgressListener spl) {
		myListeners.add(spl);
	}
	public void 	unregisterProgressListener(StepProgressListener spl) {
		myListeners.remove(spl);
	}
    @Override
	public String toString() {
		return "StepExecution-" + hashCode() + "-[" + myStatus + ", " + myStep + "]";
	}
	public static StepExecutionFactory getFactory(String factoryClassName) throws Throwable {
		StepExecutionFactory factory = theFactoryMap.get(factoryClassName);
		if (factory == null) {
			Object	factoryObj = Class.forName(factoryClassName).newInstance();
			factory = (StepExecutionFactory) factoryObj;
			registerFactory(factoryClassName, factory, false);
		}
		return factory;
	}
	public static void registerFactory(String factoryClassName, 
					StepExecutionFactory factory, boolean isDefault) {
		theFactoryMap.put(factoryClassName, factory);
		if (isDefault) {
			theDefaultStepExecutionFactory = factory;
		}
	}
	public static StepExecution makeStepExecution(Step s, Map<String, String> configMap, String factoryClassName) {
		try {
			StepExecutionFactory factory = null;
			if (factoryClassName == null) {
				if (theDefaultStepExecutionFactory == null) {
					throw new RuntimeException("No default StepExexcutionFactory is available");
				} else {
					factory = theDefaultStepExecutionFactory;
				}
			} else {
				factory = getFactory(factoryClassName);
			}
			StepExecution stepExec = factory.makeStepExecution(s, configMap);
			return stepExec;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	public static StepExecutionFactory getDefaultFactory() {
		return theDefaultStepExecutionFactory;
	}
}
