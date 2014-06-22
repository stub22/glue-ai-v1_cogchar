/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.web.config;

import java.util.List;
import org.cogchar.api.web.WebControl;

/**
 * @author Stu B. <www.texpedient.com>
 */

public interface SessionGroupManager {
		void setConfigForSession(String sessionId, LiftConfig config);
		
		void setControlForSessionAndSlot(String sessionId, int slotNum, WebControl newConfig);

		void loadPage(String sessionId, String path);
		
		String getSessionVariable(String sessionId, String key);
		
		void showSessionError(String errorSourceKey, String errorText, String sessionId);
		
		List<String> getActiveSessions();
}
