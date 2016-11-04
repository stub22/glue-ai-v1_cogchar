/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * 
 * dumpEnvironment and dumpServletContext from patch by Fred Hartman // webMethods.
 */

/** A servlet that dumps its request
 */

// Could be neater - much, much neater!
package org.joseki.servlets;

import java.util.* ;
import java.io.* ;

import javax.servlet.http.*;
import javax.servlet.* ;

public class DumpServlet extends HttpServlet
{
    private static final long serialVersionUID = 99L;  // Serilizable.


    public DumpServlet()
    {

    }

    @Override
    public void init()
    {
        return ;
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            PrintWriter out = resp.getWriter() ;
            resp.setContentType("text/html");

            String now = new Date().toString() ;

            // HEAD
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("<Title>Dump @ "+now+"</Title>") ;
            // Reduce the desire to cache it.
            out.println("<meta CONTENT=now HTTP-EQUIV=expires>") ;
            out.println("</head>") ;

            // BODY
            out.println("<body>") ;
            out.println("<pre>") ;

            out.println("Dump : "+now);
            out.println() ;
            out.println("==== Request");
            out.println() ;
            out.print(dumpRequest(req)) ;
            out.println() ;
                        
            out.println("==== Body");
            out.println() ;
            printBody(out, req) ;
            
            out.println("==== ServletContext");
            out.println() ;
            out.print(dumpServletContext());
            out.println() ;

            out.println("==== Environment");
            out.println() ;
            out.print(dumpEnvironment());
            out.println() ;

            out.println("</pre>") ;

            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
        } catch (IOException e)
        { }
    }

    // This resets the input stream

    static public String dumpRequest(HttpServletRequest req)
    {
        try {
            StringWriter sw = new StringWriter() ;
            PrintWriter pw = new PrintWriter(sw) ;

            // Standard environment
            pw.println("Method:                 "+req.getMethod());
            pw.println("getContentLength:       "+Integer.toString(req.getContentLength()));
            pw.println("getContentType:         "+req.getContentType());
            pw.println("getRequestURI:          "+req.getRequestURI());
            pw.println("getRequestURL:          "+req.getRequestURL());
            pw.println("getContextPath:         "+req.getContextPath());
            pw.println("getServletPath:         "+req.getServletPath());
            pw.println("getPathInfo:            "+req.getPathInfo());
            pw.println("getPathTranslated:      "+req.getPathTranslated());
            pw.println("getQueryString:         "+req.getQueryString());
            pw.println("getProtocol:            "+req.getProtocol());
            pw.println("getScheme:              "+req.getScheme());
            pw.println("getServerName:          "+req.getServerName());
            pw.println("getServerPort:          "+req.getServerPort());
            pw.println("getRemoteUser:          "+req.getRemoteUser());
            pw.println("getRemoteAddr:          "+req.getRemoteAddr());
            pw.println("getRemoteHost:          "+req.getRemoteHost());
            pw.println("getRequestedSessionId:  "+req.getRequestedSessionId());
            {
                Cookie c[] = req.getCookies() ;
                if ( c == null )
                    pw.println("getCookies:            <none>");
                else
                {
                    for ( int i = 0 ; i < c.length ; i++ )            
                    {
                        pw.println("Cookie:        "+c[i].getName());
                        pw.println("    value:     "+c[i].getValue());
                        pw.println("    version:   "+c[i].getVersion());
                        pw.println("    comment:   "+c[i].getComment());
                        pw.println("    domain:    "+c[i].getDomain());
                        pw.println("    maxAge:    "+c[i].getMaxAge());
                        pw.println("    path:      "+c[i].getPath());
                        pw.println("    secure:    "+c[i].getSecure());
                        pw.println();
                    }
                }
            }
            
            {
                // To do: create a string for the output so can send to console and return it.
                @SuppressWarnings("unchecked")
                Enumeration<String> en = req.getHeaderNames() ;

                for ( ; en.hasMoreElements() ; )
                {
                    String name = en.nextElement() ;
                    String value = req.getHeader(name) ;
                    pw.println("Head: "+name + " = " + value) ;
                }
            }
            
            @SuppressWarnings("unchecked")
            Enumeration<String> en2 = req.getAttributeNames() ;
            if ( en2.hasMoreElements() )
                pw.println();
            for ( ; en2.hasMoreElements() ; )
            {
                String name = en2.nextElement() ;
                String value = req.getAttribute(name).toString() ;
                pw.println("Attr: "+name + " = " + value) ;
            }

            // Note that doing this on a form causes the forms content (body) to be read
            // and parsed as form variables.
//            en = req.getParameterNames() ;
//            if ( en.hasMoreElements() )
//                pw.println();
//            for ( ; en.hasMoreElements() ; )
//            {
//                String name = (String)en.nextElement() ;
//                String value = req.getParameter(name) ;
//                pw.println("Param: "+name + " = " + value) ;
//            }

            // Don't use ServletRequest.getParameter or getParamterNames
            // as that reads form data.  This code parses just the query string.
            if ( req.getQueryString() != null )
            {
                pw.println();
                String[] params = req.getQueryString().split("&") ;
                for ( int i = 0 ; i < params.length ; i++ )
                {
                    String p = params[i] ;
                    String[] x = p.split("=",2) ;
                    String name = null ;
                    String value = null ;
                    
                    if ( x.length == 0 )
                    {
                        name = p ;
                        value = "" ;
                    }
                    else if ( x.length == 1 )
                    {
                        name = x[0] ;
                        value = "" ;
                    }
                    else
                    {
                        name = x[0] ;
                        value = x[1] ;
                    }
                    pw.println("Param: "+name + " = " + value) ;
                }
            }
            
            @SuppressWarnings("unchecked")
            Enumeration<Locale> en = req.getLocales() ;
            if ( en.hasMoreElements() )
                pw.println();
            for ( ; en.hasMoreElements() ; )
            {
                String name = en.nextElement().toString() ;
                pw.println("Locale: "+name) ;
            }

            pw.println() ;
            printBody(pw, req) ;

            pw.close() ;
            sw.close() ;
            return sw.toString() ;
        } catch (IOException e)
        { }
        return null ;
    }

    static void printBody(PrintWriter pw, HttpServletRequest req) throws IOException
    {
        BufferedReader in = req.getReader() ;
        if ( req.getContentLength() > 0 )
            // Need +2 because last line may not have a CR/LF on it.
            in.mark(req.getContentLength()+2) ;
        else
            // This is a dump - try to do something that works, even if inefficient.
            in.mark(100*1024) ;


        while(in.ready())
        {
            pw.println(in.readLine());
        }

        try { in.reset() ;} catch (IOException e) { System.out.println("DumpServlet: Reset of content failed: "+e) ; }
    }
    
    /**
     * <code>dumpEnvironment</code>
     * @return String that is the HTML of the System properties as 
name/value pairs.
     * The values are with single quotes independent of whether or not 
the value has
     * single quotes in it.
     */
    static public String dumpEnvironment()
    {
        Properties properties = System.getProperties();
        StringWriter sw = new StringWriter() ;
        PrintWriter pw = new PrintWriter(sw) ;
        Enumeration<Object> en = properties.keys();
        while(en.hasMoreElements())
        {
            String key = en.nextElement().toString();
            pw.println(key+": '"+properties.getProperty(key)+"'");
        }
        pw.println() ;
        pw.close() ;
        try {
            sw.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString() ;      
    }

    public String dumpServletContext()
    {
        StringWriter sw = new StringWriter() ;
        PrintWriter pw = new PrintWriter(sw) ;

        ServletContext sc =  getServletContext();
        pw.println("majorVersion: '"+sc.getMajorVersion()+"'");
        pw.println("minorVersion: '"+sc.getMinorVersion()+"'");
        pw.println("contextName:  '"+sc.getServletContextName()+"'");
        pw.println("servletInfo:  '"+getServletInfo()+"'");
        pw.println("serverInfo:  '"+sc.getServerInfo()+"'");

//        Enumeration en = null ;
//         // Deprecated and will be removed - from Servlet API 2.0
//        eneration en = sc.getServlets();
//        if (en != null) {
//            pw.println("servlets: ");
//            while(en.hasMoreElements())
//            {
//                String key = (String)en.nextElement();
//                try {
//                    pw.println(key+": '"+sc.getServlet(key)+"'");
//                } catch (ServletException e1) {
//                    pw.println(key+": '"+e1.toString()+"'");
//                }
//            }
//        }
        
        {
            @SuppressWarnings("unchecked")
            Enumeration<String> en = sc.getInitParameterNames();
            if (en != null) {
                pw.println("initParameters: ");
                while(en.hasMoreElements())
                {
                    String key = en.nextElement();
                    pw.println(key+": '"+sc.getInitParameter(key)+"'");
                }
            }
        }
        
        {
            @SuppressWarnings("unchecked")
            Enumeration<String> en = sc.getAttributeNames();
            if (en != null) {
                pw.println("attributes: ");
                while(en.hasMoreElements())
                {
                    String key = en.nextElement();
                    pw.println(key+": '"+sc.getAttribute(key)+"'");
                }
            }
        }
        pw.println() ;
        pw.close() ;
        try {
            sw.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString() ;      
    }

    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    {
        doGet(req, resp) ;
    }


    @Override
    public String getServletInfo()
    {
        return "Dump";
    }
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
