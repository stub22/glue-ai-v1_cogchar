/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author normenhansen
 */
public class DemoBonyWireframeRagdoll // extends SimpleApplication 
		implements ActionListener {
	
	public static final String PULL_RAGDOLL_UP = "Pull ragdoll up";

	private Node myRagDollNode = new Node();
	private Node myShouldersNode;
	private Vector3f myUpForceVec = new Vector3f(0, 200, 0);
	private boolean myApplyForceFlag = false;

	public static void main(String[] args) {

		final DemoBonyWireframeRagdoll doll = new DemoBonyWireframeRagdoll();
		SimpleApplication app = new SimpleApplication() {
			BulletAppState myBulletAppState;
			@Override public void simpleInitApp() {
				myBulletAppState = makePhysicsAppState(stateManager, assetManager, rootNode);
				doll.createDollNodes();
				Node dollNode = doll.getDollNode();
				rootNode.attachChild(dollNode);
				myBulletAppState.getPhysicsSpace().addAll(dollNode);
				registerInputHandlers();
			}
			public void registerInputHandlers() { 
				System.out.println("*******************************Registering mouse button puller-upper");
				inputManager.addMapping(PULL_RAGDOLL_UP, new MouseButtonTrigger(0));
				inputManager.addListener(doll, PULL_RAGDOLL_UP);
			}

			@Override public void simpleUpdate(float tpf) {
				doll.doSimpleUpdate(tpf);
			}
		};
		app.start();
	}
  
    
//    protected AudioRenderer audioRenderer;
//    protected Renderer renderer;
//    protected RenderManager renderManager;
//    protected ViewPort viewPort;
//    protected ViewPort guiViewPort;

//    protected JmeContext context;
//    protected AppSettings settings;
//    protected Timer timer;
//    protected Camera cam;
//    protected Listener listener;
 //   protected MouseInput mouseInput;
 //   protected KeyInput keyInput;
 //   protected JoyInput joyInput;
 //   protected TouchInput touchInput;
 //   protected InputManager inputManager;

	public static BulletAppState makePhysicsAppState(AppStateManager stateManager, AssetManager assetManager, Node rootNode) {
		System.out.println("************************************************************ makePhysicsAppState");
		BulletAppState bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		bulletAppState.getPhysicsSpace().enableDebug(assetManager);
		JJPhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
		return bulletAppState;
	}
	private void createDollNodes() {
		myShouldersNode = createLimb(0.2f, 1.0f, new Vector3f(0.00f, 1.5f, 0), true);
		Node uArmL = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, 0.8f, 0), false);
		Node uArmR = createLimb(0.2f, 0.5f, new Vector3f(0.75f, 0.8f, 0), false);
		Node lArmL = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, -0.2f, 0), false);
		Node lArmR = createLimb(0.2f, 0.5f, new Vector3f(0.75f, -0.2f, 0), false);
		Node body = createLimb(0.2f, 1.0f, new Vector3f(0.00f, 0.5f, 0), false);
		Node hips = createLimb(0.2f, 0.5f, new Vector3f(0.00f, -0.5f, 0), true);
		Node uLegL = createLimb(0.2f, 0.5f, new Vector3f(-0.25f, -1.2f, 0), false);
		Node uLegR = createLimb(0.2f, 0.5f, new Vector3f(0.25f, -1.2f, 0), false);
		Node lLegL = createLimb(0.2f, 0.5f, new Vector3f(-0.25f, -2.2f, 0), false);
		Node lLegR = createLimb(0.2f, 0.5f, new Vector3f(0.25f, -2.2f, 0), false);

		join(body, myShouldersNode, new Vector3f(0f, 1.4f, 0));
		join(body, hips, new Vector3f(0f, -0.5f, 0));

		join(uArmL, myShouldersNode, new Vector3f(-0.75f, 1.4f, 0));
		join(uArmR, myShouldersNode, new Vector3f(0.75f, 1.4f, 0));
		join(uArmL, lArmL, new Vector3f(-0.75f, .4f, 0));
		join(uArmR, lArmR, new Vector3f(0.75f, .4f, 0));

		join(uLegL, hips, new Vector3f(-.25f, -0.5f, 0));
		join(uLegR, hips, new Vector3f(.25f, -0.5f, 0));
		join(uLegL, lLegL, new Vector3f(-.25f, -1.7f, 0));
		join(uLegR, lLegR, new Vector3f(.25f, -1.7f, 0));

		myRagDollNode.attachChild(myShouldersNode);
		myRagDollNode.attachChild(body);
		myRagDollNode.attachChild(hips);
		myRagDollNode.attachChild(uArmL);
		myRagDollNode.attachChild(uArmR);
		myRagDollNode.attachChild(lArmL);
		myRagDollNode.attachChild(lArmR);
		myRagDollNode.attachChild(uLegL);
		myRagDollNode.attachChild(uLegR);
		myRagDollNode.attachChild(lLegL);
		myRagDollNode.attachChild(lLegR);

		// rootNode.attachChild(ragDoll);
		// bulletAppState.getPhysicsSpace().addAll(ragDoll);
	}

	public Node getDollNode() {
		return myRagDollNode;
	}

	private Node createLimb(float width, float height, Vector3f location, boolean rotate) {
		int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
		CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);
		Node node = new Node("Limb");
		RigidBodyControl rigidBodyControl = new RigidBodyControl(shape, 1);
		node.setLocalTranslation(location);
		node.addControl(rigidBodyControl);
		return node;
	}

	private PhysicsJoint join(Node A, Node B, Vector3f connectionPoint) {
		Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
		Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());
		ConeJoint joint = new ConeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB);
		joint.setLimit(1f, 1f, 0);
		return joint;
	}

	public void onAction(String string, boolean bln, float tpf) {
		if (PULL_RAGDOLL_UP.equals(string)) {
			if (bln) {
				myShouldersNode.getControl(RigidBodyControl.class).activate();
				myApplyForceFlag = true;
			} else {
				myApplyForceFlag = false;
			}
		}
	}

	public void doSimpleUpdate(float tpf) {
		if (myApplyForceFlag) {
			myShouldersNode.getControl(RigidBodyControl.class).applyForce(myUpForceVec, Vector3f.ZERO);
		}
	}
}
