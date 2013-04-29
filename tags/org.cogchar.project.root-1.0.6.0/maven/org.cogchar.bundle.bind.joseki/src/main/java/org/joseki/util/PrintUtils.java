/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */


package org.joseki.util;

import com.hp.hpl.jena.rdf.model.* ;
import org.slf4j.*;

/** Code to format things for output 
 *  
 * @author     Andy Seaborne
 * @version    $Id: PrintUtils.java,v 1.9 2009/04/24 14:30:45 andy_seaborne Exp $
 */
 
public class PrintUtils
{
//    static Logger logger = LoggerFactory.getLogger(PrintUtils.class) ;
    
    static public String fmt(RDFNode n)
    {
        if ( n instanceof Resource )
        {
            Resource r = (Resource)n ;
            if ( r.isAnon() )
                return "[]" ;
            return r.getURI() ;
        }
        return n.toString() ;
    }
    
    static public String fmt(Literal literal)
    {
        return literal.toString() ;
    }
    
    static public void dumpResource(Logger log, Resource r)
    {
        
        log.info("Resource: "+fmt(r)) ;
        
        StmtIterator iter = r.listProperties() ;
        for ( ; iter.hasNext() ; )
        {
            Statement s = iter.nextStatement() ;
            Property p = s.getPredicate() ;
            log.info("    "+p.getURI()) ;
        }
        iter.close() ;
    }
   
}

/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
