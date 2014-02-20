package org.cogchar.app.puma.event;

/**
 *
 * @author robokind
 */


public interface PumaNotifier {

    
    public void pumaBootNotifier();
    public void pumaClosingNotifier();
    public void pumaChangedNotifier();
    
}
