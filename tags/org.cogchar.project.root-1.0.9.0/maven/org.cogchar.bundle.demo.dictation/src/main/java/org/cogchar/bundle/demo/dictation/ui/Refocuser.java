/*
 * Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bundle.demo.dictation.ui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Matthew Stevenson <www.cogchar.org>
 */
public class Refocuser implements FocusListener{
    private Component myFocusedComponent;
    private List<Component> myIgnoredComponents;
    private Boolean myRunFlag;

    public Refocuser(Component comp){
        myFocusedComponent = comp;
        myFocusedComponent.addFocusListener(this);
        myIgnoredComponents = new ArrayList(1);
        myRunFlag = false;
    }

    public void ignoreComponent(Component comp){
        myIgnoredComponents.add(comp);
    }

    @Override
    public void focusGained(FocusEvent e) {}

    @Override
    public void focusLost(FocusEvent e) {
        if(!myRunFlag){
            return;
        }
        if(myFocusedComponent == null){
            return;
        }
        Component comp = e.getComponent();
        Component opp = e.getOppositeComponent();
        if(myIgnoredComponents.contains(opp)){
            return;
        }
        if(myFocusedComponent == opp){
            return;
        }
        myFocusedComponent.requestFocus();
    }


    public void start(){
        myRunFlag = true;
        myFocusedComponent.requestFocus();
    }

    public void stop(){
        myRunFlag = false;
    }
}
