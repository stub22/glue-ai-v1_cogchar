package org.cogchar.render.goody.basic;


import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.CameraBinding;
import org.cogchar.render.app.entity.GoodyActionExtractor;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.app.entity.VWorldEntityReg;
import org.cogchar.render.goody.bit.BitBox;
import org.cogchar.render.goody.bit.BitCube;
import org.cogchar.render.goody.bit.TicTacGrid;
import org.cogchar.render.goody.bit.TicTacMark;
import org.cogchar.render.goody.flat.CrossHairGoody;
import org.cogchar.render.goody.flat.ParagraphGoody;
import org.cogchar.render.goody.flat.ScoreBoardGoody;
import org.cogchar.render.opengl.scene.DeepSceneMgr;
import org.cogchar.render.optic.goody.VWorldCameraEntity;


import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.WantsThingAction.ConsumpStatus;

import java.awt.*;
import java.util.concurrent.Callable;

import com.jme3.scene.Node;
import org.cogchar.name.goody.GoodyNames;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.window.WantsWindowStatus;
import org.cogchar.render.sys.window.WindowStatusMonitor;
import scala.Option;

/**
 * Created by Stub22 on 5/14/2016.
 */
public class BasicGoodyCtxImpl extends BasicDebugger implements BasicGoodyCtx, WantsWindowStatus {
	private RenderRegistryClient 		myRRC;
	private VWorldEntityReg				myVWER  = new VWorldEntityReg();
	private Dimension					myScreenDimension;
	private Node 						myTopGoodyNode = null;

	public BasicGoodyCtxImpl(RenderRegistryClient rrc, WindowStatusMonitor wsm) {//  GoodyModularRenderContext gmrc) {
		myRRC = rrc;
		// attachRootGoodyNode();    Removed 2016-09-03 by stub22
		wsm.addListener(this);
		Dimension winSzOrNull = wsm.getWindowSize();
		if (winSzOrNull != null) {
			getLogger().info("Found initial window size, applying: {}", winSzOrNull);
			applyNewScreenDimension(winSzOrNull);
		} else {
			getLogger().warn("No initial window size found");
		}
	}
	@Override public void setupAsMainGoodyCtx() {
		attachTopGoodyNode();
	}
	@Override public RenderRegistryClient getRRC() {
		return myRRC;
	}

	@Override public void applyNewScreenDimension(Dimension newDimension) {
		myScreenDimension = newDimension;
		// Notify goodies of new dimensions
		for (VWorldEntity aGoody : myVWER.getAllGoodies()) {
			aGoody.applyScreenDimension(myScreenDimension);
		}
	}

	@Override public Dimension getScreenDimension() {
		return myScreenDimension;
	}

	// Added 2016-09-03
	public void setTopGoodyNode(Node tgn) {
		// TODO:  Check for old topGoodyNode, != new one, and if found log-warn and remove it.
		if ((myTopGoodyNode != null) && (myTopGoodyNode != tgn)) {
			getLogger().warn("Found old myTopGoodyNode={}, replacing with new one={}", myTopGoodyNode, tgn);
		}
		myTopGoodyNode = tgn;
		attachTopGoodyNode();
	}
	// Renamed and moved to protected scope, 2016-09-03
	protected void attachTopGoodyNode() {
		final Node topGoodyNode = getTopGoodyNode();
		getLogger().info("Queueing attachment for deep topGoodyNode={}", topGoodyNode);
		final DeepSceneMgr dsm = myRRC.getSceneDeepFacade(null);
		myRRC.getWorkaroundAppStub().enqueue(new Callable<Void>() { // Must manually do this on main render thread, ah jMonkey...

			@Override
			public Void call() throws Exception {
				dsm.attachTopSpatial(topGoodyNode);
				return null;
			}
		});
	}

	protected Node getTopGoodyNode() {
		if (myTopGoodyNode == null) {
			myTopGoodyNode = new Node("bgciGoodyNode");
		}
		return myTopGoodyNode;
	}

	@Override public VWorldEntityReg getVWER() {
		return myVWER;
	}


	// This way, EntitySpace doesn't need to know about the root node to attach. But this pattern can change if
	// we decide we rather it did!
	public VWorldEntity createAndAttachByAction(GoodyActionExtractor ga, VWorldEntity.QueueingStyle qStyle) {
		Node topGoodyNode = getTopGoodyNode();
		VWorldEntity newGoody = createByAction(ga);
		if (newGoody != null) {
			newGoody.attachToVirtualWorldNode(topGoodyNode, qStyle);
		}
		return newGoody;
	}

	// Marked protected 2016-09-03, was private before.
	protected VWorldEntity createByAction(GoodyActionExtractor ga) {
		VWorldEntity novGoody = null;
		if (ga.getKind() == GoodyActionExtractor.Kind.CREATE) {
			// Switch on string local name would be nice
			// This is getting out of hand
			// Big problem here is that GoodyFactory needs to know about each Goody type and how to make them
			// Ripe for refactoring to avoid that, perhaps via a Chain of Responsibility pattern?
			// Or perhaps we would like to pass the GoodyActionExtractor to the goodies in their constructors
			// Still would need a way (possibly reflection?) to get goody class from type
			try {
				//theLogger.info("Trying to create a goody, type is {}", ga.getType()); // TEST ONLY
				Vector3f scaleVec = ga.getScaleVec3f();
				Float scaleUniform = ga.getScaleUniform();
				if ((scaleVec == null) && (scaleUniform != null)) {
					scaleVec = new Vector3f(scaleUniform, scaleUniform, scaleUniform);
				}
				Vector3f locVec = ga.getLocationVec3f();
				Quaternion rotQuat = ga.getRotationQuaternion();
				Ident goodyID = ga.getGoodyID();
				Ident goodyType = ga.getType();
				ColorRGBA gcolor = ga.getColorOrDefault();
				String goodyText = ga.getText();
				Boolean bitBoxState = ga.getSpecialBoolean(GoodyNames.BOOLEAN_STATE);
				Boolean isAnO =  ga.getSpecialBoolean(GoodyNames.USE_O);
				Integer rowCount = ga.getSpecialInteger(GoodyNames.ROWS);

				BasicGoodyCtx bgc = this;

				if (GoodyNames.TYPE_BIT_BOX.equals(goodyType)) {
					novGoody = new BitBox(bgc, goodyID, locVec, rotQuat, scaleVec, bitBoxState);
				} else if (GoodyNames.TYPE_BIT_CUBE.equals(goodyType)) {
					novGoody = new BitCube(bgc, goodyID, locVec, rotQuat, scaleVec, bitBoxState);
				} else if (GoodyNames.TYPE_FLOOR.equals(goodyType)) {
					// Assuming physical floor for now, but that may be a good thing to define in repo
					novGoody = new VirtualFloor(bgc, ga.getGoodyID(), locVec, gcolor, true);
				} else if (GoodyNames.TYPE_TICTAC_MARK.equals(goodyType)) {
					novGoody = new TicTacMark(bgc, goodyID, locVec, rotQuat, scaleVec, isAnO);
				} else if (GoodyNames.TYPE_TICTAC_GRID.equals(goodyType)) {
					novGoody = new TicTacGrid(bgc, goodyID, locVec, rotQuat, gcolor, scaleVec);
				} else if (GoodyNames.TYPE_BOX.equals(goodyType)) {
					novGoody = new GoodyBox(bgc, goodyID, locVec, rotQuat, gcolor, scaleVec);
				} else if (GoodyNames.TYPE_CROSSHAIR.equals(goodyType)) {
					// Flat goody uses different parentNode attachment.
					novGoody = new CrossHairGoody(bgc, goodyID, locVec, scaleUniform);
				} else if (GoodyNames.TYPE_SCOREBOARD.equals(goodyType)) {
					// Flat goody uses different parentNode attachment.
					float sizeX = ga.getSizeVec3D()[0];
					float rowHeight = sizeX;
					float textSize = scaleUniform;
					getLogger().info("Scoreboard row count=" + rowCount + ", rowHeight=" + rowHeight
							+ ", textSize=" + textSize+ ", locVec=" + locVec);

					novGoody = new ScoreBoardGoody(bgc, goodyID, locVec, rowHeight, rowCount, textSize);

				} else if (GoodyNames.TYPE_TEXT.equals(goodyType)) {
					// Flat goody uses different parentNode attachment.
					// scale.getX() should return scalarScale if that is provided, or use Robosteps API scalar scale which
					// is represented as a vector scale with identical components
					novGoody = new ParagraphGoody(bgc, goodyID, locVec, scaleVec.getX(), gcolor, goodyText);

				} else if (GoodyNames.TYPE_CAMERA.equals(goodyType)) {
					Ident cameraUri = goodyID;
					if (myVWER.getGoody(cameraUri) == null) { //Otherwise this camera wrapper is already created
						getLogger().info("Adding a VWorldCameraEntity for {}", cameraUri);
						CameraBinding camBinding = myRRC.getOpticCameraFacade(null).getCameraBinding(cameraUri);
						if (camBinding != null) {
							Camera cam = camBinding.getCamera();
							if (cam != null) {
								novGoody = (new VWorldCameraEntity(bgc, cameraUri, cam));
							} else {
								throw new RuntimeException("No actual camera found in binding at " + cameraUri);
							}
						}
						else {
							getLogger().warn("Couldn't find camera with URI {} for goody", cameraUri);
						}
					}
				} else {
					getLogger().warn("Did not recognize requested goody type for creation: {}", ga.getType());
				}
			} catch (Exception e) {
				getLogger().error("Error attempting to create goody {}", ga.getGoodyID(), e);
			}
		} else {
			getLogger().warn("GoodyFactory received request to add a goody, but the GoodyAction kind was not CREATE! Goody URI: {}",
					ga.getGoodyID());
		}
		return novGoody;
	}
	@Override public ConsumpStatus consumeAction(ThingActionSpec actionSpec) {
		getLogger().info("The targetThingType is {}", actionSpec.getTargetThingTypeID()); // TEST ONLY

		// How do we decide whether it's really a VWorld / Goody action?
		// Below, the targetThing is presumed to be a "goody", either existing or new.
		GoodyActionExtractor ga = new GoodyActionExtractor(actionSpec);
		Ident gid = ga.getGoodyID();
		VWorldEntity goodyOne = myVWER.getGoody(gid);

		GoodyActionExtractor.Kind kind = ga.getKind();
		getLogger().info("The kind of Goody inspected is {}", kind); // TEST ONLY
		if (kind != null) {
			switch (ga.getKind()) {
				case CREATE: { // If it's a CREATE action, we will do some different stuff
					if (myVWER.hasGoodyAt(gid)) {
						getLogger().warn("Goody already created! Ignoring additional creation request for goody: {}", gid);
					} else {

						goodyOne = createAndAttachByAction(ga, VWorldEntity.QueueingStyle.QUEUE_AND_RETURN);
						if (goodyOne != null) {
							myVWER.addGoody(goodyOne);
							return ConsumpStatus.USED;
						}
					}
					break;
				}
				case DELETE: {
					if (!myVWER.hasGoodyAt(gid)) {
						getLogger().warn("Could not delete goody because it does not exist: {}", gid);
					} else {
						myVWER.removeGoody(goodyOne);
						return ConsumpStatus.USED;
					}
 					break;
				}
				default: {
					// For the moment, let's focus on "update"
					try {
						// Now - apply the action to goodyOne
						goodyOne.applyAction(ga, VWorldEntity.QueueingStyle.QUEUE_AND_RETURN);
						return ConsumpStatus.USED;
					} catch (Exception e) {
						getLogger().warn("Problem attempting to update goody with URI: {}", gid, e);
					}
				}
			}
		}
		return ConsumpStatus.IGNORED;
	}

	@Override public void notifyWindowSize(Dimension size) {

		applyNewScreenDimension(size);
	}
}
