
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
package org.cogchar.app.puma.config;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.bind.mio.robot.model.ModelRobot;


/*
 * Registration of this entity triggers the final binding of virtual robot to onscreen avatar.
 * Changed name to BodyHandleRecord - this should not be a "Spec" since it contains runtime handles
 * (ModelRobot and RepoClient), which are not serializable to RDF.   
 */
public class BodyHandleRecord  {
        private RepoClient		myRepoClient;
        private Ident			myBoneSrcGraphID;
        private FigureConfig	myHumFigConf;
        private ModelRobot		myModelRobot;
        
        public BodyHandleRecord(RepoClient repoCli, Ident boneSrcGraphID, FigureConfig humFigConf)   {
            myRepoClient		= repoCli;
            myBoneSrcGraphID	= boneSrcGraphID;
            myHumFigConf		= humFigConf;
        }
        
        public RepoClient getRepoClient()  {
            return myRepoClient;
        }
        
        public Ident getBoneSrcGraphID()  {
            return myBoneSrcGraphID;
        }
        public ModelRobot getModelRobot()  {
            return myModelRobot;
        }
        public void setModelRobot(ModelRobot mr) {
            myModelRobot = mr;
        }
        public FigureConfig getHumaFigureConfig() {
            return myHumFigConf;
        }
    }