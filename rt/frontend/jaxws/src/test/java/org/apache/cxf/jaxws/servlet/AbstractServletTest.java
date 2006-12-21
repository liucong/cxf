/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.jaxws.servlet;

import java.io.IOException;
import java.net.MalformedURLException;

import org.xml.sax.SAXException;

import com.meterware.httpunit.HttpException;
import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

//import org.apache.cxf.Bus;
//import org.apache.cxf.BusException;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactoryHelper;
import org.apache.cxf.test.AbstractCXFTest;
//import org.apache.cxf.transport.DestinationFactoryManager;

public abstract class AbstractServletTest extends AbstractCXFTest {

    protected ServletRunner sr;

    public void setUp() throws Exception {
        sr = new ServletRunner(getResourceAsStream(getConfiguration()));
        
        try {
            sr.newClient().getResponse("http://localhost/services/");
        } catch (HttpNotFoundException e) {
            // ignore, we just want to boot up the servlet
        }        
        super.setUp();
        
        HttpUnitOptions.setExceptionsThrownOnErrorStatus(true);        
    } 
    
    public void tearDown() {
        bus.shutdown(false);
        BusFactoryHelper.newInstance().setDefaultBus(null);                
    }
       
    //CXFservlet has create the bus, so we need to use this bus for service init 
    @Override
    public Bus getBus() {
        return BusFactoryHelper.newInstance().getDefaultBus();
    }
    
    @Override
    public Bus createBus() {
        return BusFactoryHelper.newInstance().getDefaultBus();
    }
    /**
     * @return The web.xml to use for testing.
     */
    protected String getConfiguration() {
        return "/org/apache/cxf/jaxws/servlet/web.xml";
    }

    protected ServletUnitClient newClient() {
        return sr.newClient();
    }

    /**
     * Here we expect an errorCode other than 200, and look for it checking for
     * text is omitted as it doesnt work. It would never work on java1.3, but
     * one may have expected java1.4+ to have access to the error stream in
     * responses. Clearly not.
     * 
     * @param request
     * @param errorCode
     * @param errorText optional text string to search for
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     */
    protected void expectErrorCode(WebRequest request, int errorCode, String errorText)
        throws MalformedURLException, IOException, SAXException {
        String failureText = "Expected error " + errorCode + " from " + request.getURL();

        try {
            newClient().getResponse(request);
            fail(errorText + " -got success instead");
        } catch (HttpException e) {
            assertEquals(failureText, errorCode, e.getResponseCode());
            /*
             * checking for text omitted as it doesnt work. if(errorText!=null) {
             * assertTrue( "Failed to find "+errorText+" in "+
             * e.getResponseMessage(), e.getMessage().indexOf(errorText)>=0); }
             */
        }
    }
}
