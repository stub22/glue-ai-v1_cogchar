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

package org.cogchar.api.cinema;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// Being built to help in factoring out common features of PataMgr/SpatialAnimMgr and their configs
public class SpatialActionSetConfig {
	
	public List<SpatialActionConfig> mySACs = new ArrayList<SpatialActionConfig>();
	
}
