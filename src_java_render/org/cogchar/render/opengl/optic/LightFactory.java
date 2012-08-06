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
package org.cogchar.render.opengl.optic;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.Iterator;
import java.util.concurrent.Callable;
import org.cogchar.api.cinema.LightConfig;
import org.cogchar.api.cinema.LightsCameraConfig;
import org.cogchar.render.app.core.CogcharRenderContext;
import org.cogchar.render.sys.core.RenderRegistryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 *
 * Kludge alert! Not sure we really want this RenderRegistryAware - need it to get root node to add lights via main
 * rendering thread, currently handled directly from here to WorkaroundFuncsMustDie. If not for the requirement to
 * enqueue the request, we'd want to do this via established methods in CogCharRenderContext. BUT we need access to the
 * app to enqueue, and CogcharRenderContext doesn't have getApp - that's up in BonyRenderContext. So for now, we'll get
 * the root node here and send it to WorkaroundFuncsMustDie along with the hrc from the calling method, from which it
 * can getApp() We could just have WorkaroundFuncsMustDie get the root node from hrc, but findJme3RootDeepNode is
 * protected.
 */
public class LightFactory extends RenderRegistryAware {

	private Node myParentNode;

	public void setParentNode(Node n) {
		myParentNode = n;
	}

	protected Node getParentNode() {
		if (myParentNode == null) {
			myParentNode = findJme3RootDeepNode(null);
		}
		return myParentNode;
	}
	static Logger theLogger = LoggerFactory.getLogger(CameraMgr.class);

	public static void addLightGrayAmbientLight(Node rootNode) {
		addAmbientLight(rootNode, ColorRGBA.LightGray);
	}

	public static void addAmbientLight(Node rootNode, ColorRGBA c) {
		AmbientLight light = new AmbientLight();
		light.setColor(c);
		rootNode.addLight(light);
	}

	public static AmbientLight makeAmbientLight(ColorRGBA c) {
		AmbientLight light = new AmbientLight();
		light.setColor(c);
		return light;
	}

	public static AmbientLight makeWhiteAmbientLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1));
		return al;
	}

	public DirectionalLight makeDirectionalLight(Vector3f direction, ColorRGBA color) {
		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(direction.normalizeLocal());
		dl.setColor(color);
		return dl;
	}

	public DirectionalLight makeWhiteOpaqueDirectionalLight(Vector3f direction) {
		//   rootNode.addLight(al);
		ColorRGBA whiteOpaqueLight = new ColorRGBA(1f, 1f, 1f, 1.0f);
		return makeDirectionalLight(direction, whiteOpaqueLight);
	}

	public void initLightsFromConfig(LightsCameraConfig config, CogcharRenderContext crc) {
		for (LightConfig lc : config.myLCs) {
			theLogger.info("Building Light for config: " + lc);
			ColorRGBA color = new ColorRGBA(lc.lightColor[0], lc.lightColor[1], lc.lightColor[2], lc.lightColor[3]);
			if (lc.lightType.equals(LightConfig.LightType.DIRECTIONAL)) {
				Vector3f direction = new Vector3f(lc.lightDirection[0], lc.lightDirection[1], lc.lightDirection[2]);
				addLightOnMainThread(makeDirectionalLight(direction, color), crc);
			}
			if (lc.lightType.equals(LightConfig.LightType.AMBIENT)) {
				addLightOnMainThread(makeAmbientLight(color), crc);
			}
		}
	}

	// Needed to ensure light is added on main rendering thread
	public void addLightOnMainThread(final Light l, final CogcharRenderContext crc) {
		crc.enqueueCallable(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				getParentNode().addLight(l);
				return null;
			}
		});

	}
	
	public void removeLightOnMainThread(final Light l, final CogcharRenderContext crc) {
		crc.enqueueCallable(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				getParentNode().removeLight(l);
				return null;
			}
		});

	}
	
	public void clearLights(CogcharRenderContext crc) {
		Iterator lightIterator = getParentNode().getWorldLightList().iterator();
		theLogger.info("Clearing Lights...");
		while (lightIterator.hasNext()) {
			Light nextLight = (Light)lightIterator.next();
			theLogger.info("Removing light of type: " + nextLight.getType().name());
			removeLightOnMainThread(nextLight, crc);
		}
	}
}
