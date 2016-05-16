package org.cogchar.render.rendtest;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.render.goody.basic.BasicGoodyCtx;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.name.goody.GoodyNames;
/**
 * Created by Owner on 5/15/2016.
 */
public class GoodyTestMsgMaker extends BasicDebugger {
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
	private Ident makeIdentForIdx(int idx) {
		return new FreeIdent(NamespaceDir.CCRT_NS + "ttg_" + idx);
	}
	private void makeTicTacGrid(LocalGoodyHarness lgh, Ident entityID) {
		LocalGoodyHarness.GARecipe gar = makeActionTemplate();
		gar.entityID = entityID;
		getLogger().info("********************************************** Make TICTAC GRID");
		// First we CREATE a GRID
		gar.entityTypeID = GoodyNames.TYPE_TICTAC_GRID;
		gar.locX = -8.0f;
		gar.locZ = -5.0f;
		lgh.makeActionSpecAndSend(gar);

		getLogger().info("********************************************** Extra SET op on same TICTAC GRID");
		// Now we (set some properties on that GRID
		gar.verbID = GoodyNames.ACTION_SET;
		// String removeString = ga.getSpecialString(CLEAR_IDENT);
		lgh.makeActionSpecAndSend(gar);

	}
	private void makeTicTacMark(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make TICTAC MARK");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.locX = 1.0f;
		gb.locY = 2.0f;
		gb.locY = 3.0f;
		gb.entityTypeID = GoodyNames.TYPE_TICTAC_MARK;
		lgh.makeActionSpecAndSend(gb);
	}
	private void makeBitBox(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make BITBOX");
		// Now let's CREATE a BIT_BOX

		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.entityTypeID = GoodyNames.TYPE_BIT_BOX;
		gb.locX = -5.0f;
		lgh.makeActionSpecAndSend(gb);
		// ...and finish setting the properties on the BIT_BOX
		gb.verbID = GoodyNames.ACTION_SET;
		lgh.makeActionSpecAndSend(gb);

	}
	private void makeBox(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make BOX");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.entityTypeID = GoodyNames.TYPE_BOX;
		gb.locX = 10.0f;
		lgh.makeActionSpecAndSend(gb);
	}
	private void makeBitCube(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make BitCube -- fails because we can't find resource");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;
		gb.entityTypeID = GoodyNames.TYPE_BIT_CUBE;
		gb.locX = 6.5f;
		lgh.makeActionSpecAndSend(gb);
	}
	private void makeFloor(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make Floor");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_FLOOR;
		gb.locX = -2.0f;
		gb.locZ = -3.0f;
		gb.locY = -4.0f;
		lgh.makeActionSpecAndSend(gb);
	}
	private void makeScoreboard(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make SCOREBOARD");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_SCOREBOARD;
//		gb.locX = 0.1f;
		gb.locX = 50.0f; gb.locY = 400.0f;
		lgh.makeActionSpecAndSend(gb);

	}
	private void makeCrosshair(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make CROSSHAIR");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_CROSSHAIR;
		// gb.locX = 0.7f; gb.locY = 0.2f;
		gb.locX = 150.0f; gb.locY = 350.0f;
		gb.scaleX = 5.0f;
		lgh.makeActionSpecAndSend(gb);

	}
	private void makeText(LocalGoodyHarness lgh, Ident entityID) {
		getLogger().info("********************************************** Make TEXT");
		LocalGoodyHarness.GARecipe gb = makeActionTemplate();
		gb.entityID = entityID;

		gb.entityTypeID = GoodyNames.TYPE_TEXT;  // Currently creates a ParagraphGoody, which does
		// NOT use fractional screen-size

		// gb.locX = 10.0f; gb.locY = 10.0f;
		gb.locX = 201.0f; gb.locY = 198.0f;
		gb.text = "Oh yes indeedy! XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		lgh.makeActionSpecAndSend(gb);
	}


	// We are on the JME3 thread!
	// The visual effects of this work are not shown until the method returns.
	public void sendGoodyCreationMessages_onJME3Thread(BasicGoodyCtx bgc) throws Throwable {

		LocalGoodyHarness lgh = new LocalGoodyHarness(bgc);

		LocalGoodyHarness.GARecipe garTemplate_AA = makeActionTemplate();

		int gbi = 0;

		makeTicTacGrid(lgh, makeIdentForIdx(gbi++));

		makeTicTacMark(lgh, makeIdentForIdx(gbi++));

		makeBitBox(lgh, makeIdentForIdx(gbi++));

		makeBox(lgh, makeIdentForIdx(gbi++));

		makeBitCube(lgh, makeIdentForIdx(gbi++));

		makeFloor(lgh, makeIdentForIdx(gbi++));

		// 2D Goodies
		makeScoreboard(lgh, makeIdentForIdx(gbi++));

		makeCrosshair(lgh, makeIdentForIdx(gbi++));

		makeText(lgh, makeIdentForIdx(gbi++));

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

}
