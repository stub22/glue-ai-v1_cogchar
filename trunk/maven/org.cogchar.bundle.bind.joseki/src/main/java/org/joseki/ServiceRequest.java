/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.joseki;

import org.cogchar.joswrap.ModJosDatasetDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceRequest
{
    private static Logger log = LoggerFactory.getLogger(ServiceRequest.class) ;
    
    private final ModJosDatasetDesc datasetDesc ;
    private final Processor   processor ;
    private final Request     request ;
    private final Response    response ;

    public ServiceRequest(Request request, Response response, Processor processor, ModJosDatasetDesc datasetDesc )
    { 
        this.request = request ;
        this.response = response ;
        this.datasetDesc = datasetDesc ;
        this.processor = processor ; 
    }

    public void start()
    {
        log.debug("ServiceRequest.start") ;
    }
    
    public void exec(Request request, Response response) throws ExecutionException
    {
        processor.exec(request, response, datasetDesc) ;
    }
    
    public void finish()
    {
        log.debug("ServiceRequest.finish") ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
