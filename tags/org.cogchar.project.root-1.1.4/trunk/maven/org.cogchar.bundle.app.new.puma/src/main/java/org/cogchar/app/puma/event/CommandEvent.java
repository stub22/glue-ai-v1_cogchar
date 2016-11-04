package org.cogchar.app.puma.event;

import org.appdapter.core.name.Ident;
/**
 *
 * @author Major Jacquote II <mjacquote@gmail.com>
 *
 */
public class CommandEvent {

    public enum EventType {

        START_ANIMATION,
        STOP_ANIMATION,
        DATABALL_GOODY,
        UPDATE
    }
    
    private Updater updater;
    
    public void setUpdater(Updater updater)
    {
        this.updater=updater;
    }
   
    public boolean update(EventType e, Ident ident, String action, String text, boolean forceFreshDefaultRepo) {

        boolean result = false;

        if (e == EventType.START_ANIMATION) {
            updater.triggerStartAnimation(ident);
        }
        if (e == EventType.STOP_ANIMATION) {
            updater.triggerStopAnimation(ident);
        }
        if (e == EventType.DATABALL_GOODY) {
            
            updater.databallUpdate(action, text);
        }
        if (e == EventType.UPDATE) {
            
            updater.processUpdate(action, forceFreshDefaultRepo);
        }



        return result;
    }
    
    public boolean checkUpdater()
    {
        if(updater==null)
        {
            return false;
        }
        
        return true;
    }
}
