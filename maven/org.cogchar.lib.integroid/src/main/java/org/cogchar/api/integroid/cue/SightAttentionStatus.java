/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.api.integroid.cue;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public enum SightAttentionStatus {
	CONFIRMED,		// Sight is on-camera, centered, and identity-confirmed
	MISTAKEN,		// We thought this sight was on-camera and centered, but identity is dis-confirmed (it's someone/something else)
	BLURRY,			// Presumed target is on-camera but not identified (yet!)
	MISSING,		// We are pointing near last known pos, but can't see target
	SCANNING,		// Looking around near last known pos
	TRAVELING,		// Not yet near last known pos
	IGNORED			// Not currently paying attention to this target.
}
