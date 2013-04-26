/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
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
package org.cogchar.gui.demo;

import com.hp.hpl.jena.graph.Node;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.BoxContext;
import org.appdapter.api.trigger.MutableBox;
import org.appdapter.api.trigger.Trigger;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.WithDirectory;
import org.appdapter.demo.DemoResources;
import org.appdapter.demo.DemoServiceWrapFuncs;
import org.appdapter.gui.box.DisplayContextProvider;
import org.appdapter.gui.box.ScreenBoxContextImpl;
import org.appdapter.gui.box.ScreenBoxImpl;
import org.appdapter.gui.box.ScreenBoxPanel;
import org.appdapter.gui.box.ScreenBoxPanel.Kind;
import org.appdapter.gui.box.ScreenBoxTreeNode;
import org.appdapter.gui.browse.BrowsePanel;
import org.appdapter.gui.demo.DemoNavigatorCtrl;
import org.appdapter.gui.demo.triggers.BridgeTriggers;
import org.appdapter.gui.demo.triggers.DatabaseTriggers;
import org.appdapter.gui.demo.triggers.RepoTriggers;
import org.appdapter.gui.repo.MutableRepoBox;
import org.appdapter.gui.repo.RepoBox;
import org.appdapter.gui.repo.RepoBoxImpl;
import org.appdapter.gui.repo.RepoModelBoxImpl;
import org.appdapter.gui.trigger.BootstrapTriggerFactory;
import org.appdapter.gui.trigger.SysTriggers;
import org.appdapter.impl.store.FancyRepo;
import org.appdapter.impl.store.QueryHelper;
import org.appdapter.scafun.BoxOne;
import org.appdapter.scafun.TriggerOne;
import org.matheclipse.core.reflection.system.For;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import java.lang.annotation.*;

/**
 * @author Stu B. <www.texpedient.com>
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
public interface AnyOper {

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface UISalient {
    }

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface SalientVoidCall {
        /**
         *  "" = use the splitted of camelcase for methodname
         * @return 
         */
        public String Named() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface ToStringResult {
    }

    @Retention(RetentionPolicy.RUNTIME)
    static public @interface Named {
        /**
         *  "" = use the splitted of camelcase for methodname
         * @return 
         */
        public String MenuName() default ""; 
    }
}
