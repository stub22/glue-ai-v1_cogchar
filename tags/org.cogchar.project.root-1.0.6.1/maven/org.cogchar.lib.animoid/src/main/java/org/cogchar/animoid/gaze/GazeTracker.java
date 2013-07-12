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
package org.cogchar.animoid.gaze;

import java.util.List;
import org.cogchar.animoid.gaze.IGazeTarget;
import org.cogchar.sight.obs.ObservationTrackerRegistry;
import org.cogchar.api.sight.SightObservation;

/**
 *
 * @author Matthew Stevenson
 */
public class GazeTracker implements ObservationTrackerRegistry<SightObservation, IGazeTarget> {
    
    @Override
    public IGazeTarget addObservation(SightObservation obs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<IGazeTarget> getObservationTrackers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}