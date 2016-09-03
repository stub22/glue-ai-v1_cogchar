package org.cogchar.render.app.entity;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Stub22 on 5/15/2016.
 */
public class VWorldEntityReg extends BasicDebugger {

	private Map<Ident, VWorldEntity> myEntsByID =  new HashMap<Ident, VWorldEntity>();

	public void addGoody(VWorldEntity newGoody) {
		if (newGoody != null) {
			Ident goodyUri = newGoody.getUri();
			getLogger().info("Adding Goody with URI: {}", goodyUri);
			myEntsByID.put(goodyUri, newGoody);
		} else {
			getLogger().warn("Something is attempting to add a null goody to the GoodySpace, ignoring");
		}
	}

	public void removeGoody(VWorldEntity departingGoody) {
		departingGoody.detachFromVirtualWorldNode(VWorldEntity.QueueingStyle.QUEUE_AND_RETURN); // Safe to perform even if it's not currently attached
		myEntsByID.remove(departingGoody.getUri());
	}

	// Providing this so that CinematicMgr can access goodies to use in Cinematics
	public VWorldEntity getGoody(Ident goodyUri) {
		return myEntsByID.get(goodyUri);
	}

	public boolean hasGoodyAt(Ident goodyUri) {
		return myEntsByID.containsKey(goodyUri);
	}

	public Collection<VWorldEntity> getAllGoodies() {
		return myEntsByID.values();
	}

}
