/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.calc.time;

import java.util.HashMap;
import java.util.Map;
import org.cogchar.calc.number.NumberFactory;
import org.jscience.mathematics.number.FieldNumber;

/**
 *
 * @author winston
 */
public class TimePointFactory<OffsetSecondsFN extends FieldNumber<OffsetSecondsFN>> {
	public enum EpochName {
		UNIX_EPOCH_1970,
		MILLENIUM_EPOCH_2000,
		TP_FACTORY_INIT_STAMP
	}
	private	NumberFactory<OffsetSecondsFN>					myOffsetNumberFactory;

	private	Map<EpochName, ExactTimePoint<OffsetSecondsFN>>		myEpochMap;
	private	ExactTimePoint<OffsetSecondsFN>						myInitStamp;
	
	public TimePointFactory(NumberFactory<OffsetSecondsFN> offsetNF) {
		myOffsetNumberFactory = offsetNF;
		myEpochMap = new HashMap<EpochName, ExactTimePoint<OffsetSecondsFN>>();
		myInitStamp = getNowPoint();
	}
	public OffsetSecondsFN convertMillisecToSeconds(long millisec) {
		return myOffsetNumberFactory.makeNumberFromRatioOfLongs(millisec, 1000);
	}
	public OffsetSecondsFN getNowOffsetFromUnixEpoch1970() {
		return convertMillisecToSeconds(System.currentTimeMillis());
	}
	public ExactTimePoint<OffsetSecondsFN> getNowPoint() {
		OffsetSecondsFN nowSec = getNowOffsetFromUnixEpoch1970();
		ExactTimePoint unixEpochRefPoint = getEpochReferencePoint(EpochName.UNIX_EPOCH_1970);
		return unixEpochRefPoint.addOffsetSeconds(nowSec);
	}
	public ExactTimePoint<OffsetSecondsFN> getNowPointPlusOffsetSec(OffsetSecondsFN offsetSec) {
		ExactTimePoint<OffsetSecondsFN> nowPoint = getNowPoint();
		return nowPoint.addOffsetSeconds(offsetSec);
	}
	public ExactTimePoint<OffsetSecondsFN> getNowPointPlusOffsetMillisec(long millisec) {
		OffsetSecondsFN offsetSec = convertMillisecToSeconds(millisec);
		return getNowPointPlusOffsetSec(offsetSec);
	}
	public ExactTimePoint<OffsetSecondsFN> getEpochReferencePoint(final EpochName en) {
		ExactTimePoint<OffsetSecondsFN> rp = myEpochMap.get(en);
		if (rp == null) {
			if (en == EpochName.UNIX_EPOCH_1970) {

			}
			ExactTimePoint<OffsetSecondsFN>	unixEpochStamp = getEpochReferencePoint(EpochName.UNIX_EPOCH_1970);
			// unixEpochRP = getEpochReferencePoint(en);
			rp = new ExactTimePoint<OffsetSecondsFN>() {
				@Override public OffsetSecondsFN findMyOffsetToReferencePoint(ExactTimePoint<OffsetSecondsFN> otherPoint) {
					if (otherPoint.equals(this)) {
						return myOffsetNumberFactory.getZero();
					} else {
						OffsetSecondsFN  reverseOffset = otherPoint.findMyOffsetToReferencePoint(otherPoint);
						return reverseOffset.opposite();
					}
				}
			};
			myEpochMap.put(en, rp);
		}
		return rp;
	}
	public ExactTimePoint<OffsetSecondsFN> getOffsetFromEpoch(EpochName epoch, OffsetSecondsFN offset) {
		return null;
	}
	public ExactTimePoint<OffsetSecondsFN> getCurrentSysTimeOffsetFromEpoch(EpochName epoch) {
		// sysTimeMillis ->
		return null;
	}
}
