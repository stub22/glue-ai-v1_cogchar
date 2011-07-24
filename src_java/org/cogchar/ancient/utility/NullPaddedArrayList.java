/*
 *  Copyright 2008 Hanson Robotics Inc.
 *  All Rights Reserved.
 */

package org.cogchar.ancient.utility;

import java.util.ArrayList;

/**
 *
 * @author Stu Baurmann
 */
public class NullPaddedArrayList<E> extends ArrayList<E> {
	@Override 
	public E get(int index) {
		int sz = size();
		// System.out.println("NullPaddedArrayList.get(" + index + ") called on list of size " + sz);
		if (index >= sz) {
			return null;
		} else {
			return super.get(index);
		}
	}

}
