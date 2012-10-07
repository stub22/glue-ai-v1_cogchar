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
import org.appdapter.scafun.FullBox;
import org.appdapter.scafun.FullTrigger;
import org.appdapter.scafun.BoxOne;
import org.appdapter.scafun.FullTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 * FullBox extends ScreenBoxImpl
 * 
 * Because it is a Box, it can do these things:
 * 	public BoxContext getBoxContext();
	public List<TrigType> getTriggers();
	* 
	* Because it is a MutableBox, it can:
	void attachTrigger(TrigType bt);
	void setContext(BoxContext bc);
	* 
The BoxContext can do these things
 * 	public Box getRootBox();
	public Box getParentBox(Box child);
	public List<Box> getOpenChildBoxes(Box parent);
	public <BT extends Box<TT>, TT extends Trigger<BT>> List<BT> getOpenChildBoxesNarrowed(Box parent, Class<BT> boxClass, Class<TT> trigClass);
	public void contextualizeAndAttachChildBox(Box<?> parentBox, MutableBox<?> childBox);

* 
Because it is a ScreenBox, it can:
 	public DisplayContext getDisplayContext();
	void setDisplayContextProvider(DisplayContextProvider dcp);	
	public ScreenBoxPanel findBoxPanel(ScreenBoxPanel.Kind kind);

where the DisplayContext can simply:
    public JTabbedPane getBoxPanelTabPane();
 
and where a ScreenBoxPanel can:
	public enum Kind {
		MATRIX,
		DB_MANAGER,
		REPO_MANAGER,
		OTHER
	}
	public abstract void focusOnBox(BoxType b);
 */
public class CogcharScreenBox extends FullBox<CogcharActionTrigger> {

}
