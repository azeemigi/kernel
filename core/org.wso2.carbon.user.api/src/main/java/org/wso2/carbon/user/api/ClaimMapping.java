/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.user.api;

/**
 * This class represents the claim mapping between a given claim to the actual
 * attribute which resides in the user store.
 */
public class ClaimMapping {

    /**
     * The claim object
     */
    protected Claim claim;

    /**
     * The mapped attribute in the user store
     */
    protected String mappedAttribute;

    public ClaimMapping() {

    }

    public ClaimMapping(Claim claim, String mappedAttribute) {
        this.claim = claim;
        this.mappedAttribute = mappedAttribute;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getMappedAttribute() {
        return mappedAttribute;
    }

    public void setMappedAttribute(String mappedAttribute) {
        this.mappedAttribute = mappedAttribute;
    }
}