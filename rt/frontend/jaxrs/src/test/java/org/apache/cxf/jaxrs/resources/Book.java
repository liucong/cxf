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

package org.apache.cxf.jaxrs.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "Book")
public class Book {
    private String name;
    private long id;
    private Map<Long, Chapter> chapters = new HashMap<Long, Chapter>();
    
    public Book() {
    }
    
    public void setName(String n) {
        name = n;
    }

    public String getName() {
        return name;
    }
    
    public void setId(long i) {
        id = i;
    }
    public long getId() {
        return id;
    }
    
    @UriTemplate("chapters/{chapterid}/")
    @HttpMethod("GET")
    public Chapter getChapter(@UriParam("id")int chapterid) {
        return chapters.get(new Long(chapterid));
    }   

    @HttpMethod("GET")
    public String getState() {
        return "";
    }
}