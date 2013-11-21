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

class LifterClientRegistration extends WantsThingAction {
    
  //http://www.cogchar.org/lift/action/registration#
    
  private val lifterStudentRegistrationActionID : Ident =
    new FreeIdent(
      "http://www.cogchar.org/lift/action/registration#student");
  
//  private val lifterFacilitatorRegistrationActionID : Ident =
//    new FreeIdent(
//      "http://www.cogchar.org/lift/action/registration#facilitator");
    
    private val lifterActionID : Ident = 
            new FreeIdent("http://www.cogchar.org/lift/user/action#action");

  private val theStudentRegistrationLocalName : String = lifterStudentRegistrationActionID.getLocalName;
//  private val theFacilitatorRegistrationLocalName : String = lifterFacilitatorRegistrationActionID.getLocalName;
  
  private val lifterSessionID :Ident = 
    new FreeIdent("http://www.cogchar.org/lift/user/action#session");

  @Override
  def consumeAction(
    actionSpec:ThingActionSpec, srcGraphID:Ident ): ConsumpStatus = {

    val t:TypedValueMap = actionSpec.getParamTVM();
    if(t.getAsIdent(lifterActionID) == null) {
      return ConsumpStatus.IGNORED;
    }
    else{
      // This ensures the TA is a student registration
      val action :Ident = t.getAsIdent(lifterActionID);
      if( action.getLocalName().equals(
          theStudentRegistrationLocalName)) {
                
        // register the student's tablet's session
        LifterClientRegistration.studentSessionID = 
          t.getAsString(lifterSessionID);
        return ConsumpStatus.CONSUMED;
      }
      return ConsumpStatus.IGNORED;
    }
  }
}

object LifterClientRegistration {
  
  def getCurrentStudentLifterSession(): String = {
    return LifterClientRegistration.studentSessionID;
  }
  
  var studentSessionID : String = null;
}
