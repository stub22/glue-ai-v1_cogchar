/**  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.lifter.model

import org.appdapter.core.name.{FreeIdent,Ident}
//import org.cogchar.bind.lift.{ControlConfig, LiftConfig}
//import org.cogchar.name.lifter.{ActionStrings}
//import org.cogchar.lifter.model.{LifterState,PageCommander}
//import scala.collection.mutable.ArrayBuffer
import org.cogchar.api.thing.{ThingActionSpec, WantsThingAction, TypedValueMap}
import org.cogchar.api.thing.WantsThingAction.ConsumpStatus
import org.cogchar.name.lifter.ActionStrings
import org.cogchar.name.thing.ThingCN
import org.slf4j.{Logger,LoggerFactory}

import org.cogchar.api.fancy.{FancyThingModelWriter}
import org.cogchar.impl.thing.basic.BasicThingActionSpecBuilderTempFunctions
import org.cogchar.impl.thing.basic.{BasicThingActionSpec, BasicTypedValueMapWithConversion, BasicTypedValueMap};
import org.cogchar.impl.thing.fancy.ConcreteTVM;
import java.util.Random;
import org.cogchar.outer.client.AgentRepoClient;
import org.cogchar.outer.client.TestOuterClientSOH;


class LifterClientRegistration extends WantsThingAction {
  val theLogger: Logger = LifterClientRegistration.theLogger
  
  @Override
  def consumeAction(
    actionSpec:ThingActionSpec, srcGraphID:Ident ): ConsumpStatus = {

    val t:TypedValueMap = actionSpec.getParamTVM();
    
    // Check if the TA matches registration for any of the users
    val registrationAction:Ident = t.getAsIdent(
      ActionStrings.LIFTER_ACTION);
    
    val registrationSession:String = t.getAsString(
      ActionStrings.LIFTER_SESSION)
    
    theLogger.debug("checking possible registration action: {}", registrationAction)
      
    for( ID <- LifterClientRegistration.listOfRegistrationIDs ) {
      
      if( registrationAction.getLocalName().equals(ID)) {
        
        theLogger.debug("registration action detected: {}", registrationAction)
    
        // register the user's session
        LifterClientRegistration.registerSession(
          ID,
          registrationSession)
        
        theLogger.info(
          "User \""
          + ID
          + "\" registered with session # "
          + registrationSession)
        
        return ConsumpStatus.CONSUMED;
      }
    }
    return ConsumpStatus.IGNORED;
  }
}



/**
 * Retains user session registrations for local code to reference lifter 
 * browsers with.
 */
object LifterClientRegistration {
  val theLogger: Logger = LoggerFactory.getLogger(classOf[LifterClientRegistration])
  val ID: Ident = new FreeIdent(
    "http://www.glue.ai/system/class/reference#"
    + "org.cogchar.lifter.model.LifterClientRegistration");
  
  // The list from which registered users are indentified
  val listOfRegistrationIDs = List(
    ActionStrings.DEFAULT_REGISTRATION,
    ActionStrings.STUDENT_REGISTRATION,
    ActionStrings.FACILITATOR_REGISTRATION
  )
  
  // Stores the lifter session for user, allowing communication with browser
  private var mapUserToSession: collection.immutable.Map[String,String] =
    collection.Map.empty;
  
  // The start page for a given user role
  val mapRegistrationIDsToStartPageURIs = Map[String,Ident](
    

    ActionStrings.DEFAULT_REGISTRATION ->
    ActionStrings.STUDENT_START_PAGE,
    
    ActionStrings.STUDENT_REGISTRATION ->
    ActionStrings.STUDENT_START_PAGE,
    
    ActionStrings.FACILITATOR_REGISTRATION -> 
    ActionStrings.FACILITATOR_START_PAGE
  )
  
  private val LifterClientRegistrationID: Ident = new FreeIdent(
    "http://www.cogchar.org/schema/crazyRandom#org.cogchar.lifter.model.LifterClientRegistration");
  
  def registerSession( userRegistationID:String, sessionID:String ) {
    
    // This is for backwards compatibility 
    // may be removed once RDF data is updated
    if( userRegistationID == ActionStrings.STUDENT_REGISTRATION ) {
      LifterClientRegistration.mapUserToSession = 
        mapUserToSession + ((ActionStrings.DEFAULT_REGISTRATION, sessionID))
    }
    
    LifterClientRegistration.mapUserToSession = 
      mapUserToSession + ((userRegistationID, sessionID))
  }
  
  def getLifterSession( userRegistationID :String ): String = {
    return LifterClientRegistration.mapUserToSession(userRegistationID);
  }
}