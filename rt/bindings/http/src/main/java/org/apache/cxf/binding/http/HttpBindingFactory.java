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
package org.apache.cxf.binding.http;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.cxf.binding.AbstractBindingFactory;
import org.apache.cxf.binding.Binding;
import org.apache.cxf.binding.http.interceptor.ContentTypeOutInterceptor;
import org.apache.cxf.binding.http.interceptor.DatabindingInSetupInterceptor;
import org.apache.cxf.binding.http.interceptor.DatabindingOutSetupInterceptor;
import org.apache.cxf.binding.xml.XMLBinding;
import org.apache.cxf.binding.xml.interceptor.XMLFaultInInterceptor;
import org.apache.cxf.binding.xml.interceptor.XMLFaultOutInterceptor;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.service.model.BindingInfo;

public class HttpBindingFactory extends AbstractBindingFactory {

    public static final String HTTP_BINDING_ID = "http://apache.org/cxf/binding/http";
    private Collection<String> activationNamespaces;    
    
    @Resource
    public void setActivationNamespaces(Collection<String> ans) {
        activationNamespaces = ans;
    }
    
    public Collection<String> getActivationNamespaces() {
        return activationNamespaces;
    }

    public Binding createBinding(BindingInfo bi) {
        XMLBinding binding = new XMLBinding();
        
        binding.getInInterceptors().add(new AttachmentInInterceptor());
        binding.getInInterceptors().add(new DatabindingInSetupInterceptor());

        binding.getOutInterceptors().add(new AttachmentOutInterceptor());
        binding.getOutInterceptors().add(new ContentTypeOutInterceptor());

        binding.getOutInterceptors().add(new DatabindingOutSetupInterceptor());
        
        binding.getInFaultInterceptors().add(new XMLFaultInInterceptor());
        
        binding.getOutFaultInterceptors().add(new StaxOutInterceptor());
        binding.getOutFaultInterceptors().add(new XMLFaultOutInterceptor());
        
        return binding;
    }

}
