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

package org.cogchar.bind.robokind.joint;

import org.robokind.motion.JointController;
import org.robokind.motion.serial.SerialControllerConfig;
import org.robokind.utils.config.VersionProperty;
import org.robokind.utils.services.ServiceFactory;

/**
 * Robokind connector for a Cogchar virtual skeleton.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class SkeletonConnector implements ServiceFactory<JointController,SerialControllerConfig>{

    @Override
    public JointController build(SerialControllerConfig config) {
        return new SkeletonController(config);
    }

    @Override
    public VersionProperty getServiceVersion() {
        return SkeletonController.VERSION;
    }
    
    @Override
    public Class<SerialControllerConfig> getServiceConfigurationClass() {
        return SerialControllerConfig.class;
    }

    @Override
    public Class<JointController> getServiceClass() {
        return JointController.class;
    }
}
