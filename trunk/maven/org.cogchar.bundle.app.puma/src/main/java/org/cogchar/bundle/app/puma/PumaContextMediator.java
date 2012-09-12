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

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaContextMediator {

	/**
	 * Called after panels constructed but before startOpenGLCanvas. Allows GUI to intervene and realize the panels as
	 * needed. This step is generally, when getFlagAllowJFrames returns false, e.g. when we are running under NB
	 * platform and we want to manage windows manually).
	 *
	 * @param ctx
	 * @throws Throwable
	 */
	public void notifyContextBuilt(PumaAppContext ctx) throws Throwable {
	}

	public boolean getFlagAllowJFrames() {
		return true;
	}

	public String getOptionalFilesysRoot() {
		return null;
	}

	public String getSysContextRootURI() {
		String uriPrefix = "http://model.cogchar.org/char/bony/";
		String bonyCharUniqueSuffix = "0x0000FFFF";
		String sysContextURI = uriPrefix + bonyCharUniqueSuffix;
		return sysContextURI;
	}

	public String getPanelKind() {
		return "SLIM";
	}
}
