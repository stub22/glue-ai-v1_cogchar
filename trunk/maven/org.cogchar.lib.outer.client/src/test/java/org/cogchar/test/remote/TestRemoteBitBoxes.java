/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.test.remote;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
import org.cogchar.api.vworld.GoodyActionParamWriter;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.impl.thing.fancy.ConcreteTVM;
import org.cogchar.impl.thing.fancy.FancyThingModelWriter;
import org.slf4j.Logger;

import java.util.Random;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.outer.client.AgentRepoClient;
import org.cogchar.outer.client.TestOuterClientSOH;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class TestRemoteBitBoxes  extends BasicDebugger {
	AgentRepoClient	myAgentRepoClient = new AgentRepoClient();

	static float TEST_INIT_X = 30.0f, TEST_INIT_Y = 15.0f, TEST_INIT_Z = 10.0f;
	public static void main(String[] args) {
		TestRemoteBitBoxes tester = new TestRemoteBitBoxes();
		tester.doBitBoxTest(0);
	}
	public TestRemoteBitBoxes() {
		forceLog4jConfig();
	}
	private static String goodyGraphQN = "ccrt:thing_sheet_22";
	private static String boxBaseURI = "http://dummy.org/bitbox#num_";
	
	public void doBitBoxTest(int outerLoopCount) {
		Random ran = new Random();
		Ident boxOneID = makeOneBitBox(ran, true);
		for (int outer = 0; outer < outerLoopCount; outer++) {
			for (int i = 0; i < 100 ; i++) {
				getLogger().info("Starting loop #" + outer + "." + i);
				float disp = i / 10.0f;
				updateGoodyLocation(boxOneID, TEST_INIT_X + disp, TEST_INIT_Y + disp, TEST_INIT_Z + disp, ran, false);
			}
		}
	}
	public Ident makeOneBitBox(Random ran, boolean debugFlag) {
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter gapw = new GoodyActionParamWriter(btvm);
		
		gapw.putLocation(TEST_INIT_X, TEST_INIT_Y, TEST_INIT_Z);
		gapw.putRotation(1.0f, 1.0f, 1.0f, 10.0f);
		//gapw.putSize(4f, 0f, 0f);
		gapw.putScale(4f);
		
		Ident dummyBoxID = new FreeIdent(boxBaseURI + System.currentTimeMillis());

		sendBitBoxTAS(dummyBoxID, GoodyNames.ACTION_CREATE, btvm, ran, debugFlag);
		return dummyBoxID;
	}
	public void updateGoodyLocation(Ident goodyID, float locX, float locY, float locZ, Random ran, boolean debugFlag) {
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter gapw = new GoodyActionParamWriter(btvm);
		
		gapw.putLocation(locX, locY, locZ);
		
		sendBitBoxTAS(goodyID, GoodyNames.ACTION_MOVE, btvm, ran, debugFlag);
	}
	public void sendBitBoxTAS(Ident tgtThingID, Ident verbID, TypedValueMap paramTVMap, Random ran, boolean debugFlag) {
		Ident actRecID = new FreeIdent("action_#" + ran.nextInt());
		Ident tgtThingTypeID = GoodyNames.TYPE_BIT_BOX;
		Ident srcAgentID = null;
		Long postedTStampMsec = System.currentTimeMillis();
		BasicThingActionSpec btas = new BasicThingActionSpec(actRecID, tgtThingID, tgtThingTypeID, verbID, srcAgentID, paramTVMap, postedTStampMsec);	
		sendThingActionSpec(btas, ran, debugFlag);
	}
	public void sendThingActionSpec(ThingActionSpec actionSpec, Random ran, boolean debugFlag) {
		Logger log = getLogger();
		log.info("Sending action spec: " + actionSpec);

		FancyThingModelWriter ftmw = new FancyThingModelWriter();
		String updateTxt = ftmw.writeTASpecToString(actionSpec, goodyGraphQN, ran);
		
		// logInfo("UpdateTxt:\n" + updateTxt);
		myAgentRepoClient.execRemoteSparqlUpdate(TestOuterClientSOH.glueUpdURL, updateTxt, debugFlag);
	}
}
