package org.cogchar.bind.rk.aniconv;

import java.io.FileReader;
import java.io.StreamTokenizer;
import org.robokind.api.animation.ControlPoint;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */


public class AnimationTrimmer {
    public static void trimAnimation(String animPath, String outPath) throws Exception {
        OgreAnimationWriter.writeFile(outPath, getTrimData(animPath));
    }
    
    public static AnimationData getTrimData(String fileName) throws Exception{
        StreamTokenizer st = new StreamTokenizer(new FileReader(fileName));
        AnimationData d = OgreAnimationParser.parseAnimation(fileName, st);
        AnimationData newData = new AnimationData(fileName);
        for(ChannelData<Double> cd : d.getChannels()){
            if(positionsChange(cd) 
                    && !cd.getName().contains(":cc_") 
                    && !cd.getName().contains(":grp_cc_") 
                    && cd.getName().contains("_r_")
                    && (cd.getName().contains("_shoulder")
                        || cd.getName().contains("_arm")
                        || cd.getName().contains("_elbow"))){
                newData.addChannel(cd);
            }
        }
        return newData;
    }
    
    static boolean positionsChange(ChannelData<Double> cd){
        Double prev = null;
        for(ControlPoint<Double> p : cd.getPoints()){
            if(p == null){
                continue;
            }else if(prev == null){
                prev = p.getPosition();
                continue;
            }
            Double val = p.getPosition();
            if(val.compareTo(prev) != 0){
                return true;
            }
        }
        if(cd.getPoints().size() == 1){
            return true;
        }
        return false;
    }
}
