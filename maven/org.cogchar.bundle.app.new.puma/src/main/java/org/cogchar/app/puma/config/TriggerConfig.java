package org.cogchar.app.puma.config;

import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.platform.trigger.BoxSpace;
import org.appdapter.fancy.rclient.RepoClient;

/**
 *
 * @author robokind
 */
public class TriggerConfig {
    
    private CommandSpace commandSpace;
    private BoxSpace boxSpace;
    private RepoClient repoClient;
    
    
    public CommandSpace getCommandSpace()
    {
        return commandSpace;
    }
    
    public BoxSpace getBoxSpace()
    {
        return boxSpace;
    }
    
    public RepoClient getRepoClient()
    {
        return repoClient;
    }
    
    public void setRepoClient(RepoClient repo)
    {
        repoClient=repo;
    }
    
    public void setBoxSpace(BoxSpace boxSpace)
    {
        this.boxSpace=boxSpace;
    }
    
    public void setCommandSpace(CommandSpace commandSpace)
    {
        this.commandSpace=commandSpace;
    }
}
