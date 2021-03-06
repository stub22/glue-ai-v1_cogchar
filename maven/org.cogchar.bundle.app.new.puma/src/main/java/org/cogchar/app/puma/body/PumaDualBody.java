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
package org.cogchar.app.puma.body;

//import org.cogchar.app.puma.vworld.PumaVirtualWorldMapper;
import org.osgi.framework.BundleContext;

import org.appdapter.core.name.Ident;
import org.appdapter.fancy.rclient.RepoClient;

import org.cogchar.api.humanoid.FigureConfig;
import org.cogchar.name.skeleton.BoneCN;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.app.puma.config.BodyHandleRecord;

import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.registry.ResourceFileCategory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaDualBody extends BasicDebugger {

    private Ident myDualBodyID;
    private String myNickName;
    private PumaBodyGateway myBodyMapper;
    private BodyHandleRecord bodyConfig;

    public PumaDualBody(Ident dualBodyID, String nickName) {
        myDualBodyID = dualBodyID;
        myNickName = nickName;
        bodyConfig = null;

    }

    /**
     * Performs the substance of all body initialization, given explicit
     * instructions.
     *
     * @param prc
     * @param bundleCtx
     * @param rc
     * @param humCfg
     * @param graphIdentForBony
     * @throws Throwable
     */
    public void absorbContext(PumaRegistryClient prc, BundleContext bundleCtx, RepoClient rc,
            FigureConfig humCfg, Ident graphIdentForBony) throws Throwable {

        //PumaVirtualWorldMapper vWorldMapper = prc.getVWorldMapper(null);
        // It's OK if vWorldMapper == null.  We still construct a Humanoid Mapper, which will then 
        // exist solely for the purpose of forwarding joint commands to connected Robots.
        myBodyMapper = new PumaBodyGateway(bundleCtx, myDualBodyID);
        List<ClassLoader> mioConfCLs = prc.getResFileCLsForCat(ResourceFileCategory.RESFILE_MIO_CONF);

        boolean setupOK = setupBonyModelBindingToMechIO(bundleCtx, rc, graphIdentForBony, humCfg, mioConfCLs);
        if (bodyConfig != null) {
            bodyConfig.setModelRobot(myBodyMapper.getBonyRobot());
            //myBodyMapper.setBodyConfigSpec(bodyConfig);
        }
        //boolean vwHumOK = myBodyMapper.initVWorldHumanoid(rc, graphIdentForBony, humCfg);


    }

    public String getNickName() {
        return myNickName;
    }

    public Ident getCharIdent() {
        return myDualBodyID;
    }

    public PumaBodyGateway getBodyGateway() {
        return myBodyMapper;
    }

    public void setBodyConfigSpec(BodyHandleRecord spec) {
        bodyConfig = spec;
    }

    // This method is called once (for each character) when bony config update is requested
    public void updateBonyConfig(RepoClient qi, Ident graphID, BoneCN bqn) throws Throwable {
        BoneRobotConfig brc = new BoneRobotConfig(qi, myDualBodyID, graphID, bqn);
        myBodyMapper.updateModelRobotUsingBoneRobotConfig(brc);
    }

    @Override
    public String toString() {
        return "PumaDualChar[uri=" + myDualBodyID + ", nickName=" + myNickName + "]";
    }

    private boolean setupBonyModelBindingToMechIO(BundleContext bunCtx, RepoClient rc, Ident graphIdentForBony,
            FigureConfig hc, List<ClassLoader> clsForMioConf) {
        Ident charIdent = getCharIdent();
        getLogger().debug("Setup for {} using graph {} and humanoidConf {}", new Object[]{charIdent, graphIdentForBony, hc});
        try {
            BoneCN bqn = new BoneCN();
            boolean connectedOK = myBodyMapper.connectBonyRobotToMioAndVWorld(bunCtx, hc, graphIdentForBony, rc, bqn, clsForMioConf);
            if (connectedOK) {
                return true;
            } else {
                getLogger().warn("Failed to connect RK+VWorld bindings for character: {}", charIdent);
                return false;
            }
        } catch (Throwable t) {
            getLogger().error("Exception during setupCharacterBindingToRobokind for character: {}", charIdent, t);
            return false;
        }
    }
}
