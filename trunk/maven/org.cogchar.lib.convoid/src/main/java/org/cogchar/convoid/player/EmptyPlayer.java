/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.convoid.player;

import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext.Detail;
import java.util.ArrayList;
import java.util.List;
import org.cogchar.convoid.broker.ConvoidFacadeSource;
import org.cogchar.zzz.platform.stub.ThalamentStub;

/**
 *
 * @author matt
 */
public class EmptyPlayer implements IBehaviorPlayable{
    public void run(ConvoidFacadeSource igf) {}
    public PlayerAction getAction(){
        return PlayerAction.EMPTY;
    }
    public List<Detail> getDetails() {
        return new ArrayList<Detail>();
    }

    public Step getStep() {
        return null;
    }

    public void addDetail(Detail detail) {}

    public void setCause(ThalamentStub t) {}
}
