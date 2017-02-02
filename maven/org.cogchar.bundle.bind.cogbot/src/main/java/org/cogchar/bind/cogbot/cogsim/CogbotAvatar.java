/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.cogsim;


import org.apache.http.client.HttpClient;
import org.cogchar.bind.cogbot.main.CogbotService;
import org.cogchar.bind.cogbot.scripting.CogbotPrimitive;
import org.cogchar.bind.cogbot.scripting.ObjectLispWriter;
import org.cogchar.bind.cogbot.scripting.SerialEventQueue;
import org.cogchar.bind.cogbot.unused.CogbotJMXClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_DEBUG_FLAG;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_ENABLED;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_JMX_ENABLED;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_JMX_URL;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.CONF_COGSIM_POLL_ENABLED;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.OLD_CONF_COGBOT_NAME;
import static org.cogchar.bind.cogbot.main.CogbotConfigUtils.getValue;

/**
 * @author Administrator
 */
public class CogbotAvatar implements NotificationListener, Serializable {
	private static final Logger theLogger = LoggerFactory.getLogger(CogbotAvatar.class);
	public transient CogbotService service;
	CogSimConf myCogSimConf;
	transient private CogSimBridge myCSB;
	transient final PrintWriter debugPw;
	transient Map<String, CogbotPrimitive> primitives = new HashMap<>();
	transient CogbotJMXClient cogbotJMXClient;
	final SerialEventQueue TODO_QUEUE;

	//public String USER_PARTNER = null;
	//public String UNKNOWN_PARTNER = "UNKNOWN_PARTNER";


	public String getBotId() {
		return getValue(String.class, OLD_CONF_COGBOT_NAME);
	}

	public CogSimConf getConfig() {
		return myCogSimConf;
	}

	public CogbotAvatar(CogbotService service0) {
		service = service0;
		debugPw = service0.getLogPrintWriter();
		TODO_QUEUE = new SerialEventQueue(getValue(String.class, OLD_CONF_COGBOT_NAME));
		myCogSimConf = service0.getConf();
		ensureJMX();
	}

	public synchronized void registerListener(DictationReciever dictationReciever) {
		myCSB = new CogSimBridge(this, dictationReciever, myCogSimConf);
		if (myCSB.isConfigured()) {
			Thread t = new Thread(myCSB);
			t.start();
		} else {
			theLogger.warn("CogSim connection is not configured, so no connection will be made to CogSim");
		}

		warnSettings();
	}

	public synchronized void postActionReqToCogbot(String verb, String details, boolean debugFlag) {
		if (!getValue(Boolean.class, CONF_COGSIM_ENABLED)) return;
		CogSimOp cso = makeCogSimOp(myCogSimConf, null);
		try {
			cso.postActionReqToCogbot(verb, details, debugFlag);
		} catch (Throwable t) {
			theLogger.warn("Cannot send cogbot-doit command[{}, {}]", verb, details, t);
		}
	}

	public synchronized void ensureJMX() {
		if (!getValue(Boolean.class, CONF_COGSIM_JMX_ENABLED)) {
			return;
		}
		try {
			if (cogbotJMXClient == null) {
				cogbotJMXClient = new CogbotJMXClient(getValue(String.class, CONF_COGSIM_JMX_URL), debugPw);
				cogbotJMXClient.registerListener((NotificationListener) this);
			}
		} catch (Throwable ex) {
			theLogger.error(ex.getMessage(), ex);
		}
	}

	public synchronized boolean isOnline() {
		ensureJMX();
		return true;
	}

	public synchronized String fetchLastThingWeSaid(boolean debugFlag) {
		if (!getValue(Boolean.class, CONF_COGSIM_POLL_ENABLED)) return "";
		CogSimOp cso = makeCogSimOp(myCogSimConf, null);
		try {
			return cso.fetchLastThingWeSaid(debugFlag);
		} catch (Throwable t) {
			theLogger.warn("Cannot fetchLastThingWeSaid []", t);
			return "";
		}
	}

	public synchronized String fetchLastThingWeHeard(boolean debugFlag) {
		if (!getValue(Boolean.class, CONF_COGSIM_POLL_ENABLED)) return "";
		CogSimOp cso = makeCogSimOp(myCogSimConf, null);
		try {
			return cso.fetchLastThingWeHeard(debugFlag);
		} catch (Throwable t) {
			theLogger.warn("Cannot fetchLastThingWeHeard []", t);
			return "";
		}
	}

	public void registerAction(CogbotPrimitive cogbotPrimitive) {
		synchronized (primitives) {
			primitives.put(cogbotPrimitive.getName(), cogbotPrimitive);
		}
	}

	@Override
	public void handleNotification(Notification notification, Object handback) {
		//throw new UnsupportedOperationException("Not supported yet.");
		debugJMX("----->" + notification);
		try {
//            if (notification instanceof AttributeChangeNotification) {
//                AttributeChangeNotification acn = (AttributeChangeNotification) notification;
//                String attribName = acn.getAttributeName();
//                String attribTypeName = acn.getAttributeType();
//                Object newValue = acn.getNewValue();
//                Object oldValue = acn.getOldValue();
//
//                // Dispatch based on "attribute name"
//                if (attribName.equals(IntegroidWrapperMXBean.ATTRIB_CUE_POSTED)
//						|| attribName.equals(IntegroidWrapperMXBean.ATTRIB_CUE_UPDATED)){
//                    if (newValue instanceof CueStub) {
//                        CueStub cue = (CueStub) newValue;
//                        handlePostedOrUpdatedCue(cue);
//                    }
//                } else if (attribName.equals(IntegroidWrapperMXBean.ATTRIB_CUE_CLEARED)) {
//                    if (newValue instanceof CueStub) {
//                        CueStub cue = (CueStub) newValue;
//                        debugJMX("Ignoring cueCleared notification");
//                    }
//                } else if (attribName.equals(IntegroidWrapperMXBean.ATTRIB_JOB_POSTED)) {
//                    if (newValue instanceof JobStub) {
//                        JobStub job = (JobStub) newValue;
//                        //handlePostedJob(job);
//                    } else {
//                        // handlePostedJobObject(newValue);
//                    }
//                } else if (attribName.equals(IntegroidWrapperMXBean.ATTRIB_JOB_CLEARED)) {
//                    //  Job job = (Job) newValue;
//                    debugJMX("Ignoring jobCleared notification");
//                } else {
//                    debugJMX("####################################################################");
//                    debugJMX("Unhandled attribute change notification.  Details are:");
//                    debugJMX("\tAttributeName: " + attribName);
//                    debugJMX("\tAttributeType: " + attribTypeName);
//                    debugJMX("\tNewValue: " + newValue);
//                    debugJMX("\tOldValue: " + oldValue);
//                    debugJMX("####################################################################");
//                }
//            }
			sendNotification(notification);
			debugJMX("\nFinished processing notification at:" + System.currentTimeMillis());
			debugJMX("******************");
		} catch (Throwable t) {
			t.printStackTrace();
		}


		// new Throwable().printStackTrace();
		// System.err.println("----->"+notification);

	}

	void echo(String msg) {
		if (debugPw == null) return;
		debugPw.println(msg);
	}

//    synchronized void handlePostedOrUpdatedCue(CueStub cue) {
//        if (cue instanceof VariableCue) {
//            VariableCue vc = (VariableCue)cue;
//            String varName = vc.getName();
///*
//            if (name!=null && name.equalsIgnoreCase("PARTNER")) {
//                setLookingAt(vc.getValue());
//            }
// */
//			if (varName!=null && varName.equalsIgnoreCase(AwarenessConstants.VAR_GAZE_PERSON_DESC)) {
//				String gazePersonDesc = vc.getValue();
//				String cogbotUsersName = gazePersonDesc;
//				if (gazePersonDesc.equals(AwarenessConstants.DESC_BOGEY)) {
//					cogbotUsersName = "UNRECOGNIZED";
//				} else if (gazePersonDesc.equals(AwarenessConstants.DESC_NOBODY)) {
//					cogbotUsersName = "UNSEEN";
//				}
//
//				setLookingAt(cogbotUsersName);
//			}
//        }
//    }

	public String getResponse(String input, String from) {
		return service.getCogbotResponse(this, debugPw, input, from,
				getValue(String.class, OLD_CONF_COGBOT_NAME)).getResponse();
	}

	public synchronized void setLookingAt(String value) {
//		AwarenessHelpFuncs.logAware("CogbotAvatar.setLookingAt(" + value + ")");
		// we lloked away maybe
/*        if (value == null) {
			return;
        }
        // not "me"
        value = value.trim();
        if (value.length() < 3) {
            return;
        }
        String old = USER_PARTNER;        
        USER_PARTNER = value;/*
 */
		postActionReqToCogbot("aiml", "@chuser " + value, true);// + " - "+ old, true);
	}

	private void debugJMX(String string) {
		if (!getValue(Boolean.class, CONF_COGSIM_DEBUG_FLAG)) return;
		echo("CogbotJMX: " + string);
	}

	/*
		public String coerceToUser(String userName) {
			if (!isEmpty(userName)) {
				if (isEmpty(USER_PARTNER)) {
					USER_PARTNER = userName;
				}
			}
			return USER_PARTNER;
		}
	*/
	static boolean isEmpty(String user) {
		return user == null || user.trim().isEmpty();
	}

	private CogSimOp makeCogSimOp(CogSimConf myCogSimConf, HttpClient object) {
		return new CogSimOp(this, myCogSimConf, object);
	}

	public void InvokeSerialAction(Runnable runnable) {
		TODO_QUEUE.invokeLater(runnable);
	}

	public void warnSettings() {
		if (!getValue(Boolean.class, CONF_COGSIM_ENABLED)) {
			warning("isCogSimEnabled is false so Cogbot may not know what the user is responding to");
		}
		if (!getValue(Boolean.class, CONF_COGSIM_POLL_ENABLED)) {
			warning("isPolling is false so we will not recivie Sim conversation ");
		}
	}

	private void warning(String string) {
		theLogger.warn(string);
		string = "WARNING: " + string;
		System.err.println(string);
		echo(string);
	}

	synchronized void sendNotification(Notification acn) {
		if (true) return;
		String evnt = ObjectLispWriter.makeLispObject(acn);
		postActionReqToCogbot("aiml", "@fromjmx " + evnt, false);
	}
}
