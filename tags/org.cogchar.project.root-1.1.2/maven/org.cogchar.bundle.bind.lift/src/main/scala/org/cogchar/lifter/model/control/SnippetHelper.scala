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

package org.cogchar.lifter.model.control
import org.cogchar.lifter.model.main.PageCommander
import org.cogchar.impl.web.wire.{SessionOrganizer}
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This workaround allows Snippets instantiated by the framework to always find the session organizer.
 * It is currently needed by the following kinds of snippets (only):
 
 * 	val togButton = new ToggleButton // (sorg)
	val selBoxes = new SelectBoxes // (sorg)
	val listBox = new ListBox   // (sorg)
	val loginForm = new LoginForm  // (sorg)
	val radioButtons = new RadioButtons // (sorg)
	val linkList = new LinkList   // (sorg)
	val textForm = new TextForm   // (sorg)
	val dualTextForm = new DualTextForm   /// (sorg)
 */

object SnippetHelper {
	lazy val	mySessionOrganizer : SessionOrganizer = PageCommander.getSessionOrg
}
