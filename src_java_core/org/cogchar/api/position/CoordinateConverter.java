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
package org.cogchar.api.position;

/**
 * Converts coordinates from one coordinate space to another.
 * @author Matthew Stevenson
 */
public interface CoordinateConverter<CoordSrc,CoordDest> {
    /**
     * Converts the given coordinate to the destination space.
     * @param coord coordinate to convert
     * @return coordinate in the destination coordinate space
     */
    public CoordDest convert(CoordSrc coord);
}
