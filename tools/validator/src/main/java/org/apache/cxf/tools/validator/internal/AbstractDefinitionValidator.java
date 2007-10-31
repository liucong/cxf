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

package org.apache.cxf.tools.validator.internal;

import javax.wsdl.Definition;
import javax.xml.stream.Location;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.validator.AbstractValidator;

public abstract class AbstractDefinitionValidator extends AbstractValidator {
    protected final Definition def;
    protected ToolContext env;

    public AbstractDefinitionValidator() {
        super();
        this.def = null;
    }

    public AbstractDefinitionValidator(final Definition definition) {
        this.def = definition;
    }

    public AbstractDefinitionValidator(final Definition definition, ToolContext pEnv) {
        this.def = definition;
        this.env = pEnv;
    }

    public void addError(Location loc, String msg) {
        String errMsg = loc != null ? "line " + loc.getLineNumber() + " of " : "";
        errMsg = errMsg + def.getDocumentBaseURI() + " " + msg;
        addErrorMessage(errMsg);
    }
}
