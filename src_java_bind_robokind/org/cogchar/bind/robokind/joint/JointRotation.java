/*
 * Copyright 2011 Hanson Robokind LLC.
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
package org.cogchar.bind.robokind.joint;

import org.robokind.api.common.position.NormalizableRange;
import org.robokind.api.common.position.NormalizedDouble;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */

public class JointRotation {
    final static int PITCH = 0;
    final static int ROLL = 1;
    final static int YAW = 2;
    final static int ROTATION_COUNT = 3;
    private double[] myRotationValues;

    public JointRotation(double pitch, double roll, double yaw){
        myRotationValues = new double[ROTATION_COUNT];
        myRotationValues[PITCH] = pitch;
        myRotationValues[ROLL] = roll;
        myRotationValues[YAW] = yaw;
    }

    public JointRotation(JointRotation rot){
        myRotationValues = new double[ROTATION_COUNT];
        myRotationValues[PITCH] = rot.getPitch();
        myRotationValues[ROLL] = rot.getRoll();
        myRotationValues[YAW] = rot.getYaw();
    }

    public double getRotation(int axis){
        if(axis < 0 || axis >= ROTATION_COUNT){
            throw new IllegalArgumentException(
                    "Axis (" + axis + ") is out of range.");
        }
        return myRotationValues[axis];
    }

    public double getPitch(){
        return myRotationValues[PITCH];
    }

    public double getRoll(){
        return myRotationValues[ROLL];
    }

    public double getYaw(){
        return myRotationValues[YAW];
    }

    public static JointRotation add(JointRotation a, JointRotation b){
        if(a == null && b == null){
            return null;
        }else if(a == null){
            return new JointRotation(b);
        }else if(b == null){
            return new JointRotation(a);
        }
        return new JointRotation(
                a.getPitch()+b.getPitch(), 
                a.getRoll()+b.getRoll(),
                a.getYaw()+b.getYaw());
    }

    
    public static class JointRotationRange implements NormalizableRange<JointRotation>{
        
        private double[] myMinRotations;
        private double[] myMaxRotations;
        
        public JointRotationRange(double minPitch, double maxPitch, double minRoll, double maxRoll, double minYaw, double maxYaw){
            myMinRotations = new double[ROTATION_COUNT];
            myMaxRotations = new double[ROTATION_COUNT];
            myMinRotations[PITCH] = minPitch;
            myMaxRotations[PITCH] = maxPitch;
            myMinRotations[ROLL] = minRoll;
            myMaxRotations[ROLL] = maxRoll;
            myMinRotations[YAW] = minYaw;
            myMaxRotations[YAW] = maxYaw;
        }
        
        @Override
        public boolean isValid(JointRotation t) {
            if(t == null){
                throw new NullPointerException();
            }
            for(int i=0; i<ROTATION_COUNT; i++){
                double val = t.getRotation(i);
                if(val < myMaxRotations[i] || val > myMaxRotations[i]){
                    return false;
                }
            }
            return true;
        }

        @Override
        public NormalizedDouble normalizeValue(JointRotation t) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public JointRotation denormalizeValue(NormalizedDouble v) {
            double[] vals = new double[ROTATION_COUNT];
            for(int i=0; i<ROTATION_COUNT; i++){
                double range = myMaxRotations[i] - myMinRotations[i];
                vals[i] = range*v.getValue() + myMinRotations[i];
            }
            
            return new JointRotation(vals[PITCH], vals[ROLL], vals[YAW]);
        }
    }
}
