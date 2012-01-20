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

package org.cogchar.convoid.output.config;

import java.io.File;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cogchar.platform.util.TimeUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class BehaviorDataLoader {
	private static Logger theLogger = Logger.getLogger(BehaviorDataLoader.class.getName());
	/*
    public static XStream buildDom4jXStreamForRead() {
		Dom4JDriver dom4jDriver = new Dom4JDriver();
		XStream xstream = new XStream();
		initBehaviorXStream(xstream);
		return xstream;
	}
	public static void initBehaviorXStream(XStream xstream) {
		xstream.processAnnotations(Act.class);
		xstream.processAnnotations(Category.class);
		xstream.alias("Step", Step.class);
		xstream.alias("Act", Act.class);
		xstream.alias("Category", Category.class);
		xstream.addImplicitCollection(Act.class, "mySteps", Step.class);
		xstream.addImplicitCollection(Act.class, "myMeanings", String.class);
		xstream.addImplicitCollection(Category.class, "myMeanings", String.class);
		xstream.addImplicitCollection(Category.class, "mySubCategories", Category.class);
		xstream.addImplicitCollection(Category.class, "myActs", Act.class);
		xstream.useAttributeFor(Category.class, "name");
		xstream.useAttributeFor(Category.class, "resume");
		xstream.useAttributeFor(Category.class, "cascade");
		xstream.useAttributeFor(Act.class, "name");
		xstream.useAttributeFor(Act.class, "start");
		xstream.useAttributeFor(Act.class, "next");
		xstream.useAttributeFor(Step.class, "type");		
		xstream.registerConverter(new Step.XStreamConverter());		
	}
	public static Category loadBehaviorCategory_depricated(String filename) throws Throwable {

		XStream xstream = buildDom4jXStreamForRead();
		FileReader fread = new FileReader(filename);
		Category behaviorCategory = (Category) xstream.fromXML(fread);
		return behaviorCategory;
	}
	*/
	public static void main(String[] args) {
		String filename = "C:\\_hanson\\_deploy\\distro_18d\\conf\\_robokind\\zeno4x\\convoid\\speech_behavior\\expositions\\expos_101+.xml";
		try {
            long start = TimeUtils.currentTimeMillis();
			Category loadedCat = null;
            //loadedCat = loadBehaviorCategory_depricated(filename);
            theLogger.severe("Loading file: " + filename + "\n\tLoad time: " + (TimeUtils.currentTimeMillis() - start));
            if(loadedCat != null){
                System.out.println("Loaded category: " + loadedCat.getName());
                System.out.println("Sub count: " + loadedCat.getSubCategories().size());
            }
            start = TimeUtils.currentTimeMillis();
			loadedCat = loadBehaviorCategory(filename);
            theLogger.severe("Loading file: " + filename + "\n\tLoad time: " + (TimeUtils.currentTimeMillis() - start));
			System.out.println("Loaded category: " + loadedCat.getName());
			System.out.println("Sub count: " + loadedCat.getSubCategories().size());
		} catch (Throwable t) {
			System.err.println("Caught: " + t);
			t.printStackTrace();
		}
	}

    public static Category loadBehaviorCategory(String filename) throws Throwable {
        FileReader fr = new FileReader(filename);
        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(fr);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().equals("Category")){
                        return parseCategory(xpp);
                    }
                }
                eventType = xpp.next();
            }

        }catch(Throwable t){
			theLogger.log(Level.SEVERE, "Problem reading BehaviorCategory from: " + filename, t);
        }finally{
            fr.close();
        }
        return null;
	}

    private static Category parseCategory(XmlPullParser xpp) throws IOException, XmlPullParserException, Throwable {
        Category cat = new Category();
       // try{
            int attrs = xpp.getAttributeCount();
            for(int i=0; i<attrs; i++){
                String name = xpp.getAttributeName(i);
                String val = xpp.getAttributeValue(i);
                if(name.equals("name")){
                    cat.setName(val);
                }
            }
            int eventType = xpp.next();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    String name = xpp.getName();
                    if(name.equals("Meaning")){
                        String meaning = loadMeaning(xpp);
                        if(meaning != null){
                            cat.addMeaning(meaning);
                        }
                    }else if(name.equals("Act")){
                        cat.addAct(parseAct(xpp));
                    }else if(name.equals("Category")){
                        cat.addSubCategory(parseCategory(xpp));
                    }
                } else if(eventType == XmlPullParser.END_TAG) {
                    if(xpp.getName().equals("Category")){
                        return cat;
                    }
                }
                eventType = xpp.next();
            }
       /* }
		 Exceptions need to escape to a level where the filename is known.
			catch(Throwable t){
            t.printStackTrace();
        }*/
        return cat;
    }

    private static Act parseAct(XmlPullParser xpp) throws IOException, XmlPullParserException{
        Act act = new Act();
        int attrs = xpp.getAttributeCount();
        for(int i=0; i<attrs; i++){
            String name = xpp.getAttributeName(i);
            String val = xpp.getAttributeValue(i);
            if(name.equals("name")){
                act.setName(val);
            }else if(name.equals("start")){
                act.setStart(val);
            }else if(name.equals("next")){
                act.setNext(val);
            }
        }
        loadActChildren(xpp, act);
        return act;
    }

    private static void loadActChildren(XmlPullParser xpp, Act act) throws IOException, XmlPullParserException{
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if(name.equals("Meaning")){
                    String meaning = loadMeaning(xpp);
                    if(meaning != null){
                        act.addMeaning(meaning);
                    }
                }else if(name.equals("Step")){
                    Step step = parseStep(xpp);
                    if(step != null){
                        act.addStep(step);
                    }
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("Act")){
                    return;
                }
            }
            eventType = xpp.next();
        }
    }

    private static String loadMeaning(XmlPullParser xpp) throws IOException, XmlPullParserException{
        int eventType = xpp.getEventType();
        String meaning = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.TEXT) {
                meaning = xpp.getText();
            } else if(eventType == XmlPullParser.END_TAG) {
                return meaning;
            }
            eventType = xpp.next();
        }
        return meaning;
    }

    private static Step parseStep(XmlPullParser xpp) throws IOException, XmlPullParserException{
        Step step = new Step();
        int attrs = xpp.getAttributeCount();
        for(int i=0; i<attrs; i++){
            String name = xpp.getAttributeName(i);
            String val = xpp.getAttributeValue(i);
            if(name.equals("type")){
                step.setType(val);
            }
        }
        loadStepText(xpp, step);
        String text = step.getText().trim();
        if(text.isEmpty() || text.matches("\\s*<sapi>\\s*</sapi>\\s*")){
            return null;
        }
        return step;
    }

    private static void loadStepText(XmlPullParser xpp, Step step) throws IOException, XmlPullParserException{
        String text = "";
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("Step")){
                    step.setText(text);
                    return;
                }else if(xpp.getName().equals("bookmark") ||
                         xpp.getName().equals("silence")){

                }else{
                    text += xpp.getText();
                }
            }else{
                text += xpp.getText();
            }
            eventType = xpp.next();
        }
    }

    public static List<Agenda> loadAgendas(File file) throws Throwable {
        FileReader fr = new FileReader(file);
        try{
            List<Agenda> agendas = new ArrayList();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(fr);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if(eventType == XmlPullParser.START_TAG) {
                    if(xpp.getName().equals("Agenda")){
                        Agenda a = parseAgenda(xpp);
                        if(a != null){
                            agendas.add(a);
                        }
                    }
                }
                eventType = xpp.next();
            }
            return agendas;
        }catch(Throwable t){
			theLogger.log(Level.SEVERE, "Problem reading Agenda from: " + file, t);

        }finally{
            fr.close();
        }
        return null;
    }

    private static Agenda parseAgenda(XmlPullParser xpp) throws IOException, XmlPullParserException{
        int eventType = xpp.getEventType();
        Agenda a = new Agenda();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if(name.equals("Agenda")){
                    for(int i=0; i<xpp.getAttributeCount(); i++){
                        if(xpp.getAttributeName(i).equals("name")){
                            a.setName(xpp.getAttributeValue(i));
                        }
                    }
                }else if(name.equals("Meanings")){
                    a.setMeanings(parseAgendaMeanings(xpp));
                }else if(name.equals("Timers")){
                    parseAgendaTimers(xpp, a);
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("Agenda")){
                    return a;
                }
            }
            eventType = xpp.next();
        }
        return a;
    }

    private static List<String> parseAgendaMeanings(XmlPullParser xpp) throws IOException, XmlPullParserException{
        List<String> meanings = new ArrayList<String>();
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if(name.equals("Meaning")){
                    meanings.add(parseAgendaMeaning(xpp));
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("Meanings")){
                    return meanings;
                }
            }
            eventType = xpp.next();
        }
        return meanings;
    }

    private static String parseAgendaMeaning(XmlPullParser xpp) throws IOException, XmlPullParserException{
        String meaning = null;
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.TEXT) {
                meaning = xpp.getText();
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("Meaning")){
                    return meaning;
                }
            }
            eventType = xpp.next();
        }
        return meaning;
    }

    private static void parseAgendaTimers(XmlPullParser xpp, Agenda a) throws IOException, XmlPullParserException{
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                String name = xpp.getName();
                if(name.equals("Timer")){
                    parseAgendaTimer(xpp, a);
                }
            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("Timers")){
                    return;
                }
            }
            eventType = xpp.next();
        }
    }

    private static void parseAgendaTimer(XmlPullParser xpp, Agenda a) throws IOException, XmlPullParserException{
        String name = null;
        Long time = null;
        int attrs = xpp.getAttributeCount();
        for(int i=0; i<attrs; i++){
            String attr = xpp.getAttributeName(i);
            String val = xpp.getAttributeValue(i);
            if(attr.equals("name")){
                name = val;
            }else if(attr.equals("time")){
                time = Long.parseLong(val);
            }
        }
        if(time == null || name == null){
            return;
        }
        if(name.equals("RESUME_AGENDA")){
            a.setResumeAgendaTime(time);
        }else if(name.equals("AGENDA_TIMEOUT")){
            a.setCursorTimeoutLength(time);
        }else if(name.equals("ADVANCE_AGENDA")){
            a.setAdvanceAgendaTime(time);
        }
    }
}
