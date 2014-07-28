/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.joseki.processors;


import org.cogchar.joswrap.ModJosDatasetDesc;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joseki.*;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.LockMutex;

import com.hp.hpl.jena.query.Dataset;


public abstract class ProcessorBase implements Processor
{
	public static boolean stusLockingEnabledFlag = true;
	public static boolean stusTransactEnabledFlag = true;
	
    private static final Logger log = LoggerFactory.getLogger(ProcessorBase.class) ; 
    Lock lock = new LockMutex() ;   // Default and safe choice
    
    /** Execute a operation, providing a lock */ 
    public void exec(Request request, Response response, final ModJosDatasetDesc datasetDesc) throws ExecutionException
    {
        Lock thisLock = lock ;
        boolean transactions = false ;
        Model defaultModel = null ;
        
        final Dataset dataset = buildDataset(datasetDesc, request, response)  ;
		boolean dsetXactOpen = false;
        if ( dataset != null )
        {
            ResponseCallback cbLock = new ResponseCallback() {
                public void callback(boolean successfulOperation)
                {
                    log.debug("ResponseCallback: return dataset to pool") ;
                    datasetDesc.returnDataset(dataset) ;
                }} ;
            response.addCallback(cbLock) ;
			// Begin Stu's Xact patch:
			
			if (dataset.supportsTransactions()) {
				log.info("Opening WRITE transaction against our dataset");
				dataset.begin(com.hp.hpl.jena.query.ReadWrite.WRITE);
				dsetXactOpen = true;
			}
			
			thisLock = dataset.getLock() ; // Stu sez - this call to getLock() was throwing before the patch
            // Transactions - if and only if there is a default model supporting transactions
            defaultModel = dataset.getDefaultModel() ;
            transactions = defaultModel.supportsTransactions() && stusTransactEnabledFlag ;
        }
        
//        if ( datasetDesc != null && datasetDesc.getDataset() != null )
//            thisLock = datasetDesc.getDataset().getLock() ;
        
        final Lock operationLock = thisLock ;
        
        String op = request.getParam(Joseki.OPERATION) ;
        boolean lockType = Lock.READ ;
        if ( op.equals(Joseki.OP_UPDATE) )
            lockType = Lock.WRITE ;
        
        boolean needAbort = false ;     // Need to clear up?
        
        // -- Add callbacks
		if (stusLockingEnabledFlag) {
	        operationLock.enterCriticalSection(lockType) ;		
		}
        ResponseCallback cbLock = new ResponseCallback() {
            public void callback(boolean successfulOperation)
            {
                log.debug("ResponseCallback: criticalSection") ;
				if (stusLockingEnabledFlag) {
	                operationLock.leaveCriticalSection() ;
				}
            }} ;
        response.addCallback(cbLock) ;
        final boolean dsetXactionActive = dsetXactOpen;
        // Always add - TDB supports .commit.
        if ( transactions )
        {
            needAbort = true ;
            try { defaultModel.begin(); } catch (UnsupportedOperationException ex) { needAbort = false ; }
            final Model m = defaultModel ;
			
			
            ResponseCallback cb = new ResponseCallback() {
                public void callback(boolean successfulOperation)
                {
                    log.debug("ResponseCallback: transaction") ;
                    if ( successfulOperation )
                        try { 
							m.commit(); 
							if (dsetXactionActive) {
								log.info("Commiting dataset xact");
								dataset.commit();
								dataset.end();
							}
						} catch (Exception ex) { log.info("Exception on commit: "+ex.getMessage()) ; }
                    else
                        try {
							m.abort(); 
							if (dsetXactionActive) {
								log.info("Aborting dataset xact");
								dataset.abort();
								dataset.end();
							}
						}  catch (Exception ex) { log.info("Exception on abort: "+ex.getMessage()) ; }
                }} ;
            response.addCallback(cb) ;
        } else {
            // --------
            // Long term - migrate Sync to ARQ and Commit or Sync on datasets
            // For now (to avoid a version bump), do a reflection call.
            if ( dataset != null )
            {
                ResponseCallback cb = new ResponseCallback() {
                    public void callback(boolean successfulOperation)
                    {
						if (dsetXactionActive) {
							if (successfulOperation) {
								log.info("Commiting dataset xact - Stu notest that this is where commit happens with TDB-in Mem as of 2014-06-28");
								dataset.commit();
								dataset.end();
							} else {
								log.info("Aborting dataset xact");
								dataset.abort();
								dataset.end();
							}
						}
                        log.debug("ResponseCallback: sync") ;
                        if ( attemptSync(dataset.asDatasetGraph()) )
                            log.debug("ResponseCallback: sync/done") ;
                        else
                            log.debug("ResponseCallback: sync (no action)") ;
                    }} ;
                response.addCallback(cb) ;
            }
            // --------
        }

        try {
            execOperation(request, response, dataset) ;
        } catch (ExecutionException ex)
        {
            // Callbacks should handle this.
//            // Looking bad - abort the transaction, release the lock.
//            if ( needAbort && transactions )
//                defaultModel.abort();
			
			// 2012-11-17   Stu uncommented this, since it appears that exceptions above leave Joseki hung
			// on next update attempt.
			log.warn("*********************** Leaving critical section from exception handler, using code uncommented by Stu!");
			if (stusLockingEnabledFlag) {
		          operationLock.leaveCriticalSection() ;
			}
            throw ex ; 
        }
        // These should have been caught.
        catch (JenaException ex)
        {
//            // Looking bad - abort the transaction, release the lock.
//            if ( needAbort && transactions )
//                defaultModel.abort();
			
			
			// 2012-11-17   Stu uncommented this, since it appears that exceptions above leave Joseki hung
			// on next update attempt.
			log.warn("*********************** Leaving critical section from exception handler, using code uncommented by Stu!");
			operationLock.leaveCriticalSection() ;
            log.warn("Internal error - unexpected exception: ", ex) ;
            throw ex ; 
        }
        
    }
    
    private Dataset buildDataset(ModJosDatasetDesc datasetDesc, Request request, Response response)
    {
        return (datasetDesc==null) ? null : datasetDesc.acquireDataset(request, response) ;
    }

    private static boolean attemptSync(Object object)
    {
        // Attempt "sync(boolean)"
        try
        {
            Method syncMethod = object.getClass().getMethod("sync", new Class<?>[]{Boolean.TYPE}) ;
            if ( syncMethod != null )
            {
                syncMethod.invoke(object, true) ;
                return true ;
            }
            return false;
        } catch (NoSuchMethodException ex) { return false ; }
        catch (Exception ex)
        {
            log.warn("Failed to call 'sync'", ex) ;
            return false ;
        }
    }

    public void setLock(Lock lock)
    {
        this.lock = lock ;
    }
    
    /** Execute an operation within a lock and/or a transaction (on the default model) */ 
    public abstract void execOperation(Request request, Response response, Dataset defaultDataset)
    throws ExecutionException ;
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