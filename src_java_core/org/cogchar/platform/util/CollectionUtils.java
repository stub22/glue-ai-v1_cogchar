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

package org.cogchar.platform.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Stu B. <www.texpedient.com> and Matt Stevenson
 */
public class CollectionUtils {
	/**
	 * 
	 * @param <T>
	 * @param src
	 * @param clazz
	 * @return
	 */
	public static <T extends Object> List<T> copyAndDowncastList(List src, Class<T> clazz) {
		List<T> tgt = new ArrayList<T>();
		for (Object o: src) {
			tgt.add ((T) o);
		}
		return tgt;
	}
	public static <T> List<T> list(T ...items){
		//gasp! duplicated code?  Why not call abstractList?
		//We don't want to incur reflection costs if we don't have to.
		List<T> myList = new ArrayList<T>();
		for(Object o : items){
			myList.add((T)o);
		}
		return myList;
	}
	public static <T> List<T> linkedList(T ...items){
		List<T> myList = new LinkedList<T>();
		for(Object o : items){
			myList.add((T)o);
		}
		return myList;
	}
	public static <T> List<T> abstractList(Class clazz, T ...items){
		List<T> myList = null;
		try{
			Constructor cons = clazz.getConstructor();
			myList = (List<T>)cons.newInstance();
		}catch(Exception ex){
			throw new RuntimeException("Error creating a List from class: " + clazz.getName(), ex);
		}
		for(Object o : items){
			myList.add((T)o);
		}
		return myList;
	}
	public static <T, Y> Y[] array(List<T> items, Class<Y> clazz){
		int l = items.size();
		Y[] arr =(Y[])new Object[l];
		for(int i=0; i<l; i++){
			arr[i] = (Y)items.get(i);
		}
		return (Y[])arr;
	}
	public static List<Integer> intList(int[] items){
		List<Integer> myList = new ArrayList<Integer>();
		for(Integer o : items){
			myList.add(o);
		}
		return myList;
	}
}
