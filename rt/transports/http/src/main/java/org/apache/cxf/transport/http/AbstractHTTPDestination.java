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

package org.apache.cxf.transport.http;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.configuration.Configurable;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.destination.HTTPDestinationConfigBean;
import org.apache.cxf.transports.http.configuration.HTTPServerPolicy;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;


/**
 * Common base for HTTP Destination implementations.
 */
public abstract class AbstractHTTPDestination extends HTTPDestinationConfigBean 
    implements Destination, Configurable {
    static final Logger LOG = LogUtils.getL7dLogger(AbstractHTTPDestination.class);
    
    private static final long serialVersionUID = 1L;

    protected final Bus bus;
    protected final ConduitInitiator conduitInitiator;
    protected final EndpointInfo endpointInfo;
    protected final EndpointReferenceType reference;
    protected String name;
    protected URL nurl;

    /**
     * Constructor
     * 
     * @param b the associated Bus
     * @param ci the associated conduit initiator
     * @param ei the endpoint info of the destination 
     * @throws IOException
     */    
    public AbstractHTTPDestination(Bus b,
                                   ConduitInitiator ci,
                                   EndpointInfo ei)
        throws IOException {
        bus = b;
        conduitInitiator = ci;
        endpointInfo = ei;
        
        setServer(endpointInfo.getTraversedExtensor(new HTTPServerPolicy(), HTTPServerPolicy.class));
        
        nurl = new URL(getAddressValue());
        name = nurl.getPath();

        reference = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(getAddressValue());
        reference.setAddress(address);
    }
    
    public String getBeanName() {
        String beanName = null;
        if (endpointInfo.getName() != null) {
            beanName = endpointInfo.getName().toString() + ".http-destination";
        }
        return beanName;
    }



    /**
     * @return the reference associated with this Destination
     */    
    public EndpointReferenceType getAddress() {
        return reference;
    }

    /**
     * Cache HTTP headers in message.
     * 
     * @param message the current message
     */
    protected void setHeaders(Message message) {
        Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
        copyRequestHeaders(message, requestHeaders);
        message.put(Message.PROTOCOL_HEADERS, requestHeaders);

        if (requestHeaders.containsKey("Authorization")) {
            List<String> authorizationLines = requestHeaders.get("Authorization"); 
            String credentials = authorizationLines.get(0);
            String authType = credentials.split(" ")[0];
            if ("Basic".equals(authType)) {
                String authEncoded = credentials.split(" ")[1];
                try {
                    String authDecoded = new String(Base64Utility.decode(authEncoded));
                    String authInfo[] = authDecoded.split(":");
                    String username = authInfo[0];
                    String password = authInfo[1];
                    
                    AuthorizationPolicy policy = new AuthorizationPolicy();
                    policy.setUserName(username);
                    policy.setPassword(password);
                    
                    message.put(AuthorizationPolicy.class, policy);
                } catch (Base64Exception ex) {
                    //ignore, we'll leave things alone.  They can try decoding it themselves
                }
            }
        }
           
    }
    
    @SuppressWarnings("unchecked")
    protected void updateResponseHeaders(Message message) {
        Map<String, List<String>> responseHeaders =
            (Map<String, List<String>>)message.get(Message.PROTOCOL_HEADERS);
        if (responseHeaders == null) {
            responseHeaders = new HashMap<String, List<String>>();
            message.put(Message.PROTOCOL_HEADERS, responseHeaders);         
        }
        setPolicies(responseHeaders);
    }
    
    /** 
     * @param message the message under consideration
     * @return true iff the message has been marked as oneway
     */    
    protected boolean isOneWay(Message message) {
        return message.getExchange() != null && message.getExchange().isOneWay();
    }

    /**
     * Copy the request headers into the message.
     * 
     * @param message the current message
     * @param headers the current set of headers
     */
    protected abstract void copyRequestHeaders(Message message,
                                               Map<String, List<String>> headers);

    protected final String getAddressValue() {       
        return StringUtils.addDefaultPortIfMissing(endpointInfo.getAddress());
    }        

    void setPolicies(Map<String, List<String>> headers) {
        HTTPServerPolicy policy = getServer(); 
        if (policy.isSetCacheControl()) {
            headers.put("Cache-Control",
                        Arrays.asList(new String[] {policy.getCacheControl().value()}));
        }
        if (policy.isSetContentLocation()) {
            headers.put("Content-Location",
                        Arrays.asList(new String[] {policy.getContentLocation()}));
        }
        if (policy.isSetContentEncoding()) {
            headers.put("Content-Encoding",
                        Arrays.asList(new String[] {policy.getContentEncoding()}));
        }
        if (policy.isSetContentType()) {
            headers.put(HttpHeaderHelper.CONTENT_TYPE,
                        Arrays.asList(new String[] {policy.getContentType()}));
        }
        if (policy.isSetServerType()) {
            headers.put("Server",
                        Arrays.asList(new String[] {policy.getServerType()}));
        }
        if (policy.isSetHonorKeepAlive() && !policy.isHonorKeepAlive()) {
            headers.put("Connection",
                        Arrays.asList(new String[] {"close"}));
        }
        
    /*
     * TODO - hook up these policies
    <xs:attribute name="SuppressClientSendErrors" type="xs:boolean" use="optional" default="false">
    <xs:attribute name="SuppressClientReceiveErrors" type="xs:boolean" use="optional" default="false">
    */
    }

    boolean contextMatchOnExact() {
        return "exact".equals(getContextMatchStrategy());
    }    
}
