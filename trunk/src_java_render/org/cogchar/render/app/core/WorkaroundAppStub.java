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
package org.cogchar.render.app.core;

import org.cogchar.render.sys.task.CogcharRenderSchedule;
import com.jme3.font.BitmapFont;
import com.jme3.input.FlyByCamera;
import com.jme3.system.AppSettings;
import com.jme3.renderer.ViewPort;

/** WorkaroundAppStub exposes the few required features of JME3 SimpleApplication that Cogchar does not factor out 
 * into Cogchar facade APIs.
 * 
 * These capabilities should be broken out into narrower interfaces, enabling us to more clearly
 * self-document the dependencies on these features in client code.
 * @author Stu B. <www.texpedient.com>
 */
public interface WorkaroundAppStub extends CogcharRenderSchedule {
	public void setAppSettings(AppSettings someSettings);
	public void setGuiFont(BitmapFont font);
	public void setAppSpeed(float appSpeed);
	public void setPauseOnLostFocus(boolean pauseFlag);
	
	//TODO:  Factor this into camera manager
	
	public FlyByCamera getFlyByCamera();
	
	// TOD: Factor this into ViewportFacade;
	public ViewPort  getPrimaryAppViewPort();
	

}
