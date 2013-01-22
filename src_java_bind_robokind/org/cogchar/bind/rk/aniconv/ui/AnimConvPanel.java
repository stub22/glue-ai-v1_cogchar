/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AnimConvPanel.java
 *
 * Created on Jun 26, 2012, 3:09:28 PM
 */
package org.cogchar.bind.rk.aniconv.ui;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.bind.rk.aniconv.AnimationConverter;
import org.cogchar.bind.rk.aniconv.MayaModelMap;
import org.jflux.api.core.Listener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.animation.utils.AnimationUtils;
import org.robokind.api.animation.xml.AnimationFileWriter;
import org.robokind.api.animation.xml.AnimationXML;
import org.robokind.api.common.osgi.OSGiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Stevenson
 * 
 * edited by Ryan Biggs Nov 2012 - Jan 2013
 */
public class AnimConvPanel extends javax.swing.JPanel {
	
	private static Logger theLogger = LoggerFactory.getLogger(AnimConvPanel.class);
	
    private ConfigSelector<BoneRobotConfig> myConfigSelector;
	private ConfigSelector<MayaModelMap> myMayaMapSelector;
	private List<BoneRobotConfig> myConfigs;
    private List<MayaModelMap> myMayaMaps;
	private Listener<BoneRobotConfig> myConfigAddListener;
    private Listener<BoneRobotConfig> myConfigRemoveListener;
    private Listener<MayaModelMap> myMayaMapAddListener;
    private Listener<MayaModelMap> myMayaMapRemoveListener;
	private Animation lastConvertedAnim;
	
	private boolean useMayaMap;
	private boolean useControlCurves = true;
	
	private File[] filesToConvert = null; // Adding this array to allow batches of files to be selected and held ready for conversion - Ryan Biggs 15 Nov 2012
    /** Creates new form AnimConvPanel */
    public AnimConvPanel() {
        initComponents();
		myConfigs = new ArrayList<BoneRobotConfig>();
        myMayaMaps = new ArrayList<MayaModelMap>();
		myConfigAddListener = new AddConfigListener<BoneRobotConfig>();
		myConfigRemoveListener = new RemoveConfigListener<BoneRobotConfig>();
        myMayaMapAddListener = new AddConfigListener<MayaModelMap>();
        myMayaMapRemoveListener = new RemoveConfigListener<MayaModelMap>();
    }
	class AddConfigListener<T> implements Listener<T> {
        @Override public void handleEvent(T input) {
            addConfig(input);
        }
    }
    class RemoveConfigListener<T> implements Listener<T> {
        @Override public void handleEvent(T input) {
            removeConfig(input);
        }
    }
	
	public void setConfigSelector(ConfigSelector selector){
		ConfigSelector selectorToSet;
		Listener addListener;
		Listener removeListener;
		boolean mayaMapUsed = false;
		if (selector.getType() == BoneRobotConfig.class) {
			selectorToSet = myConfigSelector;
			addListener = myConfigAddListener;
			removeListener = myConfigRemoveListener;
		} else if (selector.getType() == MayaModelMap.class) {
			selectorToSet = myMayaMapSelector;
			addListener = myMayaMapAddListener;
			removeListener = myMayaMapRemoveListener;
			mayaMapUsed = true;
		} else {
			theLogger.error("Method called with invalid ConfigSelector class: {}", selector.getType().getName());
			return;
		}
        if(selectorToSet != null){
            selectorToSet.getAddNotifier().removeListener(addListener);
            selectorToSet.getRemoveNotifier().removeListener(removeListener);
        }
        selectorToSet = selector;
		if (mayaMapUsed) {
			updateMayaMapList();
		} else {
			updateConfigList();
		}
        if(selectorToSet != null){
            selectorToSet.getAddNotifier().addListener(addListener);
            selectorToSet.getRemoveNotifier().addListener(removeListener);
        }
    }
    
	public void updateConfigList(){
        myConfigs.clear();
        comboBoneConfig.removeAllItems();
        if(myConfigSelector == null){
            return;
        }
        
        List<BoneRobotConfig> configs = myConfigSelector.getAvailableConfigs();
        
        for(BoneRobotConfig config : configs) {
            myConfigs.add(config);
            comboBoneConfig.addItem(config.myRobotName);
        }
        
    }
    public void updateMayaMapList(){
        myMayaMaps.clear();
        comboMayaMaps.removeAllItems();
        if(myMayaMapSelector == null){
            return;
        }
        
        List<MayaModelMap> configs = myMayaMapSelector.getAvailableConfigs();
        
        for(MayaModelMap config : configs) {
            myMayaMaps.add(config);
            comboMayaMaps.addItem(config.myUri.getLocalName());
        }
        
    }
    
	public BoneRobotConfig getSelectedBoneConfig(){
        return myConfigs.get(comboBoneConfig.getSelectedIndex());
    }
    public MayaModelMap getSelectedMayaMap(){
        return myMayaMaps.get(comboMayaMaps.getSelectedIndex());
    }
    
	private void addConfig(Object config){
		ConfigListAndSelector las = determineConfigListAndSelector(config);
		if (!las.success) {return;}
        if(las.configList.contains(config)){
            return;
        }
        las.configList.add(config);
		if (las.selectorBox == comboBoneConfig) {
			las.selectorBox.addItem(((BoneRobotConfig)config).myRobotName);
		} else {
			las.selectorBox.addItem(((MayaModelMap)config).myUri.getLocalName());
		}
	}
    
	 private void removeConfig(Object config){
		ConfigListAndSelector las = determineConfigListAndSelector(config);
		if (!las.success) {return;}
        int i = las.configList.indexOf(config);
        if(i < 0){
            return;
        }
        las.configList.remove(i);
        las.selectorBox.removeItemAt(i);
    }
	 
	private ConfigListAndSelector determineConfigListAndSelector(Object configObject) {
		ConfigListAndSelector output = new ConfigListAndSelector();
		if (configObject instanceof BoneRobotConfig) {
			output.configList = myConfigs; 
			output.selectorBox = comboBoneConfig;
		} else if (configObject instanceof MayaModelMap) {
			output.configList = myMayaMaps;
			output.selectorBox = comboMayaMaps;
		} else {
			output.success = false;
			theLogger.error("Method called with invalid config object class: {}", configObject.getClass().getName());
		}
		return output;
	}
	
	private class ConfigListAndSelector {
		List configList;
		JComboBox selectorBox;
		boolean success = true;
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup2 = new javax.swing.ButtonGroup();
        txtChooseFile = new javax.swing.JTextField();
        btnChooseFile = new javax.swing.JButton();
        txtOutputFile = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnConvert = new javax.swing.JButton();
        txtAnimationName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        comboMayaMaps = new javax.swing.JComboBox();
        btnPlay = new javax.swing.JButton();
        comboBoneConfig = new javax.swing.JComboBox();
        mapCheckBox = new javax.swing.JCheckBox();
        boneConfigCheckBox = new javax.swing.JCheckBox();
        controlCurveRadioButton = new javax.swing.JRadioButton();
        boneRotationRadioButton = new javax.swing.JRadioButton();

        btnChooseFile.setText("Choose File");
        btnChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChooseFileActionPerformed(evt);
            }
        });

        jLabel1.setText("Output File");

        btnConvert.setText("Convert");
        btnConvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConvertActionPerformed(evt);
            }
        });

        txtAnimationName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAnimationNameActionPerformed(evt);
            }
        });

        jLabel2.setText("Animation Name:");

        jLabel3.setText("Input File");

        comboMayaMaps.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboMayaMaps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mayaMapSelected(evt);
            }
        });

        btnPlay.setText("Play Last Conversion");
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });

        comboBoneConfig.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoneConfig.setActionCommand("boneBoxChanged");
        comboBoneConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boneConfigSelected(evt);
            }
        });

        mapCheckBox.setText("Use Conversion Map:");
        mapCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapCheckBoxActionPerformed(evt);
            }
        });

        boneConfigCheckBox.setSelected(true);
        boneConfigCheckBox.setText("Use Bone Config:");
        boneConfigCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boneConfigCheckBoxActionPerformed(evt);
            }
        });

        buttonGroup2.add(controlCurveRadioButton);
        controlCurveRadioButton.setSelected(true);
        controlCurveRadioButton.setText("with Control Curves");
        controlCurveRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controlCurveRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(boneRotationRadioButton);
        boneRotationRadioButton.setText("with Baked bone rotations");
        boneRotationRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boneRotationRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(boneConfigCheckBox)
                                .addGap(36, 36, 36)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(controlCurveRadioButton)
                                        .addGap(30, 30, 30))
                                    .addComponent(boneRotationRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(comboBoneConfig, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnConvert, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPlay))
                    .addComponent(txtOutputFile)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtChooseFile, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnChooseFile))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(txtAnimationName))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(mapCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(comboMayaMaps, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtChooseFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnChooseFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtOutputFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtAnimationName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboBoneConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(boneConfigCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlCurveRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(boneRotationRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboMayaMaps, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(mapCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPlay)
                    .addComponent(btnConvert))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChooseFileActionPerformed

		File[] files = chooseFile();
        if(files == null){
            return;
        }
		
		filesToConvert = files;
		
		// Yeah this part could use some refactoring. 
		if (files.length == 1) {
			File file = files[0];
			txtChooseFile.setText(file.getAbsolutePath());
			txtOutputFile.setText(file.getAbsolutePath() + ".xml");

			int animIndex = file.getName().lastIndexOf(".anim");

			txtAnimationName.setText(file.getName().substring(0, animIndex));
		} else {
			String multiText = "Multiple Files";
			txtChooseFile.setText(multiText);
			txtOutputFile.setText(multiText);

			txtAnimationName.setText(multiText);
		}
        setConvertButtonDefaultText();
    }//GEN-LAST:event_btnChooseFileActionPerformed

    private void btnConvertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConvertActionPerformed

		btnConvert.setText("Converting...");
		btnConvert.setBackground(Color.RED);
		
		// This may take a while, so put it on a worker thread. This allows GUI updates to be processed in the meanwhile:
		SwingWorker worker = new SwingWorker<Void, Void>() {

			@Override
			public Void doInBackground() {
				MayaModelMap config = getSelectedMayaMap();
				BoneRobotConfig skeleton = getSelectedBoneConfig(); 
				for (File file : filesToConvert) {
					int animIndex = file.getName().lastIndexOf(".anim");
					String animName = (filesToConvert.length == 1) ? txtAnimationName.getText() : file.getName().substring(0, animIndex);
					String inFile = file.getAbsolutePath();
					String outFile = (filesToConvert.length == 1) ? txtOutputFile.getText() : file.getAbsolutePath() + ".xml";
					try {
						StreamTokenizer st = new StreamTokenizer(new FileReader(inFile));
						Animation anim = AnimationConverter.convertAnimation(
								animName, skeleton, config, useMayaMap, useControlCurves, st);

						lastConvertedAnim = anim;

						AnimationFileWriter animWriter = AnimationXML.getRegisteredWriter();

						// The final null argument was added Oct 2012 for compatibility with current AnimationFileWriter 
						animWriter.writeAnimation(
								outFile, anim, AnimationUtils.getChannelsParameterSource(), null); // null should be Set<Synchronized Point Group>; SyncPointGroupXML.XPP3Writer.writeSyncGroups seems to handle null OK
					} catch (Exception e) {
						theLogger.error("Exception converting file: ", e);
						displayError(e.toString());
					}
				}
				return null;
			}
			
			@Override
			public void done() {
				btnConvert.setText("Complete!");
				btnConvert.setBackground(UIManager.getColor("Button.background"));
			}
		};
		worker.execute();
    }//GEN-LAST:event_btnConvertActionPerformed

    private void displayError(String error) {
        JOptionPane.showMessageDialog(btnConvert, error, "Error while converting", JOptionPane.ERROR_MESSAGE);
    }
    
    private void txtAnimationNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAnimationNameActionPerformed
        setConvertButtonDefaultText();
    }//GEN-LAST:event_txtAnimationNameActionPerformed

	private void btnPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPlayActionPerformed
		// Adapted from RK Workshop code:
		BundleContext context = OSGiUtils.getBundleContext(AnimationPlayer.class);
		if(context == null){
			theLogger.error("Unable to find BundleContext for AnimationPlayer");
			return;
		}

		ServiceReference ref = AnimationUtils.getAnimationPlayerReference(context, null);
		if(ref == null){
			theLogger.error("Unable to find ServiceReference for AnimationPlayer");
			return;
		}
		
		AnimationPlayer player = (AnimationPlayer)context.getService(ref);
		if(player != null){
			player.playAnimation(lastConvertedAnim);
		} else {
			theLogger.error("Unable to find AnimationPlayer from context");
		}
		
		context.ungetService(ref);
	}//GEN-LAST:event_btnPlayActionPerformed

	private void mapCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapCheckBoxActionPerformed
		AbstractButton abstractButton = (AbstractButton) evt.getSource();
        useMayaMap = abstractButton.getModel().isSelected();
		boneConfigCheckBox.setSelected(!useMayaMap);
		setConvertButtonDefaultText();
	}//GEN-LAST:event_mapCheckBoxActionPerformed

	private void boneConfigCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boneConfigCheckBoxActionPerformed
		AbstractButton abstractButton = (AbstractButton) evt.getSource();
		useMayaMap = !abstractButton.getModel().isSelected();
		mapCheckBox.setSelected(useMayaMap);
		setConvertButtonDefaultText();
	}//GEN-LAST:event_boneConfigCheckBoxActionPerformed

	private void boneConfigSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boneConfigSelected
		setConvertButtonDefaultText();
	}//GEN-LAST:event_boneConfigSelected

	private void mayaMapSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mayaMapSelected
		setConvertButtonDefaultText();
	}//GEN-LAST:event_mayaMapSelected

	private void controlCurveRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controlCurveRadioButtonActionPerformed
		AbstractButton abstractButton = (AbstractButton) evt.getSource();
		useControlCurves = abstractButton.getModel().isSelected();
		setConvertButtonDefaultText();
	}//GEN-LAST:event_controlCurveRadioButtonActionPerformed

	private void boneRotationRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boneRotationRadioButtonActionPerformed
		AbstractButton abstractButton = (AbstractButton) evt.getSource();
		useControlCurves = !abstractButton.getModel().isSelected();
		setConvertButtonDefaultText();
	}//GEN-LAST:event_boneRotationRadioButtonActionPerformed

    private File[] chooseFile(){
        JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true); // Allows multiple files to be selected for batch conversion
		//chooser.setCurrentDirectory(new File("c:\\dev\\hrk\\animations")); // TEST ONLY
        int ret = chooser.showOpenDialog(null);
        if(ret != JFileChooser.APPROVE_OPTION){
            return null;
        }
        return chooser.getSelectedFiles();
    }
	
	private void setConvertButtonDefaultText() {
		btnConvert.setText("Convert");
	}
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boneConfigCheckBox;
    private javax.swing.JRadioButton boneRotationRadioButton;
    private javax.swing.JButton btnChooseFile;
    private javax.swing.JButton btnConvert;
    private javax.swing.JButton btnPlay;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JComboBox comboBoneConfig;
    private javax.swing.JComboBox comboMayaMaps;
    private javax.swing.JRadioButton controlCurveRadioButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JCheckBox mapCheckBox;
    private javax.swing.JTextField txtAnimationName;
    private javax.swing.JTextField txtChooseFile;
    private javax.swing.JTextField txtOutputFile;
    // End of variables declaration//GEN-END:variables
}
