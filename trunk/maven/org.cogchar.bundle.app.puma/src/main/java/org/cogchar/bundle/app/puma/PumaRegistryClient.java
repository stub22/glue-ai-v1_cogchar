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

package org.cogchar.bundle.app.puma;

import org.appdapter.core.name.Ident;
import org.cogchar.platform.trigger.BoxSpace;
import org.cogchar.platform.trigger.CommandSpace;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface PumaRegistryClient {
	
	//public PumaAppContext getAppContext(Ident optSpecID);
	//public void putAppContext(PumaAppContext pac, Ident optSpecID);
	
	public PumaContextMediator getCtxMediator(Ident optSpecID);
	// public void putCtxMediator (PumaContextMediator med, Ident optSpecID);
	
	public PumaConfigManager getConfigMgr(Ident optSpecID);
	//public void putConfigMgr (PumaConfigManager pcm, Ident optSpecID);
		
	public BoxSpace getTargetBoxSpace(Ident optSpecID);
	//public void putTargetBoxSpace(BoxSpace bs, Ident optSpecID);

	public CommandSpace getCommandSpace(Ident optSpecID);
	//public void putCommandSpace(CommandSpace cs, Ident optSpecID);

	public PumaVirtualWorldMapper getVWorldMapper(Ident optSpecID);
	public void putVWorldMapper (PumaVirtualWorldMapper v, Ident optSpecID);
	
	public PumaWebMapper getWebMapper(Ident optSpecID);
	public void putWebMapper(PumaWebMapper wm, Ident optSpecID);
	
	
}
