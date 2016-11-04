/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.zzz.impl.weber.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author matt
 */
public class TextUtils {
    
    public static <T> List<T> list(T ...items){
		List<T> myList = new ArrayList<T>();
		for(Object o : items){
			myList.add((T)o);
		}
		return myList;
	}


	public static void println(String s){
		System.out.println(s);
	}

    public static String plaintext(String s){
        return s.replaceAll("[^a-zA-Z0-9 .,:?$@!']+", " ").replaceAll("\\s+", " ");
    }

    public static String stripTags(String s){
        return s.replaceAll("<[^>]*>", "");
    }

    public static String silence(int msec){
        return "<silence msec=\"" + msec + "\"/> ";
    }
}
