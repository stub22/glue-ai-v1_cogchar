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
package org.cogchar.render.opengl.bony.world;

import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class CollisionMgr {
	public static CollisionResults getCameraCollisions(Camera cam, Node shootables) { 
		// 1. Reset results list.
		CollisionResults coRes = new CollisionResults();
		// 2. Aim the ray from cam loc to cam direction.
		Ray ray = new Ray(cam.getLocation(), cam.getDirection());
		// 3. Collect intersections between Ray and Shootables in results list.
		shootables.collideWith(ray, coRes);
		
		return coRes;
	}
	
	public static void printCollisionDebug(Logger slf4jLogger,  CollisionResults coRes) { 
		// 4. Print the results
		slf4jLogger.info("----- Collision count: " + coRes.size() + "-----");
		for (int i = 0; i < coRes.size(); i++) {
			// For each hit, we know distance, impact point, name of geometry.
			float dist = coRes.getCollision(i).getDistance();
			Vector3f pt = coRes.getCollision(i).getContactPoint();
			String hit = coRes.getCollision(i).getGeometry().getName();
			slf4jLogger.info("* Collision #" + i + " hit " + hit + " at " + pt + ", " + dist + " wu away.");
		}		
	}
}
