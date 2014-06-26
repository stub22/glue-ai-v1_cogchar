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

package org.cogchar.impl.channel
import org.appdapter.core.matdat.OnlineSheetRepoSpec
import org.appdapter.core.name.{FreeIdent, Ident}
import org.appdapter.core.store.Repo
import org.appdapter.demo.DemoResources
import org.appdapter.help.repo.{RepoClient, RepoClientImpl}
import org.appdapter.impl.store.FancyRepo
/**
 * @author Stu B. <www.texpedient.com>
 */

object TestFancyChans {
	import org.cogchar.impl.channel.FancyChannelSpec;
	import org.cogchar.impl.channel.AnimFileSpecReader
	
	
	def assembleChannelSpecs (rc : RepoClient) : java.util.Set[Object] = {
		rc.assembleRootsFromNamedModel("ccrt:chan_sheet_AZR50")
	}
	
	def go(dfltTestRC : RepoClient) {
		val animGraphID = dfltTestRC.makeIdentForQName(AnimFileSpecReader.animGraphQN);
		val behavCE = new org.cogchar.blob.emit.BehaviorConfigEmitter(dfltTestRC, animGraphID)
		
		val animFiles = AnimFileSpecReader.findAnimFileSpecs(behavCE);
		println("Got animFiles: " + animFiles);

		
	}
}
