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

import org.appdapter.core.item.Item.LinkDirection;

import static org.cogchar.name.thing.ThingCN.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the SpecBuilder to construct RuntimeTAs for use in steps that fire
 * a TA directly.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class BasicThingActionSpecBuilder extends 
        CachingComponentAssembler<BasicThingActionSpec> {
    
    private final static Logger logger =
            LoggerFactory.getLogger(BasicThingActionSpecBuilder.class);
    
    protected Class<BasicThingActionSpec> decideComponentClass(
            Ident ident,
            Item item) {
        return BasicThingActionSpec.class;
    }
    
    private final static String theActionRecordIdentPrefix = TA_NS + "stepTA-";
    
    private final static FreeIdent theTargetThingFieldIdent =
            safeFreeIdent(P_targetThing);
    private final static FreeIdent theTargetThingTypeFieldIdent =
            safeFreeIdent(P_targetThingType);
    private final static FreeIdent theActionVerbFieldIdent =
            safeFreeIdent(P_verb);
    private final static FreeIdent theSourceAgentFieldIdent =
            safeFreeIdent(P_sourceAgent);
    private final static FreeIdent thePostedTimestampFieldIdent =
            safeFreeIdent(P_postedTSMsec);
    private final static FreeIdent theThingActionParamAttachedTAFieldIdent =
            safeFreeIdent(P_IdentAttachedToThingAction);
    
    private final static FreeIdent theParamIdentFieldIdent =
            safeFreeIdent(P_paramIdent);
    private final static FreeIdent theParamIdentValueFieldIdent =
            safeFreeIdent(P_paramIdentValue);
    private final static FreeIdent theParamStringValueFieldIdent =
            safeFreeIdent(P_paramStringValue);
    private final static FreeIdent theParamIntValueFieldIdent =
            safeFreeIdent(P_paramIntValue);
    private final static FreeIdent theParamFloatValueFieldIdent =
            safeFreeIdent(P_paramFloatValue);
    
    
    protected void initExtendedFieldsAndLinks(
            BasicThingActionSpec spec, 
            Item item,
            Assembler asmblr,
            Mode mode) {
        ItemAssemblyReader reader =  getReader();
        
        //Create a ActionRecordID on load
        spec.setMyActionRecordID(
                safeFreeIdent(
                    theActionRecordIdentPrefix + 
                        Long.toString(System.currentTimeMillis())));
        

        Item targetThingItem = item.getOptionalSingleLinkedItem(
                theTargetThingFieldIdent, 
                LinkDirection.FORWARD);
        if(targetThingItem != null){
            spec.setMyTargetThingID(targetThingItem.getIdent());
        }
        
        Item targetTypeItem = item.getOptionalSingleLinkedItem(
                theTargetThingTypeFieldIdent, 
                LinkDirection.FORWARD);
        if(targetTypeItem != null){
            spec.setMyTargetThingTypeID(targetTypeItem.getIdent());
        }
        
        Item actionVerbItem =item.getOptionalSingleLinkedItem(
                theActionVerbFieldIdent, 
                LinkDirection.FORWARD);
        if(actionVerbItem != null){
            spec.setMyActionVerbID(actionVerbItem.getIdent());
        }
        
        Item sourceAgentItem = item.getOptionalSingleLinkedItem(
                theSourceAgentFieldIdent, 
                LinkDirection.FORWARD);
        if(sourceAgentItem != null){
            spec.setMySourceAgentID(sourceAgentItem.getIdent());
        }
        
        Long postedTimestamp = item.getValLong(
                thePostedTimestampFieldIdent,
                System.currentTimeMillis());
        spec.setMyPostedTimestamp(postedTimestamp);
        
        // Pull in the parameters
        Set<Item> paramItems = item.getLinkedItemSet(
                    theThingActionParamAttachedTAFieldIdent,
                    LinkDirection.FORWARD);
        
        //reader.//
        
        BasicTypedValueMap paramDictionary =
                new BasicTypedValueMapTemporaryImpl();
        for( Item i : paramItems ) {
            
            // Collect the Ident to be used as the dictionary key
            Ident name = null;
            Set<Item> nameItem_RawSet = 
                    i.getLinkedItemSet( 
                        theParamIdentFieldIdent,
                        Item.LinkDirection.FORWARD);
            if( nameItem_RawSet == null && nameItem_RawSet.size() != 1 ) {
                logger.warn(
                        "ThingActionParam \"{}\" did not provide a \"paramIdent\" and was discarded",
                        i.getIdent());
                continue;
            }   
            else { name = nameItem_RawSet.iterator().next().getIdent(); }
            
            // Collect the type and value of the dictionary value
            // There should be only one type.
            Set<Item> identTypeItems =
                    i.getLinkedItemSet(
                    		theParamIdentValueFieldIdent, 
                        Item.LinkDirection.FORWARD);
            
            Set<Item> stringTypeItems =
                    i.getLinkedItemSet( 
                    		theParamStringValueFieldIdent, 
                        Item.LinkDirection.FORWARD);

            Set<Item> intTypeItems =
                    i.getLinkedItemSet( 
                    		theParamIntValueFieldIdent, 
                        Item.LinkDirection.FORWARD);
            
            Set<Item> floatTypeItems =
                    i.getLinkedItemSet( 
                    	theParamFloatValueFieldIdent, 
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
                            "ThingActionParam \"{}\" provided multiple values illegally and was discarded",
                            i.getIdent());
                }
                else {    
                    logger.warn(
                            "ThingActionParam \"{}\" has no value and was discarded",
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
                    		theParamStringValueFieldIdent, "");
                } 
                else if( intTypeItems.size() == 1 ) {
                    value = intTypeItems.iterator().next().getValInteger(
                    		theParamIntValueFieldIdent,
                            new Integer(-1));
                }
                else if( floatTypeItems.size() == 1 ) {
                    value = intTypeItems.iterator().next().getValDouble(
                            theParamFloatValueFieldIdent,
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


	protected static FreeIdent safeFreeIdent(String uri) {
		if (uri.indexOf('#') == -1) {
			logger.warn("Not URI so prefixing {} with TA_NS ", uri);
			uri = TA_NS + uri;
		}
		return new FreeIdent(uri);
	}
}