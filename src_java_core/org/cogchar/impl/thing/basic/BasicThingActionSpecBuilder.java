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
package org.cogchar.impl.thing.basic;

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.FreeIdent;
import java.util.List;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.impl.thing.basic.BasicTypedValueMapTemporaryImpl;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class BasicThingActionSpecBuilder extends CachingComponentAssembler<BasicThingActionSpec> {
    
    private final static Logger logger = LoggerFactory.getLogger(BasicThingActionSpecBuilder.class);
    
    protected Class<BasicThingActionSpec> decideComponentClass(Ident ident, Item item) {
        return BasicThingActionSpec.class;
    }
    
    private final static String theActionRecordID_Prefix = "Assembled_ActionRecordID#";
    
    //TODO: Extract idents to ontology
    private final static String theTargetThingID_FieldURI = "http://www.cogchar.org/thing/action#targetThing";
    private final static String theTargetThingTypeID_FieldURI = "http://www.cogchar.org/thing/action#targetThingType";
    private final static String theActionVerbID_FieldURI = "http://www.cogchar.org/thing/action#verb";
    private final static String theSourceAgentID_FieldURI = "http://www.cogchar.org/thing/action#";
    private final static String thePostedTimestamp_FieldURI = "http://www.cogchar.org/thing/action#postTStampMsec";
    
    private final static String theThingActionParamList_FieldURI = "http://www.cogchar.org/thing/action#hasParam";
    private final static String theParamIdent_FieldURI = "http://www.cogchar.org/thing/action#paramIdent";
    private final static String theParamIdentValue_FieldURI = "http://www.cogchar.org/thing/action#paramIdentValue";
    private final static String theParamStringValue_FieldURI = "http://www.cogchar.org/thing/action#paramStringValue";
    private final static String theParamIntValue_FieldURI = "http://www.cogchar.org/thing/action#paramIntValue";
    private final static String theParamFloatValue_FieldURI = "http://www.cogchar.org/thing/action#paramFloatValue";
    
    private final static String theThingActionParamLacksIdent_formatString =
            "ThingActionParam \"{}\" did not provide a \"paramIdent\" and was discarded";
    
    private final static String theThingActionParamHasNoValue_formatString =
            "ThingActionParam \"{}\" has no value and was discarded";
    
    private final static String theThingActionParamHasMultipleValues_formatString =
            "ThingActionParam \"{}\" provided multiple values illegally and was discarded";
    
    
    protected void initExtendedFieldsAndLinks(BasicThingActionSpec spec, Item item, Assembler asmblr, Mode mode) {
        ItemAssemblyReader reader =  getReader();
        
        //Create a ActionRecordID on load
        spec.setMyActionRecordID(
                new FreeIdent(
                    theActionRecordID_Prefix + 
                        Long.toString(System.currentTimeMillis())));

        // Load in data from sheet
        spec.setMyTargetThingID(
                new FreeIdent(
                    reader.readConfigValString(
                        item.getIdent(), 
                        theTargetThingID_FieldURI, 
                        item, 
                        "")));
        
        spec.setMyTargetThingTypeID(
                new FreeIdent(
                    reader.readConfigValString(
                        item.getIdent(), 
                        theTargetThingTypeID_FieldURI, 
                        item, 
                        "")));
        
        spec.setMyActionVerbID(
                new FreeIdent(
                    reader.readConfigValString(
                        item.getIdent(), 
                        theActionVerbID_FieldURI, 
                        item, 
                        "")));
        
        spec.setMySourceAgentID(
                new FreeIdent(
                    reader.readConfigValString(
                        item.getIdent(), 
                        theSourceAgentID_FieldURI, 
                        item, 
                        "")));
        
        spec.setMyPostedTimestamp(
            reader.readConfigValLong(
                item.getIdent(), 
                thePostedTimestamp_FieldURI, 
                item, 
                Long.valueOf(-1)));
        
        // Pull in the parameters
        List<Item> paramItems = item.getLinkedOrderedList(
                new FreeIdent(theThingActionParamList_FieldURI));
        
        BasicTypedValueMap paramDictionary =
                new BasicTypedValueMapTemporaryImpl();
        for( Item i : paramItems ) {
            
            // Collect the Ident to be used as the dictionary key
            Ident name = null;
            Set<Item> nameItem_RawSet = 
                    i.getLinkedItemSet( 
                    new FreeIdent(theParamIdent_FieldURI),
                    Item.LinkDirection.FORWARD);
            if( nameItem_RawSet == null && nameItem_RawSet.size() != 1 ) {
                logger.warn(
                        theThingActionParamLacksIdent_formatString,
                        i.getIdent());
                continue;
            }   
            else { name = nameItem_RawSet.iterator().next().getIdent(); }
            
            // Collect the type and value of the dictionary value
            // There should be only one type.
            Set<Item> identTypeItems =
                    i.getLinkedItemSet(
                        new FreeIdent(theParamIdentValue_FieldURI), 
                        Item.LinkDirection.FORWARD);
            
            Set<Item> stringTypeItems =
                    i.getLinkedItemSet( 
                        new FreeIdent(theParamStringValue_FieldURI), 
                        Item.LinkDirection.FORWARD);

            Set<Item> intTypeItems =
                    i.getLinkedItemSet( 
                        new FreeIdent(theParamIntValue_FieldURI), 
                        Item.LinkDirection.FORWARD);
            
            Set<Item> floatTypeItems =
                    i.getLinkedItemSet( 
                        new FreeIdent(theParamFloatValue_FieldURI), 
                        Item.LinkDirection.FORWARD);
            
            
            
            // Get the value
            Object value = null;
            int typeCount = identTypeItems.size() + 
                    stringTypeItems.size() + 
                    intTypeItems.size() + 
                    floatTypeItems.size();
            if( typeCount != 1 ) {
                //Log sheet problem
                if( typeCount > 1 ) {
                    logger.warn(
                            theThingActionParamHasMultipleValues_formatString,
                            i.getIdent());
                }
                else {    
                    logger.warn(
                            theThingActionParamHasNoValue_formatString,
                            i.getIdent());
                }
                continue;
            }
            else {
                // Get the typed value as an Object
                if( identTypeItems.size() == 1 ) {
                    value = identTypeItems.iterator().next().getIdent();
                }
                else if( stringTypeItems.size() == 1 ) {
                    value = stringTypeItems.iterator().next().getValString(
                            new FreeIdent(theParamStringValue_FieldURI), "");
                } 
                else if( intTypeItems.size() == 1 ) {
                    value = intTypeItems.iterator().next().getValInteger(
                            new FreeIdent(theParamIntValue_FieldURI),
                            new Integer(-1));
                }
                else if( floatTypeItems.size() == 1 ) {
                    value = intTypeItems.iterator().next().getValDouble(
                            new FreeIdent(theParamFloatValue_FieldURI),
                            new Double(-1));
                }
            }
            
            // Put the resulting item in the map
            if (name != null && value != null) {
                paramDictionary.putValueAtName(name, value);
            }
        }
        spec.setMyParamTVMap(paramDictionary);
    }
}