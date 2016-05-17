package org.cogchar.render.rendtest;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.render.goody.basic.BasicGoodyCtx;
import org.cogchar.api.thing.SerTypedValueMap;
import org.cogchar.api.thing.WantsThingAction;
import org.cogchar.api.vworld.GoodyActionParamWriter;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.impl.thing.fancy.ConcreteTVM;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.goody.GoodyNames;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Owner on 5/15/2016.
 */
public class GoodyTestMsgMaker extends BasicDebugger {

	protected Random myRandomizer = new Random();
	protected Ident myAgentID = new FreeIdent(NamespaceDir.CCRT_NS + "test_agent_LGH");

	private LocalGoodyHarness.GARecipe makeActionTemplate() {
		LocalGoodyHarness.GARecipe garTemplate_AA = new LocalGoodyHarness.GARecipe();

		garTemplate_AA.verbID = GoodyNames.ACTION_CREATE;
		garTemplate_AA.colorG = garTemplate_AA.colorA = 0.6f;
		garTemplate_AA.scaleX = garTemplate_AA.scaleY = garTemplate_AA.scaleZ = 3.0f;
		garTemplate_AA.scalarScale = 1.5f;
		garTemplate_AA.sizeX = garTemplate_AA.sizeY = garTemplate_AA.sizeZ = 10.0f;
		garTemplate_AA.rows = 5;

		return garTemplate_AA;
	}

	private List<BasicThingActionSpec> makeTicTacGrid(Ident entityID) {
		LocalGoodyHarness.GARecipe gar = makeActionTemplate();
		gar.entityID = entityID;
		getLogger().info("********************************************** Make TICTAC GRID");
		// First we CREATE a GRID
		gar.entityTypeID = GoodyNames.TYPE_TICTAC_GRID;
		gar.locX = -8.0f;
		gar.locZ = -5.0f;
		BasicThingActionSpec cmsg = makeActionSpec(gar);

		getLogger().info("********************************************** Extra SET op on same TICTAC GRID");
		// Now we (set some properties on that GRID
		gar.verbID = GoodyNames.ACTION_SET;
		// String removeString = ga.getSpecialString(CLEAR_IDENT);
		BasicThingActionSpec umsg = makeActionSpec(gar);
		return makeMsgList(cmsg, umsg);
	}
	private List<BasicThingActionSpec> makeTicTacMark(Ident entityID) {
		getLogger().info("********************************************** Make TICTAC MARK");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.locX = 1.0f;
		gb.locY = 2.0f;
		gb.locY = 3.0f;
		gb.entityTypeID = GoodyNames.TYPE_TICTAC_MARK;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);
	}
	private List<BasicThingActionSpec> makeBitBox(Ident entityID) {
		getLogger().info("********************************************** Make BITBOX");
		// Now let's CREATE a BIT_BOX

		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.entityTypeID = GoodyNames.TYPE_BIT_BOX;
		gb.locX = -5.0f;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		// ...and finish setting the properties on the BIT_BOX
		gb.verbID = GoodyNames.ACTION_SET;
		BasicThingActionSpec umsg = makeActionSpec(gb);
		return makeMsgList(cmsg, umsg);

	}
	private List<BasicThingActionSpec> makeBox(Ident entityID) {
		getLogger().info("********************************************** Make BOX");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.entityTypeID = GoodyNames.TYPE_BOX;
		gb.locX = 10.0f;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);
	}
	private List<BasicThingActionSpec> makeBitCube(Ident entityID) {
		getLogger().info("********************************************** Make BitCube -- fails because we can't find resource");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.entityTypeID = GoodyNames.TYPE_BIT_CUBE;
		gb.locX = 6.5f;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);
	}
	private List<BasicThingActionSpec> makeFloor(Ident entityID) {
		getLogger().info("********************************************** Make Floor");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_FLOOR;
		gb.locX = -2.0f;
		gb.locZ = -3.0f;
		gb.locY = -4.0f;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);
	}
	private List<BasicThingActionSpec> makeScoreboard(Ident entityID) {
		getLogger().info("********************************************** Make SCOREBOARD");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_SCOREBOARD;
//		gb.locX = 0.1f;
		gb.locX = 50.0f; gb.locY = 400.0f;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);


	}
	private List<BasicThingActionSpec> makeCrosshair(Ident entityID) {
		getLogger().info("********************************************** Make CROSSHAIR");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_CROSSHAIR;
		// gb.locX = 0.7f; gb.locY = 0.2f;
		gb.locX = 150.0f; gb.locY = 350.0f;
		gb.scaleX = 5.0f;
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);

	}
	private List<BasicThingActionSpec> makeText(Ident entityID) {
		getLogger().info("********************************************** Make TEXT");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_TEXT;  // Currently creates a ParagraphGoody, which does
		// NOT use fractional screen-size

		// gb.locX = 10.0f; gb.locY = 10.0f;
		gb.locX = 201.0f; gb.locY = 198.0f;
		gb.text = "Oh yes indeedy! XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		BasicThingActionSpec cmsg = makeActionSpec(gb);
		return makeMsgList(cmsg);

	}


	// The visual effects of this work are not shown until the method returns.
	public List<BasicThingActionSpec> makeGoodyCreationMsgs() throws Throwable {

//		LocalGoodyHarness lgh = new LocalGoodyHarness(bgc);

		LocalGoodyHarness.GARecipe garTemplate_AA = makeActionTemplate();

		int gbi = 0;

		List<BasicThingActionSpec> msgs = new ArrayList<BasicThingActionSpec>();

		msgs.addAll(makeTicTacGrid(makeIdentForIdx(gbi++)));

		msgs.addAll(makeTicTacMark(makeIdentForIdx(gbi++)));

		msgs.addAll(makeBitBox(makeIdentForIdx(gbi++)));

		msgs.addAll(makeBox(makeIdentForIdx(gbi++)));

		msgs.addAll(makeBitCube(makeIdentForIdx(gbi++)));

		msgs.addAll(makeFloor(makeIdentForIdx(gbi++)));

		// 2D Goodies
		msgs.addAll(makeScoreboard(makeIdentForIdx(gbi++)));

		msgs.addAll(makeCrosshair(makeIdentForIdx(gbi++)));

		msgs.addAll(makeText(makeIdentForIdx(gbi++)));

		return msgs;

/*		// Hominoid entities
		getLogger().info("********************************************** Make AVATAR-link");
		gb = garBlock[++gbi];
		gb.entityTypeID = GoodyNames.TYPE_AVATAR;
		gb.locX = 12.0f; gb.locY = 3.0f;
		lgh.makeActionSpecAndSend(gb);

		// Camera entities
		getLogger().info("********************************************** Make CAMERA-link");
		gb = garBlock[++gbi];
		gb.entityTypeID = GoodyNames.TYPE_CAMERA;
		gb.locX = -7.0f; gb.locY = -3.0f;
		lgh.makeActionSpecAndSend(gb);
*/
		//	GoodyRenderTestContent grtc = new GoodyRenderTestContent();
		// GoodySpace gSpace = getGoodySpace();
		// hrwMapper.addHumanoidGoodies(veActConsumer, hrc);
	}
	public BasicThingActionSpec makeActionSpec(LocalGoodyHarness.GARecipe gar) {
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter paramWriter = new GoodyActionParamWriter(btvm);
		gar.writeToMap(paramWriter);
		String mintedInstIdPrefix = NamespaceDir.CCRT_NS + "minted_";
		Ident actRecID = mintInstanceID(mintedInstIdPrefix);

		Ident srcAgentID = myAgentID;
		Long postedTStampMsec = System.currentTimeMillis();

		SerTypedValueMap valueMap = paramWriter.getValueMap();
		BasicThingActionSpec actionSpec = new BasicThingActionSpec(actRecID,
				gar.entityID, gar.entityTypeID, gar.verbID, srcAgentID, valueMap, postedTStampMsec);

		return actionSpec;
	}
	public List<BasicThingActionSpec> makeMsgList(BasicThingActionSpec oneMsg) {
		List<BasicThingActionSpec> msgList = new ArrayList<BasicThingActionSpec>();
		msgList.add(oneMsg);
		return msgList;
	}
	public List<BasicThingActionSpec> makeMsgList(BasicThingActionSpec msg1, BasicThingActionSpec msg2) {
		List<BasicThingActionSpec> msgList = new ArrayList<BasicThingActionSpec>();
		msgList.add(msg1);
		msgList.add(msg2);
		return msgList;
	}
	public Ident mintInstanceID(String uriPre) {
		int rNum = myRandomizer.nextInt(Integer.MAX_VALUE);
		String uri = uriPre + rNum;
		return new FreeIdent(uri);
	}
	public Ident makeIdentForIdx(int idx) {
		return new FreeIdent(NamespaceDir.CCRT_NS + "ttg_" + idx);
	}
}
