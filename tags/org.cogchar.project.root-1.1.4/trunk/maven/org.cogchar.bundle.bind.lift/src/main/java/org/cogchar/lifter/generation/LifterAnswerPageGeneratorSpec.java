/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.lifter.generation;

import org.appdapter.core.name.Ident;

/**
 * This spec provides the data needed for th LifterAnswerPageGenerator to
 * perform its function.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */


public class LifterAnswerPageGeneratorSpec {    
    private Ident ThingActionModelURI;
    private Ident LifterModelURI;

    public Ident getThingActionModelURI() {
        return ThingActionModelURI;
    }

    public void setThingActionModelURI(Ident ThingActionModelURI) {
        this.ThingActionModelURI = ThingActionModelURI;
    }

    public Ident getLifterModelURI() {
        return LifterModelURI;
    }

    public void setLifterModelURI(Ident LifterModelURI) {
        this.LifterModelURI = LifterModelURI;
    }
}
