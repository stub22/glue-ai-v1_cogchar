/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.platform.util;

import java.util.Collection;

/**
 * @author Stu Baurmann
 */
public class CollectionFilter {
	public abstract static class Predicate<T> {
		public abstract boolean test(T t);
	}
	public static <T> void filter(Collection<T> src, Collection<T> tgt, Predicate<T> pred) {
		if (src != null) {
			for (T t : src) {
				if (pred.test(t)) {
					tgt.add(t);
				}
			}
		}
	}
}
