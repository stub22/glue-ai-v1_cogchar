package org.cogchar.app.puma.event;

import org.appdapter.core.name.Ident;
//import java.util.concurrent.Future;

/**
 *
 * @author robokind
 */


public interface Updater {
    
    public boolean triggerStartAnimation(Ident uri);
    public boolean triggerStopAnimation(Ident uri);
    public boolean databallUpdate(String action, String text);
    public void processUpdate(String Request, boolean forceFreshDefaultRepo);
    
    
}
