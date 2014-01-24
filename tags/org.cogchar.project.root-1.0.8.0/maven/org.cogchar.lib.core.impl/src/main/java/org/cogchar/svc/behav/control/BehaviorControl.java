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
package org.cogchar.svc.behav.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.WantsThingAction;
import org.cogchar.name.web.WebUserActionNames;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.cogchar.api.thing.TypedValueMap;

/**
 * Facilitates the control of scenes via lifter. Uses a ThingAction-provided key
 * to perform a lookup on a pair of ActionCallbackMaps, providing a convenient
 * trigger mechanism for events.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class BehaviorControl implements WantsThingAction {

    static int mySceneEventIdCounter = 0;
    Logger theLogger = LoggerFactory.getLogger(BehaviorControl.class);
    // Maps containing references to the code to be triggered.
    private ActionCallbackMap mySceneActionCallbackMap;
    private ActionCallbackMap myAdminActionCallbackMap;

    /**
     * Instance a BehaviorControl.
     *
     * @param sceneActionCallbackMap URI's mapped to playable scenes
     * @param adminActionCallbackMap URI's mapped to administrator actions
     */
    public BehaviorControl(
            ActionCallbackMap sceneActionCallbackMap,
            ActionCallbackMap adminActionCallbackMap) {

        mySceneActionCallbackMap = sceneActionCallbackMap;
        myAdminActionCallbackMap = adminActionCallbackMap;
    }

    /**
     * Perform the action referenced by the ThingAction, if any.
     *
     * @param actionSpec ThingAction that carries target data
     * @param srcGraphID Source that provided the ThingAction (extraneous)
     * @return Status of the TA after processing
     */
    @Override
    public ConsumpStatus consumeAction(
            ThingActionSpec actionSpec,
            Ident srcGraphID) {

        if (actionSpec == null) {
            theLogger.warn("Null actionSpec passed to BehaviorControl for consumption.");
            return ConsumpStatus.IGNORED;
        }
        if (srcGraphID == null) {
            theLogger.warn("Null srcGraphID passed to BehaviorControl for consumption.");
            return ConsumpStatus.IGNORED;
        }
        theLogger.trace("BehaviorControl is considering action {}", actionSpec);

        // Fire an event specified by the URI of the ThingAction.
        TypedValueMap paramTVM = actionSpec.getParamTVM();
        if (paramTVM == null) {
            theLogger.warn("Null param TVM found in actionSpec passed to BehaviorControl for consumption.");
            return ConsumpStatus.IGNORED;
        }

        // Ensures TA is relevant.
        Ident sceneNameID = paramTVM
                .getAsIdent(WebUserActionNames.ACTION);
        if( sceneNameID == null ) {
            theLogger.trace("Did not find Ident on actionSpec for key: " + WebUserActionNames.ACTION.getAbsUriString() );
            return ConsumpStatus.IGNORED;
        }
        String sceneName = sceneNameID.getLocalName();

        if (myAdminActionCallbackMap != null) {
            // Pull a admin action from the ACBM, if one with given name exists
            ActionListener adminActionListener = 
                    myAdminActionCallbackMap.getActionCallback(sceneName);
            if (adminActionListener != null) {
                // Handle admin action.
                ActionEvent event =
                        new ActionEvent(this, mySceneEventIdCounter++, "");
                adminActionListener.actionPerformed(event);
                theLogger.info("BehaviorControl is consuming admin action {}", actionSpec);
                return ConsumpStatus.CONSUMED;
            }
        } if ( mySceneActionCallbackMap != null ) {
            // Pull a scene from the ACBM, if one with given name exists
            ActionListener sceneActionListener = 
                    mySceneActionCallbackMap.getActionCallback(sceneName);
            if (sceneActionListener != null) {
                // Handle Scene Trigger action.
                ActionEvent event = new ActionEvent(
                        this,
                        mySceneEventIdCounter++,
                        "");
                sceneActionListener.actionPerformed(event);
                theLogger.info("BehaviorControl is consuming scene action {}", actionSpec);
                return ConsumpStatus.CONSUMED;
            }
        }

        // Ignore any ThingAction not explicitly used
        theLogger.trace(
                "BehaviorControl ignored action {}", actionSpec);
        return ConsumpStatus.IGNORED;
    }

    public void setMySceneActionCallbackMap(ActionCallbackMap mySceneActionCallbackMap) {
        this.mySceneActionCallbackMap = mySceneActionCallbackMap;
    }

    public void setMyAdminActionCallbackMap(ActionCallbackMap myAdminActionCallbackMap) {
        this.myAdminActionCallbackMap = myAdminActionCallbackMap;
    }
}
