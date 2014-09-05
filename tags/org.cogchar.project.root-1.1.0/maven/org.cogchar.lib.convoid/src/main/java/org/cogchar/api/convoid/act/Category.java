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


package org.cogchar.api.convoid.act;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class Category {        
	// Want this to be an attribute called "name" in XML, and don't see 
	// how to easily make XStream use a particular name for an attribute, 
	// so we're calling the variable by the name we want ("name").
	
	//UPDATE: The original problem is solved using XStream method: "useAttributeFor"
	private		String			name;
	private		String			resume;
	private		String			cascade;
	private		List<Act>		myActs;
	private		List<Category>	mySubCategories;

	@XStreamImplicit(itemFieldName="Meaning")
	private List<String>		myMeanings;

	public Category(String n) {
		name = n;
		completeInit();
	}
    protected Category(){
        myMeanings = new ArrayList<String>();
    }

    public static Category makeCategoryWithEmptyMeaningsOnly() {
		return new Category();
    }
	public void completeInit() {
		// Empty collections may not be instantiated by xstream - it's probably configurable...
		if (mySubCategories == null) {
			mySubCategories = new ArrayList<Category>();
		}
		if (myActs == null) {
			myActs = new ArrayList<Act>();
		}
		initMeanings();
	}
	public void initMeanings()
	{
		if(myMeanings == null){
			myMeanings = new ArrayList<String>();
			return;
		}
		List<String> meanings = new ArrayList<String>();
		for(String m : myMeanings){
            if(!meanings.contains(m.toUpperCase())){
                meanings.add(m.toUpperCase());
            }
		}
		myMeanings = meanings;
        String meaning = name.toUpperCase();
        if(meaning.startsWith("C_")){
            meaning = meaning.substring(2);
            if(!meanings.contains(meaning)){
                myMeanings.add(meaning);
            }
        }
	}
	public void addSubCategory (Category c) {
		completeInit();
		mySubCategories.add(c);
	}
	public void addAct(Act a) {
		completeInit();
		myActs.add(a);
	}
    public void setName(String n){
        name = n;
    }
	public String getName() {
		return name;
	}
	public String toString() {
		return "Category{name=" + name + ", acts=" + myActs + ", subCats=" + mySubCategories + "}";
	}
	public List<Category> getSubCategories() {
		completeInit();
		return mySubCategories;
	}
	public List<Act> getActs() {
		completeInit();
		return myActs;
	}
	public Category findSubCategory(String name) {
		Category result = null;
		List<Category> subcats = getSubCategories();
		for (Category sc: subcats) {
			if (sc.getName().equals(name)) {
				result = sc;
				break;
			} else {
				Category ssc = sc.findSubCategory(name);
				if (ssc != null) {
					result = ssc;
					break;
				}
			}
		}
		return result;
	}

	public String getResume() {
		if(resume == null || resume.isEmpty()){
			resume = "true";
			if(getCascade().equals("true"))
				resume = "false";
		}
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public String getCascade() {
		if(cascade == null || cascade.isEmpty())
			cascade = "false";
		return cascade;
	}

	public List<String> getMeanings(){
		return myMeanings;
	}

    public void setMeanings(List<String> meanings){
        if(meanings == null){
            myMeanings = new ArrayList<String>();
            return;
        }
        myMeanings = meanings;
    }

    public void addMeaning(String m){
        m = m.toUpperCase();
        if(!myMeanings.contains(m)){
            myMeanings.add(m);
        }
    }

    public String toXML(){
        String category = "<Category name=\"" + name + "\">";
        for(String m : myMeanings){
            category += "\n\t<Meaning>" + m + "</Meaning>";
        }
        for(Act act : myActs){
            category += act.toXML();
        }
        for(Category cat : mySubCategories){
            category += cat.toXML();
        }
        category += "</Category>";
        return category;
    }
}
