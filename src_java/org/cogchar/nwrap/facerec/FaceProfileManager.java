/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.nwrap.facerec;

import org.cogchar.vision.OpenCVImage;
import java.util.Collection;

/**
 *
 * @author humankind
 */
public interface FaceProfileManager {

	double compareProfiles(FaceProfile fp1, FaceProfile fp2);

	String computeImageQuality(OpenCVImage image);

	FaceProfile makeProfile(Collection<OpenCVImage> images);

	void releaseProfile(FaceProfile fp);

}
