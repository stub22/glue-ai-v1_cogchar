package org.cogchar.bind.rk.aniconv;

import java.io.FileWriter;
import org.robokind.api.animation.ControlPoint;

/**
 *
 * @author Matthew Stevenson <www.robokind.org>
 */


public class OgreAnimationWriter {
    public static void writeFile(String path, AnimationData ad) throws Exception{
        FileWriter fw = new FileWriter(path);
        fw.append(getAnimationData(ad));
        fw.flush();
        fw.close();
    }
    
    public static String getAnimationData(AnimationData ad){
        StringBuilder sb = new StringBuilder();
        for(ChannelData<Double> cd : ad.getChannels()){
            addChanData(sb, cd);
        }
        return sb.toString();
    }
    
    private static void addChanData(StringBuilder sb, ChannelData<Double> cd){
        sb.append("anim ").append(cd.getName())
                .append("\n{\n")
                .append("\t//Time   /    Value");
        for(ControlPoint<Double> p : cd.getPoints()){
            sb.append("\n\t").append(p.getTime())
                    .append("\t").append(p.getPosition());
        }
        sb.append("\n}\n\n");
    }
}
