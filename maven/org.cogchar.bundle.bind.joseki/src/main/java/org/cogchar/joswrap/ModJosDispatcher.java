/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */
package org.cogchar.joswrap;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.FileManager;
import org.appdapter.bind.rdf.jena.model.JenaFileManagerUtils;
import org.joseki.ConfigurationErrorException;
import org.joseki.ExecutionException;
import org.joseki.Joseki;

import org.joseki.Registry;
import org.joseki.Request;
import org.joseki.Response;
import org.joseki.ResponseCallback;
import org.joseki.ReturnCodes;
import org.joseki.Service;
import org.joseki.ServiceRegistry;
import org.joseki.ServiceRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModJosDispatcher {

	private static Logger log = LoggerFactory.getLogger(ModJosDispatcher.class);
	static ModJosConfiguration configuration = null;
	static ServiceRegistry serviceRegistry = null;

	public static ModJosConfiguration getConfiguration() {
		return configuration;
	}

	public static ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	//Dispatcher dispatcher = new Dispatcher() ;
	// Stu added "synchronized" keyword to ensure we don't have overlapping update request processing.
	// We are also relying on criticalSection of the LockMRSW below, not sure how that will 
	public synchronized static void dispatch(final String serviceURI, Request request, Response response) throws ExecutionException {
		if (serviceRegistry == null) {
			buildServiceRegistry();
			if (serviceRegistry == null) {
				log.error("Service registry not initialized");
				throw new ExecutionException(ReturnCodes.rcInternalError, "Service registry not initialized");
			}
		}

		try {
			Service service = serviceRegistry.find(serviceURI);
			if (service == null) {
				log.info("404 - Service <" + serviceURI + "> not found");
				throw new ExecutionException(ReturnCodes.rcNoSuchURI, "Service <" + serviceURI + "> not found");
			}

			if (!service.isAvailable()) {
				log.info("Service is not available");
				throw new ExecutionException(ReturnCodes.rcServiceUnavailable, "Service <" + serviceURI + "> unavailable");
			}

			final ServiceRequest serviceRequest = service.instance(request, response);
			serviceRequest.start();
			ResponseCallback cb = new ResponseCallback() {

				public void callback(boolean successfulOperation) {
					// This is called during response.sendResponse, AFTER the LockMRSW.leaveCriticalSection call.
					// So this is a chance for app to do work in that circumstance.
					// Whether this is a "good" place to do work depends on the interaction of App threads with
					// the SPARQL store and specifically with the LockMRSW.
					log.info("ResponseCallback: starting serviceRequest.finish()");
					serviceRequest.finish();
					log.info("Finished serviceRequest.finish()");
				}
			};
			response.addCallback(cb);
			log.info("Starting serviceRequest.exec()  -- which enters a critical section and performs actual updates/queries");
			serviceRequest.exec(request, response);

			// Stu hacked in this RepoUpdateCallbackAdapter notification.
			// Some kind of locking is necessary to prevent partial reads of data from the *next* update 
			// during our callback processing.
			// So, we perform callback-notifications while the SPARQL-update-service is still write-locked.
			// But currently we also have static-synchronized the entire enclosing dispatch method.
			possiblySendRepoUpdateCallbacks(serviceURI);
			log.info("Starting response.sendResponse() - which leaves the critical section.  ");
			response.sendResponse();
			log.info("Finished response.sendResponse()");
		} catch (ExecutionException ex) {
			response.sendException(ex);
			return;
		}
	}

	private static void possiblySendRepoUpdateCallbacks(String serviceURI) {
		if (serviceURI.toLowerCase().contains("update")) {
			log.info("%%%%% Sending Repo-UPDATE notification callbacks");
			try {
				RepoUpdateCallbackAdapter.notifyCallbacks();
			} catch (Throwable t) {
				log.error("Caught exception during notification callbacks for serviceURI=" + serviceURI, t);
			}
		}
	}

	//  This method contains the pragmatic algorithm to determine the configuration URI.
	//  
	//  In this order (i.e. specific to general) to find the filename:
	//  System property org.joseki.rdfserver.config => a URI.
	//  Resource:                
	//  System property:         jena.rdfserver.modelmap => file name
	//  Webapp init parameter:   jena.rdfserver.modelmap
	//  Servlet init parameter:  jena.rdfserver.modelmap
	//  and then the file is loaded.
	public static void initServiceRegistry() {
		initServiceRegistry(FileManager.get());
	}

	public static void initServiceRegistry(FileManager fileManager) {
		initServiceRegistry(fileManager, null);
	}

	public static void initServiceRegistry(String configURI) {
		initServiceRegistry(FileManager.get(), configURI);
	}

	public static void initServiceRegistry(FileManager fileManager, String configURI) {
		if (configURI == null) {
			configURI = System.getProperty(Joseki.configurationFileProperty, DisabledRDFServer.defaultConfigFile);
		}
		setConfiguration(fileManager, configURI);
	}

	/**
	 * Added by Stu to allow override, see edit at line 155 below. Now we can hack it here (to produce RepoJosConfig,
	 * which doesn't do anything special yet) OR extend this Dispatcher class.
	 */
	protected static ModJosConfiguration makeConfig(FileManager fileManager, String configURI, ServiceRegistry sreg) {
		return new ModJosConfiguration(fileManager, configURI, sreg);
	}
	/**
	 * *
	 *
	 */
	private static FileManager theGlueFM;
	private static String theConfigURI;

	public static synchronized void setConfiguration(FileManager fileManager, String configURI) {
		// In Joseki default impl, this method includes all the buildServiceRegistry stuff below.
		if (configURI == null) {
			log.error("Null  configuration URI");
			return;
		}
		theGlueFM = fileManager;
		theConfigURI = configURI;
	}

	public static synchronized void buildServiceRegistry() {

		FileManager fileManager = theGlueFM;
		String configURI = theConfigURI;

		if (serviceRegistry != null) {
			log.warn("Service registry already initialized");
			return;
		}
		// Already squirreled away somewhere?
		serviceRegistry = (ServiceRegistry) Registry.find(DisabledRDFServer.ServiceRegistryName);
		if (serviceRegistry != null) {
			log.warn("Found globally registered service registry");
			return;
		}
		if (fileManager == null) {
			log.error("Null File Manager");
			return;
		}
		if (configURI == null) {
			log.error("Null config URI");
			return;
		}
		// Time to really build it!
		ServiceRegistry tmp = new ServiceRegistry();
		try {
			// Stu added classloader-reg this so our bundle-CL is on FM locator list
			ClassLoader cl = ModJosDispatcher.class.getClassLoader();
			JenaFileManagerUtils.ensureClassLoaderRegisteredWithJenaFM(fileManager, cl);

			// Stu replaced this:
			//  configuration = new ModJosConfiguration(fileManager, configURI, tmp) ;
			// with this:
			configuration = makeConfig(fileManager, configURI, tmp);

			Registry.add(DisabledRDFServer.ServiceRegistryName, tmp);
			serviceRegistry = (ServiceRegistry) Registry.find(DisabledRDFServer.ServiceRegistryName);
			log.info("Loaded data source configuration: " + configURI);
		} catch (NotFoundException ex) {
			throw new ConfigurationErrorException("Not found: " + ex.getMessage(), ex);
			//return false;
		} catch (JenaException rdfEx) {
			// Trouble processing a configuration 
			throw new ConfigurationErrorException("RDF Exception: " + rdfEx.getMessage(), rdfEx);
			//return false ;
		}

	}
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */