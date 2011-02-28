/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.calc.time;

import java.util.HashMap;
import java.util.Map;
import org.jscience.mathematics.number.FieldNumber;

/**
 *
 * @author winston
 */
public class TimePointFactory<OffsetSecondsFN extends FieldNumber<OffsetSecondsFN>> {
	public enum EpochName {
		UNIX_EPOCH_1970,
		MILLENIUM_EPOCH_2000,
		TP_FACTORY_INIT_TIMESTAMP
	}
	private	Map<EpochName, TimePoint<OffsetSecondsFN>>		myEpochMap;
	
	public TimePointFactory() {
		long initTimestamp = System.currentTimeMillis();
		myEpochMap = new HashMap<EpochName, TimePoint<OffsetSecondsFN>>() ;
	}
	public TimePoint<OffsetSecondsFN> getEpochReferencePoint(EpochName en) {
		TimePoint<OffsetSecondsFN> rp = myEpochMap.get(en);
		if (rp == null) {
			rp = new TimePoint<OffsetSecondsFN>() {
				@Override public OffsetSecondsFN findMyOffsetToSimilarPoint(TimePoint<OffsetSecondsFN> otherPoint) {
					return null;
				}
			};
			myEpochMap.put(en, rp);
		}
		return rp;
	}
	public TimePoint<OffsetSecondsFN> getOffsetFromEpoch(EpochName epoch, OffsetSecondsFN offset) {
		return null;
	}
	public TimePoint<OffsetSecondsFN> getCurrentSysTimeOffsetFromEpoch(EpochName epoch) {
		// sysTimeMillis ->
		return null;
	}
}
