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
package org.cogchar.bind.rk.robot.client;

import java.util.ArrayList;
import java.util.List;
import org.robokind.api.common.property.PropertyChangeNotifier;
import org.robokind.api.common.property.PropertyChangeSource;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.Robot.Id;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */


public interface RobotSelector extends PropertyChangeSource{
    public List<Robot.Id> getAvailableIds();
    public Robot.Id getSelectedId();
    public void selectId(Robot.Id robotId);    
    
    public static class OSGiRobotSelector extends 
            PropertyChangeNotifier implements RobotSelector {
        private List<Robot.Id> myIds;
        private Robot.Id mySelectedId;
        
        public OSGiRobotSelector(){
            myIds = new ArrayList<Robot.Id>();
        }
        
        @Override
        public List<Robot.Id> getAvailableIds() {
            return myIds;
        }

        @Override
        public Robot.Id getSelectedId() {
            return mySelectedId;
        }

        @Override
        public void selectId(Robot.Id robotId) {
            if(robotId == null || myIds.contains(robotId)){
                mySelectedId = robotId;
            }
        }
        
    }
}
