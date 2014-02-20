package org.cogchar.app.puma.event;

/**
 *
 * @author robokind
 */

import org.jflux.api.core.Listener;
import org.jflux.api.core.Notifier;
import org.jflux.api.core.util.DefaultNotifier;


public class PumaNotifierImpl implements PumaNotifier {
    
    private Notifier<String> bootStatus;
    private Notifier<String> closingStatus;
    private Notifier<String> changeNotifier;
    
    public PumaNotifierImpl()
    {
        bootStatus=new DefaultNotifier<String>();
        closingStatus=new DefaultNotifier<String>();
        changeNotifier=new DefaultNotifier<String>();
        
    }
    
    public void pumaBootNotifier() {
    }

    public void pumaClosingNotifier() {
    }

    public void pumaChangedNotifier() {
    }
}
