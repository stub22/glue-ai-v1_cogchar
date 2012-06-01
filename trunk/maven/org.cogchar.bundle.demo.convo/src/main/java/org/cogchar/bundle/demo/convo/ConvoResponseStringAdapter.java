/*
 * Copyright 2012 by The Cogchar Project (www.cogchar.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.bundle.demo.convo;

import org.cogchar.bind.cogbot.main.GenRespWithConf;
import org.jflux.api.core.Adapter;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */
public class ConvoResponseStringAdapter<R extends GenRespWithConf> implements 
        Adapter<R,String>{

    @Override
    public String adapt(R a) {
        if(a == null){
            return null;
        }
        return a.getResponse();
    }
    
}
