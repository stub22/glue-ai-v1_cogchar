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

import com.hp.hpl.jena.rdf.model.Model;
import java.util.HashMap;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.thing.WantsThingAction;
import java.util.Iterator;
import java.util.Map;
import org.appdapter.core.name.FreeIdent;
import org.cogchar.lifter.generation.PageGeneratorUtils.LifterControlDescription;
import org.cogchar.name.lifter.LiftAN;

/**
 * When properly registered, this scanner consumes TAs that define liftercofigs.
 * These configs use the 12slot template and have pushybuttons. For each control
 * a button color, image, label and action can be defined.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class LifterAnswerPageGenerator implements WantsThingAction {
    
    Ident lifterAnswerPageIdent = new FreeIdent(LiftAN.NS_liftconfig);
    String lifterAnswerPageControlLabelPrefix = "label_";
    String lifterAnswerPageControlResourcePrefix = "resource_";
    String lifterAnswerPageControlActionPrefix = "action_";
    String lifterAnswerPageControlColorPrefix = "color_";
    
    Model myModel = null;
    
    //TODO Document model purpose better.
    public LifterAnswerPageGenerator( Model model ) {
        myModel = model;
    }
    
        
    @Override
    public ConsumpStatus consumeAction(
            ThingActionSpec actionSpec,
            Ident sourceGraphID) {
        
        TypedValueMap tvm = actionSpec.getParamTVM();

        // Ignore irrelevant TAs
        if (tvm.getAsIdent(lifterAnswerPageIdent) == null) {
            return ConsumpStatus.IGNORED;
        }

        // Collect the ID of the page to be generated
        Ident liftconfigID = tvm.getAsIdent(lifterAnswerPageIdent);

        // Collect the control elements together in a Map
        Map<Integer, LifterControlDescription> controlDescriptions =
                new HashMap<Integer, LifterControlDescription>();
        Iterator<Ident> tvmIterator = tvm.iterateKeys();
        while (tvmIterator.hasNext()) {
            Ident currentElementKey = tvmIterator.next();

            String localName = currentElementKey.getLocalName();

            // Using the prefix of the local name, determine data item
            // Drop the prefix to get the control number the element is tied to
            Integer controlNumber;
            if (localName.startsWith(lifterAnswerPageControlLabelPrefix)) {
                controlNumber = parseInt(
                        localName,
                        lifterAnswerPageControlLabelPrefix);
                addControlElement(
                        controlDescriptions,
                        controlNumber,
                        LifterControlDescription.descriptionElement.BUTTON_LABEL,
                        tvm.getAsString(currentElementKey));
            }
            else if (localName.startsWith(lifterAnswerPageControlResourcePrefix)) {
                controlNumber = parseInt(
                        localName,
                        lifterAnswerPageControlResourcePrefix);
                addControlElement(
                        controlDescriptions,
                        controlNumber,
                        LifterControlDescription.descriptionElement.BUTTON_IMAGE,
                        tvm.getAsString(currentElementKey));
            }
            else if (localName.startsWith(lifterAnswerPageControlActionPrefix)) {
                controlNumber = parseInt(
                        localName,
                        lifterAnswerPageControlActionPrefix);
                addControlElement(
                        controlDescriptions,
                        controlNumber,
                        LifterControlDescription.descriptionElement.BUTTON_ACTION,
                        tvm.getAsIdent(currentElementKey));
            }
            else if (localName.startsWith(lifterAnswerPageControlColorPrefix)) {
                controlNumber = parseInt(
                        localName,
                        lifterAnswerPageControlColorPrefix);
                addControlElement(
                        controlDescriptions,
                        controlNumber,
                        LifterControlDescription.descriptionElement.BUTTON_COLOR,
                        tvm.getAsString(currentElementKey));
            }
        }
        
        // Generate the model with the new page
        Model pageModel = PageGeneratorUtils.generate12SlotLifterPageWithPushyButtons(
                liftconfigID, controlDescriptions);
        
        // Merge the model into the main model
        myModel.add(pageModel);
        
        return ConsumpStatus.CONSUMED;
    }

    /**
     * Clip the number off the end of the URI
     */
    private Integer parseInt(String localName, String prefix) {
        //TODO: handle exceptions
        return new Integer(Integer.parseInt(localName.replaceFirst(prefix, "")));
    }

    private void addControlElement(
            Map<Integer, LifterControlDescription> controlElements,
            Integer controlNumber,
            LifterControlDescription.descriptionElement elementType,
            String element) {

        if (!controlElements.containsKey(controlNumber)) {
            controlElements.put(controlNumber, new LifterControlDescription());
        }
        // The action should be handled elsewhere
        // TODO: default warning
        switch (elementType) {
            case BUTTON_COLOR:
                controlElements.get(controlNumber).color = element;
                break;
            case BUTTON_IMAGE:
                controlElements.get(controlNumber).image = element;
                break;
            case BUTTON_LABEL:
                controlElements.get(controlNumber).label = element;
                break;
        }
    }
    
    private void addControlElement(
            Map<Integer, LifterControlDescription> controlElements,
            Integer controlNumber,
            LifterControlDescription.descriptionElement elementType,
            Ident element) {

        if (!controlElements.containsKey(controlNumber)) {
            controlElements.put(controlNumber, new LifterControlDescription());
        }
        // Only the action should get this far
        // TODO: default warning
        switch (elementType) {
            case BUTTON_ACTION:
                controlElements.get(controlNumber).action = element;
                break;
        }
    }
}
