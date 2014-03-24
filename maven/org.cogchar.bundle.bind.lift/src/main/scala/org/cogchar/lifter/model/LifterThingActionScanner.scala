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
import org.slf4j.{Logger,LoggerFactory}

class LifterThingActionScanner extends WantsThingAction {
  val theLogger: Logger = 
    LoggerFactory.getLogger(classOf[LifterThingActionScanner])
    
//  private val lifterFlowActionID:Ident =
//    new FreeIdent("http://www.cogchar.org/lift/flow/action#action");
//  private val lifterFlowActionID:Ident =
//    new FreeIdent("http://www.cogchar.org/lift/flow/action#action");

//  private val lifterActionID : Ident = 
//            new FreeIdent("http://www.cogchar.org/lift/user/action#action");
  
  private val lifterActionIDPrefix : String = 
    ("http://www.cogchar.org/lift/user/action#");
  
  private val lifterConfigIDPrefix : String =
    "http://www.cogchar.org/lift/config/configroot#";
  
  @Override
  def consumeAction(
    actionSpec:ThingActionSpec, srcGraphID:Ident ): ConsumpStatus = {
    
    val t: TypedValueMap = actionSpec.getParamTVM();
      
    // Check to see if action is associated with a registered user.
    for ( ID <- LifterClientRegistration.listOfRegistrationIDs ) {
      
      
      theLogger.trace( "localName: " + ID )
      theLogger.trace( "lifterRegistrationIDPrefix: " + lifterActionIDPrefix )
        
      // Take the value (command) associated with this action key
      val configControlID:Ident = t.getAsIdent(
        new FreeIdent( lifterActionIDPrefix + ID ));
      
      
      theLogger.trace( "t: " + t )
      
      
      theLogger.trace( "configControlID: " + configControlID )
        
      // If the command is not targeting lifter, ignore it
      if( configControlID != null && 
         configControlID.getAbsUriString.startsWith(lifterConfigIDPrefix) ) {
        
        
        // This is for backwards compatibility 
        // may be removed once RDF data is updated
        var checkedID = ID;
        if(checkedID.equals(ActionStrings.DEFAULT_REGISTRATION)) {
          checkedID = ActionStrings.STUDENT_REGISTRATION;
        }
        
        theLogger.info(
          "Lifter targeted action detected for user: " + checkedID )
        
        // Pull the registration for this user
        val sessionID: String = 
          LifterClientRegistration.getLifterSession(checkedID)
        
        theLogger.trace( "sessionID: " + sessionID )
        
        // If this user is not registered, ignore command
        if(sessionID == null) return ConsumpStatus.IGNORED
        
        theLogger.info( "User " +
                       checkedID +
                       " is registered, sending command." )
        
        // Send to lifter
        pushPage(configControlID, sessionID); 
        
          theLogger.info( "Pushed " + 
                         configControlID +
                         " to user " +
                         checkedID )    
        
        return ConsumpStatus.USED;
      }
      
      // Also check for registration events
      val registrationAction:Ident = t.getAsIdent(
        ActionStrings.LIFTER_ACTION);
      
      if( registrationAction != null ) {
        
        
        // Send to lifter
        val registrationSession:String = t.getAsString(
          ActionStrings.LIFTER_SESSION)
      
        if( registrationSession != null ) {
          theLogger.info( "Lifter registration action detected for user: " + ID )
          pushPage(
            LifterClientRegistration.mapRegistrationIDsToStartPageURIs(ID),
            registrationSession);   
          theLogger.info( "Pushed " + 
                         LifterClientRegistration.mapRegistrationIDsToStartPageURIs(ID) +
                         " to user " +
                         ID )     
          return ConsumpStatus.USED;
        }
      }
    }
    return ConsumpStatus.IGNORED
  }
  
//  def checkForLifterPageCommand(): = Boolean (
//  ) {
//    //
//  }
//    
//  def checkForLifterRegistrationCommand(): = Boolean (
//  ) {
//    //
//  }
    
  /**
   * Send a page in response to the command
   */
  def pushPage(configControlID: Ident, sessionID: String) {
    PageCommander.getLiftAmbassador.activateControlsFromUri(
      sessionID, configControlID)
  }
}
