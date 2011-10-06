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

package org.cogchar.speech;

/**
 *
 * @author josh
 */
public interface ITTSEngineObserver
{
    /**
     * @note Do not alter this method name. Or you need to change native code that reflectively searches for it.
     */
    public void startInputStreamEvent(long streamNumber);

    /**
     * @note Do not alter this method name. Or you need to change native code that reflectively searches for it.
     */
    public void endInputStreamEvent(long streamNumber);

    /**
     * @note Do not alter this method name. Or you need to change native code that reflectively searches for it.
     */
    public void bookMarkEvent(long streamNumber, java.lang.String s);

    /**
     * @note Do not alter this method name. Or you need to change native code that reflectively searches for it.
     */
    public void visemeEvent(long streamNumber, int curViseme, int duration, byte flags, int nextViseme);
}
