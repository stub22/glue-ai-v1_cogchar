/*
 * Copyright 2011 The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.robot.model;

import org.cogchar.avrogen.bind.robokind.RotationAxis;
import org.robokind.api.common.position.NormalizedDouble;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ModelBoneRotRange {
    private String myBoneName;
    private RotationAxis myRotationAxis;
    private double myMinRotation;
    private double myMaxRotation;

    public ModelBoneRotRange(String boneName, RotationAxis axis, double min, double max){
        if(boneName == null || axis == null){
            throw new NullPointerException();
        }
        myBoneName = boneName;
        myRotationAxis = axis;
        myMinRotation = min;
        myMaxRotation = max;
    }
    
    public String getBoneName(){
        return myBoneName;
    }

    public RotationAxis getRotationAxis(){
        return myRotationAxis;
    }

    public ModelBoneRotation makeRotationForNormalizedFraction(NormalizedDouble normVal){
        double range = myMaxRotation - myMinRotation;
        double boneAngle = range*normVal.getValue() + myMinRotation;
        return new ModelBoneRotation(myBoneName, myRotationAxis, boneAngle);
    }
}
