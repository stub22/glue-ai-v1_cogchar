
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

public class BodyConfigSpec
    {
        private RepoClient rc;
        private Ident graphIdentForBony;
        private FigureConfig humCfg;
        private ModelRobot modelRobot;
        
        public BodyConfigSpec(RepoClient repo, Ident ident, FigureConfig config)
        {
            rc=repo;
            graphIdentForBony=ident;
            humCfg=config;
        }
        
        public RepoClient getRepoClient()
        {
            return rc;
        }
        
        public Ident getGraphIdentForBony()
        {
            return graphIdentForBony;
        }
        
        public ModelRobot getModelRobot()
        {
            return modelRobot;
        }
        public void setModelRobot(ModelRobot mr)
        {
            modelRobot=mr;
        }
        public FigureConfig getHumCfg()
        {
            return humCfg;
        }
    }