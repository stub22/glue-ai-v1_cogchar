/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.outer.client;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicThingActionSpec;
import org.cogchar.render.model.goodies.GoodyActionParamWriter;
import org.cogchar.render.model.goodies.GoodyNames;
import org.cogchar.api.thing.BasicTypedValueMap;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.impl.thing.ConcreteTVM;
import org.cogchar.impl.thing.FancyThingModelWriter;
import org.slf4j.Logger;

import java.util.Random;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class TestRemoteBitBoxes  extends BasicDebugger {
	AgentRepoClient	myAgentRepoClient = new AgentRepoClient();

	public static void main(String[] args) {
		Random ran = new Random();
		TestRemoteBitBoxes test = new TestRemoteBitBoxes();
		Ident boxOneID = test.makeOneBitBox(ran);
		test.updateGoodyLocation(boxOneID, 700.0f, 800.0f, 900.0f, ran);
		
	}
	public TestRemoteBitBoxes() {
		forceLog4jConfig();
	}
	private static String goodyGraphQN = "ccrt:thing_sheet_22";
	private static String boxBaseURI = "http://dummy.org/bitbox#num_";
	
	public Ident makeOneBitBox(Random ran) {
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter gapw = new GoodyActionParamWriter(btvm);
		
		gapw.putType(GoodyNames.TYPE_BIT_BOX);
		gapw.putLocation(44.0f, 55.0f, 66.0f);
		gapw.putRotation(1.0f, 1.0f, 1.0f, 90.0f);
		
		long tstamp = System.currentTimeMillis();
		String dummyBoxURI = boxBaseURI + tstamp;
		Ident dummyBoxID = new FreeIdent(dummyBoxURI);
		
		Ident actRecID = new FreeIdent("action_#" + ran.nextInt());
		Ident tgtThingID = dummyBoxID;
		Ident actVerbID = GoodyNames.CREATE_URI;
		Ident srcAgentID = null;
		
		BasicThingActionSpec btas = new BasicThingActionSpec(actRecID, tgtThingID, actVerbID, srcAgentID, btvm);

		sendThingActionSpec(btas, ran);
		return dummyBoxID;
	}
	public void updateGoodyLocation(Ident goodyID, float locX, float locY, float locZ, Random ran) {
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter gapw = new GoodyActionParamWriter(btvm);
		
		gapw.putLocation(locX, locY, locZ);

		
		Ident actRecID = new FreeIdent("action_#" + ran.nextInt());
		Ident tgtThingID = goodyID;
		Ident actVerbID = GoodyNames.MOVE_URI;
		Ident srcAgentID = null;
		
		BasicThingActionSpec btas = new BasicThingActionSpec(actRecID, tgtThingID, actVerbID, srcAgentID, btvm);

		sendThingActionSpec(btas, ran);
	}
	
	public void sendThingActionSpec(ThingActionSpec actionSpec, Random ran) {
		Logger log = getLogger();
		log.info("Sending action spec: " + actionSpec);

		FancyThingModelWriter ftmw = new FancyThingModelWriter();
		String updateTxt = ftmw.writeTASpecToString(actionSpec, goodyGraphQN, ran);
		
		// logInfo("UpdateTxt:\n" + updateTxt);
		myAgentRepoClient.execRemoteSparqlUpdate(TestOuterClientSOH.glueUpdURL, updateTxt);
	}
}
