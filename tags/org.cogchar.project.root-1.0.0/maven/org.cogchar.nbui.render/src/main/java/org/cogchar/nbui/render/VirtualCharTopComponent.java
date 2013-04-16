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

package org.cogchar.nbui.render;

import java.io.File;
import java.util.Properties;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelRobotUtils;

import org.cogchar.bind.rk.robot.svc.RobotServiceFuncs;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.osgi.framework.BundleContext;
import org.robokind.api.common.osgi.OSGiUtils;
import org.robokind.api.common.services.ServiceConnectionDirectory;
import org.robokind.api.motion.Robot;
import org.robokind.api.motion.jointgroup.JointGroup;
import org.robokind.api.motion.jointgroup.RobotJointGroup;
import org.cogchar.bundle.app.puma.PumaAppContext;
import org.cogchar.bundle.app.puma.PumaDualCharacter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Top component which displays the OpenGL Canvas
 * @author Matthew Stevenson <www.robokind.org>
 */
@ConvertAsProperties(dtd = "-//org.cogchar.nbui//VirtualChar//EN",
autostore = false)
public final class VirtualCharTopComponent extends TopComponent {
	static Logger theLogger = LoggerFactory.getLogger(VirtualCharTopComponent.class);
    private static VirtualCharTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "VirtualCharTopComponent";
    
    private boolean myInitializedFlag;
    
    public VirtualCharTopComponent() {
        //initComponents();
        setName(NbBundle.getMessage(VirtualCharTopComponent.class, "CTL_VirtualCharTopComponent"));
        setToolTipText(NbBundle.getMessage(VirtualCharTopComponent.class, "HINT_VirtualCharTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        myInitializedFlag = false;
    }
    
    private synchronized void init(BundleContext bundleCtx) throws Throwable {
		theLogger.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX - Simulator init() BEGIN");
        if(myInitializedFlag){
            return;
        }
        if(bundleCtx == null){
            throw new NullPointerException();
        }

		String dualCharURI = "NBURI:huzzah"; // =>  org_cogchar_nbui_render/bonyRobotConfig.json"));        
		PumaAppContext pac = new PumaAppContext(bundleCtx);
		BonyRenderContext brc = pac.getBonyRenderContext(dualCharURI);
		initVirtualCharPanel(brc);
		
		// Requires access to sun.misc.Unsafe, which must be explicitly imported by 
		// ext.bundle.osgi.jmonkey, and explicitly allowed by the container using
		// netigso
		pac.startOpenGLCanvas(dualCharURI, false);
/*		
		PumaDualCharacter pdc = pac.connectDualRobotChar(dualCharURI);	
        File file = new File("org_cogchar_nbui_render/jointgroup.xml");
        RobotServiceFuncs.registerJointGroup(bundleCtx, file);
*/		
        myInitializedFlag = true;
		theLogger.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX - Simulator init() END");
    }
    
    private void initVirtualCharPanel(BonyRenderContext BonyRenderContext) throws Throwable {
        if(BonyRenderContext == null){
			theLogger.error("BonyRenderContext is null");
            throw new Exception("BonyRenderContext is null");
        }
        myVirtualCharPanel = BonyRenderContext.getPanel();
        if(myVirtualCharPanel == null){
			theLogger.error("VirtualCharPanel is null");
            throw new Exception("VirtualCharPanel is null");
        }
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myVirtualCharPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myVirtualCharPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myVirtualCharPanel = new javax.swing.JPanel();

        javax.swing.GroupLayout myVirtualCharPanelLayout = new javax.swing.GroupLayout(myVirtualCharPanel);
        myVirtualCharPanel.setLayout(myVirtualCharPanelLayout);
        myVirtualCharPanelLayout.setHorizontalGroup(
            myVirtualCharPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        myVirtualCharPanelLayout.setVerticalGroup(
            myVirtualCharPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myVirtualCharPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(myVirtualCharPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel myVirtualCharPanel;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized VirtualCharTopComponent getDefault() {
        if (instance == null) {
            instance = new VirtualCharTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the VirtualCharTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized VirtualCharTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            theLogger.warn(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof VirtualCharTopComponent) {
            return (VirtualCharTopComponent) win;
        }
        theLogger.warn(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        try{
            BundleContext context = OSGiUtils.getBundleContext(Robot.class);
            if(context == null){
                throw new NullPointerException(
                        "Cannot find BundleContext for" + Robot.class);
            }
            init(context);
        }catch(Throwable t){
            theLogger.warn("Error initializing Cogchar-Robokind binding or OpenGL rendering.", t);
        }
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}