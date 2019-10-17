/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.internal.impldep.org.apache.nifi.extension.definition.extraction;

import org.gradle.internal.impldep.org.apache.nifi.extension.definition.ExtensionDefinition;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.ExtensionType;
import org.gradle.internal.impldep.org.apache.nifi.extension.definition.ServiceAPIDefinition;

import java.util.Set;

public class StandardExtensionDefinition implements ExtensionDefinition {

    private final ExtensionType extensionType;
    private final String extensionName;
    private final Set<ServiceAPIDefinition> providedServiceApis;

    public StandardExtensionDefinition(final ExtensionType extensionType, final String extensionName, final Set<ServiceAPIDefinition> providedServiceApis) {
        this.extensionType = extensionType;
        this.extensionName = extensionName;
        this.providedServiceApis = providedServiceApis;
    }

    @Override
    public ExtensionType getExtensionType() {
        return extensionType;
    }

    @Override
    public Set<ServiceAPIDefinition> getProvidedServiceAPIs() {
        return providedServiceApis;
    }

    @Override
    public String getExtensionName() {
        return extensionName;
    }

    @Override
    public String toString() {
        return "ExtensionDefinition[type=" + getExtensionType() + ", name=" + getExtensionName() + "]";
    }
}
