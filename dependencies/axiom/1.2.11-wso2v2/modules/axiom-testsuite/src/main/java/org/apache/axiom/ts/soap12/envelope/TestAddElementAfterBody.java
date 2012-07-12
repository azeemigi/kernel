/*
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
package org.apache.axiom.ts.soap12.envelope;

import org.apache.axiom.om.OMMetaFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPProcessingException;
import org.apache.axiom.ts.AxiomTestCase;

/**
 * Test that an attempt to add an arbitrary element to the SOAP envelope triggers an exception.
 */
public class TestAddElementAfterBody extends AxiomTestCase {
    public TestAddElementAfterBody(OMMetaFactory metaFactory) {
        super(metaFactory);
    }

    protected void runTest() throws Throwable {
        SOAPFactory soapFactory = metaFactory.getSOAP12Factory();
        SOAPEnvelope env = soapFactory.getDefaultEnvelope();
        try {
            env.addChild(soapFactory.createOMElement("test", "urn:test", "p"));
            fail("Expected SOAPProcessingException");
        } catch (SOAPProcessingException ex) {
            // Expected
        }
    }
}
