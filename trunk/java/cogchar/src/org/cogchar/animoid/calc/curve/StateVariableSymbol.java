/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.animoid.calc.curve;

/**
 *
 * @param <T> 
 * @author Stu Baurmann
 */
public interface StateVariableSymbol<T extends StateVariableSymbol> {
	public String getSymbolString();
	public int getSymbolIndex();
	public int getSymbolBlockSize();
	
	public T getSymbolAtIndex(int index);

}
