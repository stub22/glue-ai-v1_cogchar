/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bony;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.UIManager;
import jme3test.TestChooser;
import org.osgi.framework.Bundle;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class TestChooserWrapper extends TestChooser {
    private static final Logger logger = Logger.getLogger(TestChooserWrapper.class.getName());
	
	private	List<Class> myTestClasses = new ArrayList<Class>();
		
	public static void displayTestChooser(Bundle bun, final String[] args) {         
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        TestChooserWrapper tcw = new TestChooserWrapper(bun);

		tcw.start(args);
	}
	public TestChooserWrapper(Bundle bun) {
		super();
		// This gets just the first level of entries below /jme3test
		// Enumeration<String> testPaths = b.getEntryPaths("/jme3test");
		// This will return all the matching entries, as URLs like:
		// bundle://5.0:0/jme3test/app/TestContextRestart.class
		Enumeration<URL> testPaths = bun.findEntries("/jme3test", "*.class", true);
		if (testPaths != null) {
			while (testPaths.hasMoreElements()) {
				URL tp = testPaths.nextElement();
				String urlText = tp.toExternalForm();
				// System.out.println("FOUND: " + urlText);
				Class c = load(urlText);
				if (c != null) {
					myTestClasses.add(c);
				}
			}
		}		
	}
	@Override protected void addDisplayedClasses(Vector<Class> classes) {
        // find("jme3test", true, classes);
		for (Class c : myTestClasses) {
			classes.add(c);
		}
    }
    private Class load(String name) {
		int cns = name.indexOf("jme3test/");
        if (name.endsWith(".class")
         && name.indexOf("Test") >= 0
         && name.indexOf('$') < 0
		&& cns >= 0) {
		
			String classname = name.substring(cns, name.length() - ".class".length());

            if (classname.startsWith("/")) {
                classname = classname.substring(1);
            }
            classname = classname.replace('/', '.');

			// System.out.println("Deduced classname: " + classname);
            try {
				logger.info("Trying to load class: " + classname);
                final Class<?> cls = Class.forName(classname);
                cls.getMethod("main", new Class[] { String[].class });
                if (!getClass().equals(cls)) {
                    return cls;
                }
            } catch (NoClassDefFoundError e) {
                // class has unresolved dependencies
                return null;
            } catch (ClassNotFoundException e) {
                // class not in classpath
                return null;
            } catch (NoSuchMethodException e) {
                // class does not have a main method
                return null;
            }
        }
        return null;
    }
}
