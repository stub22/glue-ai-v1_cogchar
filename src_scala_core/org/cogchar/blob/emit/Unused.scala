/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.blob.emit

/**
 * @author Stu B. <www.texpedient.com>
 */
object Unused {}
/*
object HierConfMgr {
}

class BootSpec {

}
class BootSpecBuilder {

}
object BootFieldNames {
	import org.cogchar.impl.perform.ChannelNames;	
	
	val		NS_ccScn =	ChannelNames.NS_ccScn;
	val		NS_ccScnInst = ChannelNames.NS_ccScnInst;
	
}
class ZenoConfigEmitter extends BonyConfigEmitter {
}
*/
	// Commercial "Extra Robot" model, not loadable without a license, ignored in those cases.
/*
	val EXTRA_ROBOT_MESH_PATH = "zenobot_06_20120323/zenobot_06_20120323.mesh.xml";		
	
	// val EXTRA_ROBOT_MESH_PATH = "zenobot_05_20120309/ZenoBot05.ma.mesh.xml";		
	//		String dualCharURI = "urn:org.cogchar/platform/nb701?char=HRK_Zeno_R50&version=20120302";   
	//		
	//			
	val HRK_URN_PREFIX = "urn:fdt:com.hrk/";
	val ZENO_R50_NICKNAME = "ZenoR50";
	val	ZENO_R50_CHAR_URI = COGCHAR_CHAR_URN_PREFIX + ZENO_R50_NICKNAME;
	val	ZENO_R50_CHAR_IDENT = new FreeIdent(ZENO_R50_CHAR_URI, ZENO_R50_NICKNAME)
*/

	// Appear to be no longer used
	//val SINBAD_JOINT_PATH = "rk_bind_config/motion/bonyRobotConfig_Sinbad.json";
	//val ZENO_JOINT_PATH = "rk_bind_config/motion/bonyRobotConfig_ZenoR50.json";
	/*
	val NB_PLATFORM_CURRENT = "nb701";
	
	def isZenoHome() : Boolean = {	true;	}	
	
	//val HRK_URN_PREFIX = "urn:ftd:hrkind.com:2012:chars#";
	
	// val HRK_TEMP_PREFIX = "http://www.hrkind.com/model#"
	
	//val ZENO_R50_NICKNAME = "char_cajunZeno_77";
	//val	ZENO_R50_CHAR_URI = HRK_TEMP_PREFIX + ZENO_R50_NICKNAME;
	//val	ZENO_R50_CHAR_IDENT = new FreeIdent(ZENO_R50_CHAR_URI, ZENO_R50_NICKNAME)

	//val AZR50_NICKNAME = "aZR50";
	//val	AZR50_CHAR_URI = HRK_TEMP_PREFIX + AZR50_NICKNAME;
	//val	AZR50_CHAR_IDENT = new FreeIdent(AZR50_CHAR_URI, AZR50_NICKNAME)	
	
	
	
	val FANCY_PANEL_CLASSNAME = "org.cogchar.render.gui.bony.FancyCharPanel";
	val SLIM_PANEL_CLASSNAME = "org.cogchar.render.gui.bony.VirtCharPanel";
	

	val WOS_SCALE = 0.5f;
	
	// val NB_BONY_ROBOT_ID = "COGCHAR_NB_ROBOT";
	// val DUMMY_ROBOT_ID = "DummyRobot22";
	*/
   	/*
	def getNamedFloatVector(vectorURI : String) : Array[Float] = {
		val third = 0.33333f;
		val res = new Array[Float](3); // third, third, third);
		res(0) = third;
		res(1) = third;
		res(2) = third;
		val res2 : Array[Float] = Array(third, third, third);
		res2;
	}
		//public String getJointConfigAssetNameForChar(String charURI) {
//		return getBonyConfigEmitter().getJointConfigAssetNameForChar(charURI);
//	}

	public Vector3f getConfigVector3f(String vectorURI) {
		float[] xyz = getBonyConfigEmitter().getNamedFloatVector(vectorURI);
		return JmonkeyMathObjFactory.makeVector(xyz);

 */	
  /*
	private def buildBaseHumanoidFigureConfigForChar(charIdent:Ident, bonyGraphIdent:Ident) : HumanoidFigureConfig = {
		val hfc = new HumanoidFigureConfig();
		hfc.myCharIdent = charIdent;
		hfc.myNickname = HumanoidConfigEmitter.getRobotId(charIdent);
		hfc.myMeshPath = HumanoidConfigEmitter.getMeshPath(charIdent);
		hfc.myDebugSkelMatPath = getMaterialPath;
		hfc.myBoneConfig = new HumanoidBoneConfig();
		val initialPosition = HumanoidConfigEmitter.getInitialPosition(charIdent);
		hfc.myInitX = initialPosition(0);
		hfc.myInitY = initialPosition(1);
		hfc.myInitZ = initialPosition(2);
		hfc.myPhysicsFlag = HumanoidConfigEmitter.getPhysicsFlag(charIdent);
		addBoneDescsFromBoneRobotConfig(charIdent, bonyGraphIdent, hfc);
		hfc
	}
	private def getSinbadFigureConfig()  : HumanoidFigureConfig = {
		val hfc = buildBaseHumanoidFigureConfigForChar(SINBAD_CHAR_IDENT, new FreeIdent("","")); // Blank FreeIdent is a total band-aid to keep any bonedescs from loading from query config - need to clean this up soon!!!!
		hfc.myBoneConfig.addSinbadDefaultBoneDescs();
		hfc
	}

	def getHumanoidFigureConfigForChar(charIdent: Ident, bonyGraphIdent:Ident) : HumanoidFigureConfig = {
		if (charIdent.equals(SINBAD_CHAR_IDENT)) {
			getSinbadFigureConfig()		
		} else {
			buildBaseHumanoidFigureConfigForChar(charIdent, bonyGraphIdent);
		}
	}
	*/

  /*
  // This is for getting bone names from Turtle, but was going to require a ClassLoader from somewhere
  // Instead, just jumped to query-based config
  def getBoneNames(robotIdent:Ident, loader:ClassLoader, queryUri:String)={
	val rdfModel = RepoDatasetFactory.createPrivateMemModel
	val solutions = new ArrayBuffer[String]
	try {
	  val stream = loader.getResourceAsStream(HumanoidConfigEmitter.getBonyConfigPath(robotIdent));
	  rdfModel.read(stream, null, "TURTLE");
	} catch {
	  case e: Exception => {
		  println("Exception attemping to read Turtle file: " + e); // Oh so temporary to have this here - need a real logger and probably better handling
		}
	}
	val queryString = HumanoidConfigEmitter.getQuery(queryUri);
	val query = QueryFactory.create(queryString);
	val qexec = QueryExecutionFactory.create(query, rdfModel);
	var results:ResultSet = null;
	try {
	  results = qexec.execSelect();
	} finally {
	  qexec.close();
	}
	
	while (results.hasNext) {
	  val soln = results.nextSolution
	  val varNames = soln.varNames
	  while(varNames.hasNext())
	  {
		val solutionNode = soln.get(varNames.next());
		solutions += solutionNode.toString
	  }
	}
	//for (solution <- solnList if solution contains BONE_VAR_NAME) yield solution.getLiteral(BONE_VAR_NAME).getString
	solutions
  }
  */
 
  // An undesirable compromise for now: these constants are in org.cogchar.api.skeleton.config.BoneConfigNames,
  // which is now in o.c.lib.animoid so we can't see it. This is the only remaining place in o.c.lib.core which
  // needs BoneConfigNames, which is another reason this will likely be refactored soon.
  /*
  final val BONE_NAMES_QUERY_TEMPLATE_URI = "ccrt:template_boneNames_99";
  final val ROBOT_IDENT_QUERY_VAR = "robotUri";
  final val BONE_NAME_VAR_NAME = "boneName";
  def addBoneDescsFromBoneRobotConfig(charIdent:Ident, graphIdent:Ident, hfc:HumanoidFigureConfig) {
	val queryEmitter = QuerySheet.getInterface
	var queryString = queryEmitter.getQuery(BONE_NAMES_QUERY_TEMPLATE_URI);
	queryString = queryEmitter.setQueryVar(queryString, ROBOT_IDENT_QUERY_VAR, charIdent);
	val solutionList = queryEmitter.getTextQueryResultList(queryString, graphIdent);
	val boneNames = queryEmitter.pullStrings(solutionList, BONE_NAME_VAR_NAME);
	boneNames.foreach(boneName => {
		hfc.myBoneConfig.addBoneDesc(boneName);
	  })
  }
 
	def getSystemContextURI() : String = { myBehaviorCE.getSystemContextURI()}

	
	val myBehaviorCE : BehaviorConfigEmitter = new BehaviorConfigEmitter();
		
	def getBehaviorConfigEmitter() : BehaviorConfigEmitter = myBehaviorCE;
	

	val myConvyCE : ConvyConfigEmitter = new ConvyConfigEmitter();
	
	def	getConvyConfigEmitter() : ConvyConfigEmitter = myConvyCE;

	
	val myRobokindBindingCE : RobokindBindingConfigEmitter = new RobokindBindingConfigEmitter();
	
	def	getRobokindBindingConfigEmitter() : RobokindBindingConfigEmitter = myRobokindBindingCE;	

	
	lazy val myRenderCE : RenderConfigEmitter = new RenderConfigEmitter(Some(getSystemContextURI()));
	
	def getRenderConfigEmitter() : RenderConfigEmitter = myRenderCE 
 
 
 
  */
  