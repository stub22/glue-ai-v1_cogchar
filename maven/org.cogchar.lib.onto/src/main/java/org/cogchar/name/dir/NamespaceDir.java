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
package org.cogchar.name.dir;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class NamespaceDir {
	// Formal prefix for Robokind 2012 runtime 
	public static String RKRT_NS_PREFIX = "urn:ftd:robokind.org:2012:runtime#";
	// Formal prefix for Cogchar 2012 runtime 
	public static String NS_CCRT_RT = "urn:ftd:cogchar.org:2012:runtime#";
	// Formal prefix for Cogchar 2012 goody
	public static String GOODY_NS = "urn:ftd:cogchar.org:2012:goody#";
	// Formal prefix for Cogchar Web
	//public static String WEB_NS = "http://www.cogchar.org/lift/config#";
	
	// Less formal web prefix still widely used:
	final private static String NSP_Root = "http://www.cogchar.org/";
	
	public static final String GC_NS = NSP_Root + "general/config#";
	public static final String NS_GCI = NSP_Root + "general/config/instance#";
	
	
	public static final String NS_ChatAssmbl = NSP_Root + "chat/config#";
	
	// Big section of Lifter namespaces, broken down by common prefixes.
	final private static String NSP_LifterRoot = NSP_Root + "lift/";
	// Lifter 
	// Some of these may be revisited to be sure we have a coherent scema
	final public static String NS_LifterQuery = NSP_LifterRoot + "action/query#";
	final public static String NS_LifterUser = NSP_LifterRoot + "user#";
	final public static String NS_LifterUserAccessInstance = NSP_LifterRoot + "user/config/instance#";
	final public static String NS_LifterUserAction = NSP_LifterRoot + "user/action#";
	final public static String NS_RequestLifterRepoOutput = NSP_LifterRoot + "repooutput#";
	
	// "Config" NSs
	// Note that this constant ends in "config#", thus it sorta collides with the prefix "config/" use below.
	public static final String NS_CgcLC = NSP_LifterRoot + "config#"; // "http://www.cogchar.org  /lift/config#";
	
	final private static String NSP_LifterConfigRoot = NSP_LifterRoot + "config/";
	final public static String NS_LifterCmd = NSP_LifterConfigRoot + "command#";
	final public static String NS_LifterVar = NSP_LifterConfigRoot + "variable#";
	final public static String NS_LifterSessionVar = NSP_LifterConfigRoot + "sessionVariable#";
	final public static String NS_LifterConfig = NSP_LifterConfigRoot + "configroot#";
	final public static String NS_LifterInstance = NSP_LifterConfigRoot + "instance#";

	// "Schema" NSs
	final private static String NSP_SchemaRoot = NSP_Root + "schema/";
	final public static String NS_SceneTrig = NSP_SchemaRoot + "scene/trigger#";
	// Used in FancyChan.scala
	public static	String 		NS_ccScn =	NSP_SchemaRoot + "scene#"; // http://www.cogchar.org  /schema/scene#";
	public static	String 		NS_ccScnInst = NSP_SchemaRoot + "scene/instance#"; // http://www.cogchar.org  /schema/scene/instance#";	
	//ActionStrings uses this
	final public static String NS_CinePathDef = NSP_SchemaRoot + "path/definition#";
	//But LiftAN uses this:
	final public static String NS_CineDef = NSP_SchemaRoot + "cinematic/definition#";
	final public static String NS_ThingAnim = NSP_SchemaRoot + "thinganim/definition#";
	// Used in CinemaAN
	public static	String		NS_CgcCC		= NSP_SchemaRoot + "cinematic#";  // "http://www.cogchar.org  /schema/cinematic#";
	
	// Used in LightsCameraAN - possibly incorrect - does repo data match this prefix?
	public static	String		NS_CgcBC		= "http://www.cogchar.org/bony/config#";

	
	// From FancyThingModelWriter
	
	public static	String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static	String XSD_NS = "http://www.w3.org/2001/XMLSchema#";

	public static	String CCRT_NS = NS_CCRT_RT; // "urn:ftd:cogchar.org:2012:runtime#";
	
	public static	String TA_NS = NSP_Root + "thing/action#";  // "http://www.cogchar.org  /thing/action#";
	// public static	String GOODY_NS = "urn:ftd:cogchar.org:2012:goody#"	;
	
	public static   String DC_NS = "http://purl.org/dc/elements/1.1/";
		
		
}
