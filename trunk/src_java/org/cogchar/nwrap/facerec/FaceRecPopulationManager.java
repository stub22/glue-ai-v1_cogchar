/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.nwrap.facerec;

import org.cogchar.vision.OpenCVImage;
import java.util.Collection;

/**
 * Restricted API of FaceRec features.
 * @author Stu Baurmann
 */
public interface FaceRecPopulationManager {

	public long createPopulation();
	public void destroyPopulation(long pop_id);
	public void loadPopulationAndReplaceDefault(String fileName);
	public boolean savePopulation(long popID, String fileName);
	public long getDefaultPopulationID();
	public String matchPerson(OpenCVImage image, long population);
	// public boolean addNamedPerson(OpenCVImage image, String name, long pop_id);
	public boolean addNamedPerson(Collection<OpenCVImage> image, String name, long pop_id);
	public void removePerson(String name, long pop_id);
	public String[] listPopulation(long pop_id);
	
	
}
