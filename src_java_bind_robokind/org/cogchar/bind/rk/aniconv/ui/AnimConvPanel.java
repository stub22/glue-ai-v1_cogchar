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
	
    private ConfigSelector myConfigSelector;
	private MayaModelSelector myMayaMapSelector;
	private List<BoneRobotConfig> myConfigs;
    private List<MayaModelMap> myMayaMaps;
	private Listener<BoneRobotConfig> myConfigAddListener;
    private Listener<BoneRobotConfig> myConfigRemoveListener;
    private Listener<MayaModelMap> myMayaMapAddListener;
    private Listener<MayaModelMap> myMayaMapRemoveListener;
	private Animation lastConvertedAnim;
	
	private boolean useMayaMap;
	
	private File[] filesToConvert = null; // Adding this array to allow batches of files to be selected and held ready for conversion - Ryan Biggs 15 Nov 2012
    /** Creates new form AnimConvPanel */
    public AnimConvPanel() {
        initComponents();
		// LOTS of duplication here to factor out as possible...
		myConfigs = new ArrayList<BoneRobotConfig>();
        myMayaMaps = new ArrayList<MayaModelMap>();
		myConfigAddListener = new AddConfigListener();
		myConfigRemoveListener = new RemoveConfigListener();
        myMayaMapAddListener = new AddMayaMapListener();
        myMayaMapRemoveListener = new RemoveMayaMapListener();
    }
	class AddConfigListener implements Listener<BoneRobotConfig> {
        @Override public void handleEvent(BoneRobotConfig input) {
            addConfig(input);
        }
    }
    class RemoveConfigListener implements Listener<BoneRobotConfig> {
        @Override public void handleEvent(BoneRobotConfig input) {
            removeConfig(input);
        }
    }
    class AddMayaMapListener implements Listener<MayaModelMap> {
        @Override public void handleEvent(MayaModelMap input) {
            addMayaMap(input);
        }
    }
    class RemoveMayaMapListener implements Listener<MayaModelMap> {
        @Override public void handleEvent(MayaModelMap input) {
            removeMayaMap(input);
        }
    }
	
	public void setConfigSelector(ConfigSelector selector){
        if(myConfigSelector != null){
            myConfigSelector.getAddNotifier().removeListener(myConfigAddListener);
            myConfigSelector.getRemoveNotifier().removeListener(myConfigRemoveListener);
        }
        myConfigSelector = selector;
        updateConfigList();
        if(myConfigSelector != null){
            myConfigSelector.getAddNotifier().addListener(myConfigAddListener);
            myConfigSelector.getRemoveNotifier().addListener(myConfigRemoveListener);
        }
    }
    public void setMayaMapSelector(MayaModelSelector selector){
        if(myMayaMapSelector != null){
            myMayaMapSelector.getAddNotifier().removeListener(myMayaMapAddListener);
            myMayaMapSelector.getRemoveNotifier().removeListener(myMayaMapRemoveListener);
        }
        myMayaMapSelector = selector;
        updateMayaMapList();
        if(myMayaMapSelector != null){
            myMayaMapSelector.getAddNotifier().addListener(myMayaMapAddListener);
            myMayaMapSelector.getRemoveNotifier().addListener(myMayaMapRemoveListener);
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
        comboConfig.removeAllItems();
        if(myMayaMapSelector == null){
            return;
        }
        
        List<MayaModelMap> configs = myMayaMapSelector.getAvailableConfigs();
        
        for(MayaModelMap config : configs) {
            myMayaMaps.add(config);
            comboConfig.addItem(config.myUri.getLocalName());
        }
        
    }
    
	public BoneRobotConfig getSelectedBoneConfig(){
        return myConfigs.get(comboBoneConfig.getSelectedIndex());
    }
    public MayaModelMap getSelectedMayaMap(){
        return myMayaMaps.get(comboConfig.getSelectedIndex());
    }
    
	private void addConfig(BoneRobotConfig config){
        if(myConfigs.contains(config)){
            return;
        }
        myConfigs.add(config);
        comboBoneConfig.addItem(config.myRobotName);
    }
    private void addMayaMap(MayaModelMap config){
        if(myMayaMaps.contains(config)){
            return;
        }
        myMayaMaps.add(config);
        comboConfig.addItem(config.myUri.getLocalName());
    }
    
	 private void removeConfig(BoneRobotConfig config){
        int i = myConfigs.indexOf(config);
        if(i < 0){
            return;
        }
        myConfigs.remove(i);
        comboBoneConfig.removeItemAt(i);
    }
    private void removeMayaMap(MayaModelMap config){
        int i = myMayaMaps.indexOf(config);
        if(i < 0){
            return;
        }
        myMayaMaps.remove(i);
        comboConfig.removeItemAt(i);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtChooseFile = new javax.swing.JTextField();
        btnChooseFile = new javax.swing.JButton();
        txtOutputFile = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        btnConvert = new javax.swing.JButton();
        txtAnimationName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        comboConfig = new javax.swing.JComboBox();
        btnPlay = new javax.swing.JButton();
        comboBoneConfig = new javax.swing.JComboBox();
        mapCheckBox = new javax.swing.JCheckBox();
        boneConfigCheckBox = new javax.swing.JCheckBox();

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

        comboConfig.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnPlay.setText("Play Last Conversion");
        btnPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlayActionPerformed(evt);
            }
        });

        comboBoneConfig.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        comboBoneConfig.setActionCommand("boneBoxChanged");

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mapCheckBox)
                            .addComponent(boneConfigCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(comboBoneConfig, 0, 120, Short.MAX_VALUE)
                            .addComponent(comboConfig, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPlay, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnConvert, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addComponent(txtAnimationName)))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnConvert)
                        .addGap(44, 44, 44)
                        .addComponent(btnPlay))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboBoneConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(boneConfigCheckBox))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(comboConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(mapCheckBox))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
								animName, skeleton, config, useMayaMap, st);

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
        // TODO add your handling code here:
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
    private javax.swing.JButton btnChooseFile;
    private javax.swing.JButton btnConvert;
    private javax.swing.JButton btnPlay;
    private javax.swing.JComboBox comboBoneConfig;
    private javax.swing.JComboBox comboConfig;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JCheckBox mapCheckBox;
    private javax.swing.JTextField txtAnimationName;
    private javax.swing.JTextField txtChooseFile;
    private javax.swing.JTextField txtOutputFile;
    // End of variables declaration//GEN-END:variables
}
