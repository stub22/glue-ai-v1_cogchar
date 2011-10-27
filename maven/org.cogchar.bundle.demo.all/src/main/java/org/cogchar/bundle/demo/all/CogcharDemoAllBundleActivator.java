package org.cogchar.bundle.demo.all;

import java.io.File;
import java.net.URISyntaxException;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.swing.JFrame;
import org.apache.avro.generic.IndexedRecord;
import org.apache.qpid.client.AMQQueue;
import org.appdapter.osgi.core.BundleActivatorBase;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import org.cogchar.render.opengl.bony.app.BonyStickFigureApp;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.sys.VirtCharPanel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.robokind.api.messaging.PollingService;
import org.robokind.api.motion.Robot;
import org.robokind.avrogen.motion.MotionFrameRecord;
import org.robokind.impl.messaging.ConnectionManager;
import org.robokind.impl.messaging.JMSPollingService;
import org.robokind.impl.motion.messaging.RobotMoveServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CogcharDemoAllBundleActivator extends BundleActivatorBase {
	static Logger theLogger = LoggerFactory.getLogger(CogcharDemoAllBundleActivator.class);
	
	@Override protected Logger getLogger() {
		return theLogger;
	}
	@Override public void start(BundleContext bundleCtx) throws Exception {
		super.start(bundleCtx);
		BonyContext bc = getBonyContext(bundleCtx);
		if (bc != null) { 
			initOpenGLDemoStuff(bc);
			initRobokindJointPumperDemo(bundleCtx, bc);
		} else {
			theLogger.warn("Can't find BonyContext");
		}
		
	}
	private void initRobokindJointPumperDemo(BundleContext bundleCtx, BonyContext bc)  throws Exception {
        File jointBindingConfigFile = new File("bonyRobotConfig.json");
		RobokindJointBindingDemo rjbd = new RobokindJointBindingDemo();
		
        //load robot from file
        //rjbd.createAndRegisterRobot(bundleCtx);
		Robot r = rjbd.createAndRegisterRobot(bundleCtx, jointBindingConfigFile);
        if(r == null){
            return;
        }
		
        rjbd.connectToVirtualChar(bc);
        Connection connection = ConnectionManager.createConnection(
                "admin", "admin", "client1", "test", "tcp://127.0.0.1:5672");
        if(connection != null){
            String connId = "connection1";
            ServiceRegistration connReg =  ConnectionManager.registerConnection(
                    bundleCtx, connId, connection, null);
            String queue = "test.RobotMoveQueue; {create: always, node: {type: queue}}";
            startRobotServer(bundleCtx, r.getRobotId(), connId, queue);
        }
		
	}
	private void initOpenGLDemoStuff(BonyContext bc) throws Exception {

		theLogger.info("Got BonyContext: " + bc);
		if (bc != null) {
			ClassLoader tccl = Thread.currentThread().getContextClassLoader();
			try {
				// Must set context classloader so that JMonkey can find goodies
				// on the classpath, currently presumed to be in same class space
				// as the BonyContext class.  (Could generalize this and make
				// it use the loader of a configured bundle).
				ClassLoader bonyLoader = bc.getClass().getClassLoader();
				theLogger.info("Setting thread class loader to bony loader: " + bonyLoader);
				Thread.currentThread().setContextClassLoader(bonyLoader);
				VirtCharPanel vcp = bc.getPanel();
				theLogger.info("Got VirtCharPanel: " + vcp);
				// Frame must be packed after panel created, but created 
				// before startJMonkey.  If startJMonkey is called first,
				// we often hang in frame.setVisible() as JMonkey tries
				// to do some magic restart thing that doesn't work as of
				// jme3-alpha4-August 2011.
				JFrame jf = vcp.makeEnclosingJFrame();
				theLogger.info("Got Enclosing Frame, adding to BonyContext for WindowClose triggering: " + jf);
				// Frame will receive a close event when org.cogchar.bundle.render.opengl is STOPPED
				bc.setFrame(jf);
				BonyVirtualCharApp app = bc.getApp();
				theLogger.info("Starting JMonkey canvas: " + jf);
				app.startJMonkeyCanvas();
				((BonyStickFigureApp) app).setScoringFlag(true);			

			} finally {
				theLogger.info("Restoring old class loader: " + tccl);
				Thread.currentThread().setContextClassLoader(tccl);
			}
				
		} else {
			theLogger.error("BonyContext is NULL");
		}

	}
    public static <T extends IndexedRecord> PollingService<T> 
             startPollingService(Class<T> clazz, 
                    BundleContext context, String connectionId, String destStr) 
                            throws JMSException, URISyntaxException, Exception{
         
        ServiceReference ref = ConnectionManager.retrieveConnectionReference(
                context, connectionId, null);
        if(ref == null){
            throw new NullPointerException();
        }
        Object obj = context.getService(ref);
        if(!(obj instanceof Connection)){
            theLogger.warn("Service not expected type.");
            return null;
        }
        Connection connection = (Connection)obj;
        Session session = 
                connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        Destination dest = new AMQQueue(destStr);
        MessageConsumer moveConsumer = session.createConsumer(dest);
        connection.start();
        PollingService pollService = new JMSPollingService
                (MotionFrameRecord.class, 
                MotionFrameRecord.SCHEMA$, 
                moveConsumer);
        return pollService;
    }
    
    private void startRobotServer(
            BundleContext context, Robot.Id id, String conStr, String destStr)
                throws Exception{
        RobotMoveServer server = new RobotMoveServer(context, id);
        PollingService<MotionFrameRecord> poll = startPollingService(
                MotionFrameRecord.class, context, conStr, destStr);
        server.setPollingService(poll);
        poll.start();
    }
	
	protected static BonyContext getBonyContext(BundleContext bundleCtx) {
		ServiceReference ref = bundleCtx.getServiceReference(BonyContext.class.getName());
		if(ref == null){
			return null;
		}
		return (BonyContext) bundleCtx.getService(ref);
	}
}
