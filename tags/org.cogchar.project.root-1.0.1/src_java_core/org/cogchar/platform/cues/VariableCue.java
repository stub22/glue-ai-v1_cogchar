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

package org.cogchar.platform.cues;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * The value is mutable, and will trigger property-change events on a change.
 */
public class VariableCue extends NamedCue {
	private String	myValue;
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public VariableCue(String name, String value) {
		super(name);
		myValue = value;
	}
	/**
	 * 
	 * @return
	 */
	public String getValue() {
		return myValue;
	}	
	/**
	 * 
	 * @param v
	 */
	public void setValue(String v) {
		String oldValue = myValue;
		myValue = v;
		if (((oldValue != null) && !oldValue.equals(myValue)) || ((oldValue == null) && (myValue != null))) {
			safelyFirePropertyChange("value", oldValue, myValue);
			this.markUpdatedNow();
			// VariableCues should tell JMX clients when they change
			broadcastUpdateNotice();
		}
	}
	/**
	 * 
	 * @return
	 */
	@Override  public String getContentSummaryString() {
		return "name=" + getName() + ", value=" + getValue();
	}
}
