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

package org.cogchar.convoid.output.exec;

import org.cogchar.convoid.output.config.Agenda;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Matt Stevenson
 */
public class AgendaManager {
    private Map<String, Agenda>  myAgendas;

    public AgendaManager(){
        myAgendas = new HashMap<String, Agenda>();
    };

    public void addAgenda(Agenda ac){
        myAgendas.put(ac.getName(), ac);
    }

    public Agenda getAgenda(String name){
        return myAgendas.get(name);
    }

    public void addAgendas(Collection<Agenda> agendas){
        for(Agenda a : agendas){
            addAgenda(a);
        }
    }

    @Override public String toString(){
        String desc = "";
        for(Agenda a : myAgendas.values()){
            desc += a.toString();
            desc += "\n";
        }
        desc = desc.substring(0,desc.length()-1);
        return desc;
    }
}
