/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.convoid.player;

import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.player.BehaviorContext.Detail;
import java.util.List;
import org.cogchar.convoid.broker.ConvoidFacadeSource;
import org.cogchar.platform.stub.ThalamentStub;

/**
 *
 * @author matt
 */
public interface IBehaviorPlayable {
    public void run(ConvoidFacadeSource igf);
    public PlayerAction getAction();
    public List<Detail> getDetails();
    public Step getStep();
    public void addDetail(Detail detail);
    public void setCause(ThalamentStub t);
}
