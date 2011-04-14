/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.sight.obs;

import org.cogchar.sight.hypo.SightHypothesis;
import org.cogchar.animoid.config.FaceNoticeConfig;
import org.cogchar.animoid.config.ViewPort;
import org.cogchar.animoid.protocol.EgocentricDirection;

/**
 *
 * @author Stu Baurmann
 */
public class SightObservationComparison {
	public double distance;
	public double timeDiffSec;
	public EgocentricDirection diffDir;
	public double azDiffDiams;
	public double elDiffDiams;

	public SightObservationComparison(SightObservation one, SightObservation two, ViewPort vp) {

		timeDiffSec = ((double) Math.abs(one.getTimeStampMsec() - two.getTimeStampMsec())) / 1000.0;
		EgocentricDirection dir1 = one.getCenterDirection();
		EgocentricDirection dir2 = two.getCenterDirection();
		EgocentricDirection diffDir = dir1.subtract(dir2);
		double azDiffDeg = Math.abs(diffDir.getAzimuth().getDegrees());
		double elDiffDeg = Math.abs(diffDir.getElevation().getDegrees());

		double dd1 = one.getDiameterDeg(vp);
		double dd2 = two.getDiameterDeg(vp);
		double gmdd = Math.sqrt(dd1 * dd2); // geometric mean diameter degrees

		azDiffDiams = azDiffDeg / gmdd;
		elDiffDiams = elDiffDeg / gmdd;

		FaceNoticeConfig fnc = SightHypothesis.getFaceNoticeConfig();

		double termAzSquared = fnc.cogDistCoeffAzDiamSquared * azDiffDiams * azDiffDiams;
		double termElSquared = fnc.cogDistCoeffElDiamSquared * elDiffDiams * elDiffDiams;
		double termSeconds =   fnc.cogDistCoeffSeconds * timeDiffSec;
		// double termProduct = fnc.cogDistCoeffProduct * placeDiffFactor * timeDiffSec;
		distance = termAzSquared + termElSquared + termSeconds;
	}
}
