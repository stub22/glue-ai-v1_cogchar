/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.demo.render.trial;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import org.cogchar.demo.render.opengl.DemoYouPickStuff;
import org.cogchar.demo.render.opengl.UnfinishedDemoApp;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.sys.context.CoreFeatureAdapter;


/**
 * @author Stu B. <www.texpedient.com>
 */

public class TrialBalloon extends UnfinishedDemoApp {

	public static void main(String[] args) {
		TrialBalloon app = new TrialBalloon();
		app.start();
	}

	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		TB_RenderContext rc = new TB_RenderContext();
		return rc;
	}

	@Override public void simpleInitApp() {
		super.simpleInitApp();
		flyCam.setMoveSpeed(20);
	}
	public class TB_RenderContext extends ConfiguredPhysicalModularRenderContext {

		Node myMainNode;

		@Override public void completeInit() {
			myMainNode = new Node("my_main");
			rootNode.attachChild(myMainNode);
			BonyGameFeatureAdapter.initCrossHairs(settings, getRenderRegistryClient()); // a "+" in the middle of the screen to help aiming
			setupLight();	
			// shedMoreLight();
			BitmapText btSpatial = makeTextSpatial();
			myMainNode.attachChild(btSpatial);
			BillboardControl bbCont =new BillboardControl();
			/**
			 AxialY           Aligns this Billboard to the screen, but keeps the Y axis fixed.
			 AxialZ           Aligns this Billboard to the screen, but keeps the Z axis fixed.
			 Camera           Aligns this Billboard to the camera position.
			 Screen           Aligns this Billboard to the screen.
			 */
			bbCont.setAlignment(BillboardControl.Alignment.Screen);
			btSpatial.addControl(bbCont);
			viewPort.setBackgroundColor(ColorRGBA.Blue);
		}
		private BitmapText makeTextSpatial() {
			String txtB = "ABCDEFGHIKLMNOPQRSTUVWXYZ1234567890`~!@#$%^&*()-=_+[]\\;',./{}|:<>?";
			RenderRegistryClient rrc = getRenderRegistryClient();
			
			BitmapText txtSpatial = rrc.getSceneTextFacade(null).getScaledBitmapText(txtB, 1.0f);
			Rectangle rect = new Rectangle(0, 0, 6, 3);
			
			txtSpatial.setBox(rect);
		//	txtSpatial.setQueueBucket(RenderQueue.Bucket.Transparent);
			txtSpatial.setSize(1.0f);
			txtSpatial.setText(txtB);
			
			BitmapFont bf = txtSpatial.getFont();
			// We will only see the backside of the text unless we set the material of the font to not cull faces.
			// The materials of the font are called its "pages".
			// http://hub.jmonkeyengine.org/forum/topic/render-back-of-bitmaptext/
			int pageCount = bf.getPageSize();
			System.out.println("Disabling culling for a total of " + pageCount + " font materials");
			for (int i=0; i < pageCount; i++) {
				Material fontMat = bf.getPage(i);
				fontMat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
			}
			return txtSpatial;
			
		}
		private void shedMoreLight() { 
			ConfiguredPhysicalModularRenderContext cpmrc = (ConfiguredPhysicalModularRenderContext) getRenderContext();
			RenderRegistryClient rrc = cpmrc.getRenderRegistryClient();
			Vector3f otherLightDir = new Vector3f(0.1f, 0.7f, 1.0f).normalizeLocal();
			DirectionalLight odl = rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(otherLightDir);
			CoreFeatureAdapter.addLightToRootNode(cpmrc, odl);
		}
		
	}
}
