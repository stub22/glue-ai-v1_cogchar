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
package org.cogchar.api.perform;

import org.appdapter.core.name.Ident;
import java.util.List;

/**
 
 * A Channel is a handle to a useful symbol/signal stream resource producer/consumer.  
 * @author Stu B. <www.texpedient.com>
 */

public interface Channel {
	    /**
     * Returns the unique identifier for this Channel.
     * @return unique identifier for this Channel
     */
    public Ident getIdent();
    /**
     * Returns the name of the Channel.
     * @return name of the Channel
     */
    public String getName();
}
