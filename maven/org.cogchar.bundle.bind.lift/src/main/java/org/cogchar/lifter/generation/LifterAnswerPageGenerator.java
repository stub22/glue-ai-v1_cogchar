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
import org.appdapter.core.log.BasicDebugger;


/**
 * When properly registered, this scanner consumes TAs that define liftercofigs.
 * These configs use the 12slot template and have pushybuttons. For each control
 * a button color, image, label and action can be defined.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class LifterAnswerPageGenerator extends BasicDebugger implements WantsThingAction {
    
    private Ident lifterAnswerPageIdent = new FreeIdent(LiftAN.NS_liftconfig);
    private String lifterAnswerPageControlLabelPrefix = "label_";
    private String lifterAnswerPageControlResourcePrefix = "resource_";
    private String lifterAnswerPageControlActionPrefix = "action_";
    private String lifterAnswerPageControlColorPrefix = "color_";
    
    private Model myLifterModelReadonly = null;
    private Model myThingActionModelReadonly = null;
    
    /**
     * Provide models to which the new RDF data will be stored. One model will
     * be the source Lifter draws its web data from, the other will be where it
     * scans for thing actions.
     * 
     * @param lifterModel The data model that will receive the new RDF data
     * @param thingActionModel The data model that will receive the trigger TA
     */
    public LifterAnswerPageGenerator(
            Model lifterModel, 
            Model thingActionModel ) {
        
        myLifterModelReadonly = lifterModel;
        myThingActionModelReadonly = thingActionModel;
    }
        
    /**
     * Consumes TAs that indicate a Lifter web page should be created.
     * 
     * @param actionSpec
     * @param sourceGraphID
     * @return 
     */
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
        Model pageModel = PageGeneratorUtils.make12SlotLifterPageWithPushyButtons(
                liftconfigID, controlDescriptions);
        
		getLogger().warn("Adding contents of pageModel to 'myLifterModelReadonly'");  // FIXME
        // Merge the model into the main model
        myLifterModelReadonly.add(pageModel);
        
		getLogger().warn("Adding contents taModel to 'myThingActionModelReadonly'");  // FIXME
        // Fire the TA that triggers the lifter page to display.
        Model taModel = PageGeneratorUtils.
                makeThingActionTriggerForLifterPage(liftconfigID);
        myThingActionModelReadonly.add(taModel);
        
        // The page is available and the triggering TA is in transit.
        return ConsumpStatus.CONSUMED;
    }

    /**
     * Clip the number off the end of the URI
     */
    private Integer parseInt(String localName, String prefix) {
        //TODO: handle exceptions
        return new Integer(Integer.parseInt(localName.replaceFirst(prefix, "")));
    }

    /**
     * This function helps build up the RDF that defines a Lifter control.
     * 
     * @param controlElements
     * @param controlNumber
     * @param elementType
     * @param element 
     */
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
    
    /**
     * This function helps build up the RDF that defines a Lifter control.
     * 
     * @param controlElements
     * @param controlNumber
     * @param elementType
     * @param element 
     */
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
