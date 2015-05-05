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
package org.cogchar.svc.behav.control;

import java.awt.event.ActionListener;
import java.util.Set;
import org.jflux.api.core.Listener;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */


public interface ActionCallbackMap {
    public void putActionCallback(String actionCallbackName, ActionListener listener);
	
    public ActionListener getActionCallback(String actionCallbackName);	
    
    public void removeActionCallback(String actionCallbackName);
    
    public void addActionListener(Listener<String> listener);
    
    public void removeActionListener(Listener<String> listener);
    
    public void addActionRemoveListener(Listener<String> listener);
    
    public void removeActionRemoveListener(Listener<String> listener);
    
    public Set<String> getActionKeys();
}
