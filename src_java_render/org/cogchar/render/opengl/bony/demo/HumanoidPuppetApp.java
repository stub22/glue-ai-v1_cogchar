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
 * 
 * ------------------------------------------------------------------------------
 *
 *		This file contains code copied from the JMonkeyEngine project.
 *		You may not use this file except in compliance with the
 *		JMonkeyEngine license.  See full notice at bottom of this file. 
 */

package org.cogchar.render.opengl.bony.demo;

import org.cogchar.render.opengl.bony.model.HumanoidFigure;



import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class HumanoidPuppetApp extends BonyVirtualCharApp<HumanoidRenderContext> {
    private final static Logger		theLogger = LoggerFactory.getLogger(HumanoidPuppetApp.class);

	public static void main(String[] args) {
		BonyConfigEmitter bce = new BonyConfigEmitter();
		HumanoidPuppetApp app = new HumanoidPuppetApp(bce);
		app.start();
	}
	public HumanoidPuppetApp(BonyConfigEmitter bce) { 
		super(bce); 		
	}
	@Override protected HumanoidRenderContext makeCogcharRenderContext() {
		BonyConfigEmitter bce = getBonyConfigEmitter();
		HumanoidRenderContext hrc = new HumanoidRenderContext(bce);
		hrc.setApp(this);
		return hrc;
	}
	

	/*
	 * Called from SimpleUpdate to propagate position state from the figure state in the
	 * BonyRenderContext to the OpenGL-rendered bones of the humanoid puppet, as mediated
	 * by the myTwister delegate (which doesn't do anything smart, yet)..  
	 */
	/*
	public void applyFigureState() {
		BonyRenderContext ctx = getBonyRenderContext();
		FigureState fs = ctx.getFigureState();
		myHumanoidWrapper.applyFigureState(fs);
	}
	 * 
	 */


    private long myLastUpdateTime = System.currentTimeMillis();
    private void logUpdateTime(){
        long prev = myLastUpdateTime;
        long now = System.currentTimeMillis();
        long elapsed = now - prev;
        theLogger.info("Updating Robot.  " + elapsed + "msec since last update.  Cur time: " + now);
        myLastUpdateTime = now;
    }
    /*
	@Override public void simpleUpdate(float tpf) {
        //logUpdateTime();
		super.simpleUpdate(tpf);
		//applyTwisting(tpf);
		applyFigureState();
	}
	 * 
	 */

}
		/*VirtCharPanel vcp = getVCPanel();
		int testChannelNum = vcp.getTestChannelNum();
		String direction = vcp.getTestDirection();
		HumanoidBoneConfig hbc = myHumanoidWrapper.getHBConfig();
		List<HumanoidBoneDesc> boneDescs = hbc.getBoneDescs();
		HumanoidBoneDesc hbd = boneDescs.get(testChannelNum);
		String boneName = hbd.getSpatialName();
		Bone tgtBone = myHumanoidWrapper.getSpatialBone(boneName);
		myTwister.twistBone(tpf, tgtBone, direction);
		
		Bone rootBone = myHumanoidWrapper.getRootBone();*/
			/*    Weird old bits of code
	float elTime = 0;
	boolean forward = true;
	AnimControl animControl;
	
	Vector3f direction = new Vector3f(0, 0, 1);
	Quaternion rotate = new Quaternion().fromAngleAxis(FastMath.PI / 8, Vector3f.UNIT_Y);
	boolean dance = true;
 * / myHumanoidWrapper.wiggle(tpf);
	
	  * JMonkey Team Comment as of about August 2011:
 * PHYSICS RAGDOLLS ARE NOT WORKING PROPERLY YET!
		//  Below is JMonkey test code from TestBoneRagdoll, which is commented out in JMonkey trunk as of about 
		// 2011-08-01.
		// System.out.println(((BoundingBox) myHumanoidModel.getWorldBound()).getYExtent());
//        elTime += tpf;
//        if (elTime > 3) {
//            elTime = 0;
//            if (dance) {
//                rotate.multLocal(direction);
//            }
//            if (Math.random() > 0.80) {
//                dance = true;
//                myHumanoidAnimChannel.setAnim("Dance");
//            } else {
//                dance = false;
//                myHumanoidAnimChannel.setAnim("RunBase");
//                rotate.fromAngleAxis(FastMath.QUARTER_PI * ((float) Math.random() - 0.5f), Vector3f.UNIT_Y);
//                rotate.multLocal(direction);
//            }
//        }
//        if (!myHumanoidKRC.hasControl() && !dance) {
//            if (myHumanoidModel.getLocalTranslation().getZ() < -10) {
//                direction.z = 1;
//                direction.normalizeLocal();
//            } else if (myHumanoidModel.getLocalTranslation().getZ() > 10) {
//                direction.z = -1;
//                direction.normalizeLocal();
//            }
//            if (myHumanoidModel.getLocalTranslation().getX() < -10) {
//                direction.x = 1;
//                direction.normalizeLocal();
//            } else if (myHumanoidModel.getLocalTranslation().getX() > 10) {
//                direction.x = -1;
//                direction.normalizeLocal();
//            }
//            myHumanoidModel.move(direction.multLocal(tpf * 8));
//            direction.normalizeLocal();
//            myHumanoidModel.lookAt(myHumanoidModel.getLocalTranslation().add(direction), Vector3f.UNIT_Y);
// 
*/ 

/*
 * 
 * Contains code copied and modified from the JMonkeyEngine.com project,
 * under the following terms:
 * 
 * -----------------------------------------------------------------------
 * 
 * Copyright (c) 2009-2010 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
