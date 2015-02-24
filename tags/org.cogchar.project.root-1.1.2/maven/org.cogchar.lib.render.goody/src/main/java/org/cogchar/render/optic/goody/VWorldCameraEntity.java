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
package org.cogchar.render.optic.goody;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl.ControlDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.cinema.AttachedItemType;
import org.cogchar.api.cinema.PathInstanceConfig;
import org.cogchar.api.cinema.WaypointConfig;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.render.app.entity.GoodyActionExtractor;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.goody.basic.BasicGoodyEntity;
import org.cogchar.render.scene.goody.PathMgr;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

/**
 * Much as with HumanoidFigureGoodyWrapper, this is a kludge to allow us to
 * control Cameras from the Robosteps API until the GoodySpace becomes the
 * EntitySpace
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
public class VWorldCameraEntity extends VWorldEntity {

    private Camera myCamera;

    public VWorldCameraEntity(GoodyRenderRegistryClient aRenderRegCli, Ident cameraUri, Camera theCamera) {
        super(aRenderRegCli, cameraUri);
        myCamera = theCamera;
    }

    @Override
    public void setPosition(final Vector3f position, QueueingStyle qStyle) {
        enqueueForJme(new Callable() { // Do this on main render thread
            @Override
            public Void call() throws Exception {
                myCamera.setLocation(position);
                return null;
            }
        }, qStyle);
    }

    public void setRotation(final Quaternion rotation, QueueingStyle qStyle) {
        enqueueForJme(new Callable() { // Do this on main render thread
            @Override
            public Void call() throws Exception {
                myCamera.setRotation(rotation);
                return null;
            }
        }, qStyle);
    }

    protected void setNewPositionAndRotationIfNonNull(Vector3f newPosition, Quaternion newRotation, QueueingStyle qStyle) {
        if (newPosition != null) {
            setPosition(newPosition, qStyle);
        }
        if (newRotation != null) {
            setRotation(newRotation, qStyle);
        }
    }

    protected void moveViaPath(Vector3f newPosition, Quaternion newOrientation, float duration) {
        Vector3f currentPosition = myCamera.getLocation();
        Quaternion currentOrientation = myCamera.getRotation();
        if (newPosition == null) {
            //newPosition = currentPosition; // Eventually this should work, but...
            getLogger().warn("No new position specified for Camera MOVE operation -- currently MOVEs of rotation only are not supported.");
            return;
        }
        if (newOrientation == null) {
            newOrientation = currentOrientation;
        }
        List<WaypointConfig> waypoints = new ArrayList<WaypointConfig>();
        waypoints.add(new WaypointConfig(new FreeIdent(NamespaceDir.NS_CCRT_RT + "Start"), currentPosition.toArray(new float[3])));
        waypoints.add(new WaypointConfig(new FreeIdent(NamespaceDir.NS_CCRT_RT + "End"), newPosition.toArray(new float[3])));
        Ident pathUri = new FreeIdent(NamespaceDir.NS_CCRT_RT + "CamMovePath");
        Vector3f lookAtLocation = getLookAtLocation(newPosition, newOrientation);
        PathInstanceConfig cameraPath = new PathInstanceConfig(getUri(), AttachedItemType.CAMERA,
                duration, lookAtLocation.toArray(new float[3]), waypoints, pathUri);
        PathMgr pMgr = getRenderRegCli().getScenePathFacade(null);
        pMgr.buildAnimation(cameraPath);
        pMgr.controlAnimationByName(pathUri, PathMgr.ControlAction.PLAY);
    }

    /**
     * Returns a Vector3f containing the location that should be looked at while the camera is rotating
     * on a path. This position is determined by taking the new rotation and the new position, and choosing
     * a spot that is a normalized 55 distance away from the newPosition in the new direction.
     * 55 is hardcoded for the amazing boxes program and may negatively effect small changes in rotation.
     * If the cameras initial position/rotation had a vector shooting in the direction of it's rotation, and this vector intersected
     * the final position/rotations vector shooting in it's rotation, 55 would be the length of the vector from the final position... 
     * Hardcoded to avoid that complex 3D math. 
     * @param newPosition Position where camera will be at the end of the path.
     * @param orientation The direction the camera should be facing at the end of the path.
     * @return 
     */
    private Vector3f getLookAtLocation(Vector3f newPosition, Quaternion orientation) {
        Vector3f vel = new Vector3f();
        vel = orientation.getRotationColumn(2, vel);
        
        float max = Math.max(Math.abs(vel.getX()), Math.abs(vel.getY()));
        max = Math.max(max, Math.abs(vel.getZ()));
        float normalized = 55.0f/max;
        
        vel.multLocal(normalized);
        Vector3f lookAtLocation = newPosition.clone();
        lookAtLocation.addLocal(vel);
        return lookAtLocation;
    }

    public void attachToGoody(BasicGoodyEntity attachedGoody, QueueingStyle qStyle) {
        //create the camera Node
        final CameraNode camNode = new CameraNode("Camera Node of " + getUri().getLocalName(), myCamera);
        //Get the Goody's Node
        final Node goodyNode = attachedGoody.getContentNode();
        getLogger().info("Attaching camNode to goodyNode {}", attachedGoody.getUri()); // TEST ONLY
        enqueueForJme(new Callable() { // Do this on main render thread
            @Override
            public Void call() throws Exception {
                //Move camNode, e.g. behind and above the target:
                camNode.setLocalTranslation(new Vector3f(0, 5, -5)); // ... for initial hack-up; probably these offsets need to come from GoodyActionExtractor
                //Rotate the camNode to look at the target:
                camNode.lookAt(goodyNode.getLocalTranslation(), Vector3f.UNIT_Y);
                //This mode means that camera copies the movements of the target:
                camNode.setControlDir(ControlDirection.SpatialToCamera);
                //Attach the camNode to the Goody Node:
                goodyNode.attachChild(camNode);
                //And the root node -- required?
                getRenderRegCli().getJme3RootDeepNode(null).attachChild(camNode);
                return null;
            }
        }, qStyle);

        camNode.setEnabled(true);
    }

    // Searches for the URI specified by the input string and attaches to the Goody if found
    // Fairly ugly getting into the list of goodies by GoodyFactory.getTheFactory().getActionConsumer().getGoody
    private void attachToGoody(String goodyUriString, QueueingStyle qStyle) {
        if (goodyUriString != null) {
            Ident goodyUri = new FreeIdent(goodyUriString);
            VWorldEntityActionConsumer consumer = GoodyFactory.getTheFactory().getActionConsumer();
            VWorldEntity goodyToAttach = consumer.getGoody(goodyUri);
            // Can avoid this instanceof code smell by refactoring here or in VWorldEntityActionConsumer;
            // All this stuff needs refactoring in any case to separate cameras out from the "Goody" concept, among many
            // other reasons...
            if ((goodyToAttach != null) && (goodyToAttach instanceof BasicGoodyEntity)) {
                attachToGoody((BasicGoodyEntity) goodyToAttach, qStyle);
            }
        }
    }

    @Override
    public void setUniformScaleFactor(Float scale, QueueingStyle qStyle) {
        getLogger().warn("setScale not supported in CameraGoodyWrapper");
    }

    @Override
    public void applyAction(GoodyActionExtractor ga, QueueingStyle qStyle) {
        Vector3f newLocation = ga.getLocationVec3f();
        Quaternion newRotation = ga.getRotationQuaternion();
        String attachToGoodyUriString = ga.getSpecialString(GoodyNames.ATTACH_TO_GOODY);
        switch (ga.getKind()) {
            case SET: {
                setNewPositionAndRotationIfNonNull(newLocation, newRotation, qStyle);
                attachToGoody(attachToGoodyUriString, qStyle);
                break;
            }
            case MOVE: {
                Float timeEnroute = ga.getTravelTime();
                if (timeEnroute == null) {
                    setNewPositionAndRotationIfNonNull(newLocation, newRotation, qStyle);
                } else {
                    // MoveViaPath's rotation is not perfect... It looks at a specific area, but does not line up it's rotation
                    // By setting the rotation immediately afterwards we get the camera to move via a path, rotate, and line up the rotation perfectly.
                    moveViaPath(newLocation, newRotation, timeEnroute);
                    setNewPositionAndRotationIfNonNull(newLocation, newRotation, qStyle);
                }
                break;
            }
            default: {
                getLogger().error("Unknown action requested in CameraGoodyWrapper {}: {}", getUri().getLocalName(), ga.getKind().name());
            }
        }
    }
;
}
