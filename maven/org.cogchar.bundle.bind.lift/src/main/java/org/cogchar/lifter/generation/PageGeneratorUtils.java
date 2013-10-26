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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import java.util.Map;
import org.appdapter.core.name.Ident;
import com.hp.hpl.jena.rdf.model.Resource;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.name.lifter.LiftAN;
        
        
//        import com.hp.hpl.jena.rdf.model.{Model, Statement, Resource, Property, Literal, RDFNode, ModelFactory}
//import com.hp.hpl.jena.query.{ResultSet, ResultSetFormatter, ResultSetRewindable, ResultSetFactory, QuerySolution};
//import com.hp.hpl.jena.ontology.{OntProperty, ObjectProperty, DatatypeProperty}
//import com.hp.hpl.jena.datatypes.{RDFDatatype, TypeMapper}
//import com.hp.hpl.jena.datatypes.xsd.{XSDDatatype}
//import com.hp.hpl.jena.shared.{PrefixMapping}

/**
 * This class provides a mechanism for dynamically generating lifter pages for
 * a question-response scenario.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class PageGeneratorUtils {
    
    /**
     * Generates a page and stores it in the local repo.
     * 
     * @param lifterConfigID ID of the page to be generated
     * @param answerLabelsAndAction button label => button action
     */
    public static Model generate12SlotLifterPageWithPushyButtons(
            Ident lifterConfigID,
            Map<Integer, LifterControlDescription> controlDescriptions) {
        
        // The RDF container
        Model dataModel = ModelFactory.createDefaultModel();
        
        // Generate lifterConfig RDF idividial, which represents the lifter page
        generate12SlotLifterConfig( dataModel, lifterConfigID );
        
        // Generate the button for each response
        for( Integer key : controlDescriptions.keySet() ) {
            LifterControlDescription desc = controlDescriptions.get(key);
            generatePushyButtonControlForQuestion(
                    dataModel, 
                    lifterConfigID, 
                    key,
                    desc.label,
                    desc.color,
                    desc.image,
                    desc.action);
        }
        return dataModel;
    }
    
    /**
     * Collection of values to describe a button that should be generated
     */
    public static class LifterControlDescription {
        
        public static enum descriptionElement { 
            BUTTON_COLOR, 
            BUTTON_IMAGE, 
            BUTTON_LABEL, 
            BUTTON_ACTION };
        
        public String color = null;
        public String image = null;
        public String label = null;
        public Ident action = null;
    }
    
    private static void generate12SlotLifterConfig(
            Model dataModel, 
            Ident lifterConfigID) {
        
        // Generate lifterConfig idividial
        Resource lifterConfigResource = dataModel.createResource(
                lifterConfigID.getAbsUriString());
        
        // Defines the RDF type to be that of a liftConfig page definition
        Property rdfType = dataModel.createProperty(
                GoodyNames.RDF_TYPE.getAbsUriString());
        lifterConfigResource.addProperty(
                rdfType, 
                "liftconfig");
        
        // Sets the page template to be used by the page
        Property pageTemplate = dataModel.createProperty(LiftAN.P_template);
        lifterConfigResource.addProperty(
                pageTemplate, 
                "12slots");
    }
    
    private static void generatePushyButtonControlForQuestion(
            Model dataModel,
            Ident lifterConfigID,
            Integer controlCount,
            String label,
            String color,
            String image,
            Ident thingAction) {
        
        // Generate control individual
        String controlIDString = 
                lifterConfigID.getAbsUriString() +
                "_control_" +
                controlCount.toString();
        Resource controlResource = dataModel.createResource(controlIDString);
        
        // Defines the RDF type to be that of a liftcontrol
        Property rdfType = dataModel.createProperty(
            GoodyNames.RDF_TYPE.getAbsUriString());
        controlResource.addProperty(
                rdfType, 
                "liftcontrol");
        
        // Attach the control to its parent page
        Property parentConfig = dataModel.createProperty(
                LiftAN.P_liftConfig);
        controlResource.addProperty(
                parentConfig,
                lifterConfigID.getAbsUriString());
        
        // Define the control type to use
        Property controlType = dataModel.createProperty(
                LiftAN.P_controlType);
        controlResource.addProperty(
                controlType,
                "PUSHYBUTTON");
        
        // set the button's label
        if(label != null && !label.isEmpty()) {
            Property buttonLabelText = dataModel.createProperty(
                    LiftAN.P_controlText);
            controlResource.addProperty(
                    buttonLabelText,
                    label);
        }
        
        //TODO: type checking on color
        // set the style element (the color) of the button
        if(color != null && !color.isEmpty()) {
            Property buttonStyle = dataModel.createProperty(
                    LiftAN.P_controlStyle);
            controlResource.addProperty(
                    buttonStyle,
                    color);
        }
        
        // set the buttons resource (the image)
        if(image != null && !image.isEmpty()) {
            Property buttonResource = dataModel.createProperty(
                    LiftAN.P_controlResource);
            controlResource.addProperty(
                    buttonResource,
                    image);
        }
        
        // set the buttons action
        if(thingAction != null) {
            Property buttonAction = dataModel.createProperty(
                    LiftAN.P_controlAction);
            controlResource.addProperty(
                    buttonAction,
                    thingAction.getAbsUriString());
        }
        
    }
}
