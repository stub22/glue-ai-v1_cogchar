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

/**  This mediator is our "/etc/rc", the "top" mechanism available to customize the Cogchar PUMA boot up sequence.
 * @author Stu B. <www.texpedient.com>
 */
public class PumaContextMediator {

	/** Boot callback sequence #1.
	 *  The mediator should now do any special init that it wants to, but without assuming GUI exists.
		 * This stage includes getting the FIRST whack (both read and write) at the configuration services.  
		 * This should happen before the mediator is required to answer the getFlags and getMainConfigIdent,
		 * getModeIdent kinds of questions.
		 * However, the default mediator impl does nothing.  If you want the whole application to fail and
		 * shut down, you can signal that by allowing an exception to escape.
	 * @param ctx
	 * @throws Throwable 
	 */
	public void notifyContextBuilt(PumaAppContext ctx) throws Throwable {
	}
	/**  Boot callback sequence #2 - optional.
	 * Could apply to any GUI concepts, but currently applies only to our VWorld OpenGL panel.  This callback
	 * is currently *not* invoked if there is no VWorld startup (as controlled by VWorld flag below).
	 * Called after panels constructed but before startOpenGLCanvas. Allows GUI to intervene and realize the panels as
	 * needed. This step is normally used when getFlagAllowJFrames returns false, e.g. when we are running under NB
	 * platform and we want to manage windows manually). 
	 * 
	 * This callback comes after notifyContextBuilt, only if panels are actually constructed, as noted above.
	 * 
	 * If you want the whole application to fail and shut down, you can signal that by allowing an exception to escape.
	 * 
	 * @param ctx
	 * @throws Throwable
	 */	
	public void notifyPanelsConstructed(PumaAppContext ctx) throws Throwable {
	}
	/**  Boot callback sequence #3.
	 * The Puma chars have now been loaded, or reloaded.  If you want to fix up
	 * your chars as a group, this is a good opportunity to do so.  This might
	 * be called repeatedly if user re-loads the character system repeatedly,
	 * without a full reboot (and hence without the other callbacks necessarily
	 * repeating).
	 * 
	 * @param ctx
	 * @throws Throwable 
	 */
	public void notifyCharactersLoaded(PumaAppContext ctx) throws Throwable {
	}	
	/**
	 *  Boot callback sequence #4.
	 * Last thing that happens before result processing during a boot is this callback.
	 * Allows app to make sure it gets the last word on config + state.
	 * @param ctx
	 * @throws Throwable 
	 */
	public void notifyBeforeBootComplete(PumaAppContext ctx) throws Throwable {
		
	}

	public String getOptionalFilesysRoot() {
		return null;
	}


	public String getPanelKind() {
		return "SLIM";
	}
	public boolean getFlagAllowJFrames() {
		return true;
	}
	public boolean getFlagIncludeVirtualWorld() { 
		return true;
	}
	public boolean getFlagIncludeWebServices() { 
		return false;
	}
	public boolean getFlagIncludeCharacters() { 
		return true;
	}
	/**** Todo:  Allow this mediator to specify what kind of repo should be used for main config.
	 */
	
	// IGNORE:This is not really used at present.
	public String getSysContextRootURI() {
		String uriPrefix = "http://model.cogchar.org/char/bony/";
		String bonyCharUniqueSuffix = "0x0000FFFF";
		String sysContextURI = uriPrefix + bonyCharUniqueSuffix;
		return sysContextURI;
	}	
}
