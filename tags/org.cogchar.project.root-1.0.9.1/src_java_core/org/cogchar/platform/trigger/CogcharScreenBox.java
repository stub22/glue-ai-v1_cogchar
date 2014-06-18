/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.platform.trigger;
import org.appdapter.trigger.bind.jena.FullBox;
import org.appdapter.trigger.bind.jena.FullTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 * FullBox extends ScreenBoxImpl
 * 
 * Because it is a KnownComponent, it can:
 * 	public Ident getIdent();
	public String getDescription();
	public String getShortLabel();
  and because it is a MutableKnownComponent:
 	public void setIdent(Ident id);
	public void setDescription(String description);
	public void setShortLabel(String shortLabel);

 * Because it is a Box, it can do these things:
 * 	public BoxContext getBoxContext();   
	public List<TrigType> getTriggers();
	* 
* And because it is a MutableBox, it can:
	void attachTrigger(TrigType bt);
	void clearTriggers()  
	void setContext(BoxContext bc);
	* 
The BoxContext can do these things
 * 	public Box getRootBox();
	public Box getParentBox(Box child);
	public List<Box> getOpenChildBoxes(Box parent);
	public <BT extends Box<TT>, TT extends Trigger<BT>> List<BT> getOpenChildBoxesNarrowed(Box parent, Class<BT> boxClass, Class<TT> trigClass);
	public void contextualizeAndAttachChildBox(Box<?> parentBox, MutableBox<?> childBox);

* 
Because it is a ScreenBox, it can, publicly:
 	public DisplayContext getDisplayContext();
	void setDisplayContextProvider(DisplayContextProvider dcp);	
	public ScreenBoxPanel findBoxPanel(ScreenBoxPanel.Kind kind);

where the DisplayContext can simply:
    public JTabbedPane getBoxPanelTabPane();
 
and where a ScreenBoxPanel is a JPanel, of one of these types
	public enum Kind {MATRIX,DB_MANAGER,REPO_MANAGER,OTHER
 that can:
	public abstract void focusOnBox(BoxType b);
	
	With protected scope, this CogcharScreenBox also gets, from ScreenBoxImpl:
		protected void putBoxPanel(ScreenBoxPanel.Kind kind, ScreenBoxPanel bp) 
		protected ScreenBoxPanel getBoxPanel(ScreenBoxPanel.Kind kind) 
		protected ScreenBoxPanel makeBoxPanel(ScreenBoxPanel.Kind kind) 
		protected ScreenBoxPanel makeOtherPanel() 
	
	By overriding makeOtherPanel(), our CogcharScreenBox can return any type
	of OTHER screenBoxPanel that is appropriate for interacting with its content.
	* 
	* 
Current examples of CogcharScreenBox are:
*	PumaDualCharacter
*	Theater
* 
We consider adding:
*	PumaAppContext
*	BonyGameFeatureAdapter
* 
*   WorldModel
* 
*   CinematicHooHaManager
*	SuperDuperConfigManager
* 
* RepoBrowseAdapter
* 
* RegistryBrowseAdapter
* 
* VisionMonitor
* EgosphereMonitor
*	
 */
public class CogcharScreenBox extends FullBox<CogcharActionTrigger> {

}
