/*
 * Copyright (c) 2015 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.honeycomb.lisp.translate.write;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import io.fd.honeycomb.translate.vpp.util.NamingContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.honeycomb.vpp.test.write.WriterCustomizerTest;
import io.fd.vpp.jvpp.core.dto.LispAddDelLocatorSet;
import io.fd.vpp.jvpp.core.dto.LispAddDelLocatorSetReply;
import io.fd.vpp.jvpp.core.dto.LispLocatorSetDetails;
import io.fd.vpp.jvpp.core.dto.LispLocatorSetDetailsReplyDump;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev161214.locator.sets.grouping.LocatorSets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev161214.locator.sets.grouping.locator.sets.LocatorSet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev161214.locator.sets.grouping.locator.sets.LocatorSetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev161214.locator.sets.grouping.locator.sets.LocatorSetKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev161214.locator.sets.grouping.locator.sets.locator.set.InterfaceBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LocatorSetCustomizerTest extends WriterCustomizerTest {

    private LocatorSetCustomizer customizer;

    @Override
    public void setUp() {
        customizer = new LocatorSetCustomizer(api, new NamingContext("locator-set", "locator-set-context"));
    }

    @Test(expected = NullPointerException.class)
    public void testWriteCurrentAttributesNullData() throws WriteFailedException {
        customizer.writeCurrentAttributes(null, null, writeContext);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteCurrentAttributesBadData() throws WriteFailedException {
        customizer.writeCurrentAttributes(null, mock(LocatorSet.class), writeContext);
    }

    @Test
    public void testWriteCurrentAttributes() throws WriteFailedException, InterruptedException, ExecutionException {
        noMappingDefined(mappingContext, "Locator", "locator-set-context");
        LocatorSet locatorSet = new LocatorSetBuilder()
                .setName("Locator")
                .setInterface(Arrays.asList(new InterfaceBuilder().build()))
                .build();

        InstanceIdentifier<LocatorSet> validId =
                InstanceIdentifier.create(LocatorSets.class).child(LocatorSet.class, new LocatorSetKey("Locator"));


        ArgumentCaptor<LispAddDelLocatorSet> locatorSetCaptor = ArgumentCaptor.forClass(LispAddDelLocatorSet.class);

        when(api.lispAddDelLocatorSet(any(LispAddDelLocatorSet.class)))
                .thenReturn(future(new LispAddDelLocatorSetReply()));
        when(writeContext.readAfter(validId)).thenReturn(Optional.of(locatorSet));

        final LispLocatorSetDetailsReplyDump reply = new LispLocatorSetDetailsReplyDump();
        LispLocatorSetDetails details = new LispLocatorSetDetails();
        details.lsName = "Locator".getBytes(StandardCharsets.UTF_8);
        reply.lispLocatorSetDetails = ImmutableList.of(details);

        customizer.writeCurrentAttributes(validId, locatorSet, writeContext);

        verify(api, times(1)).lispAddDelLocatorSet(locatorSetCaptor.capture());

        LispAddDelLocatorSet request = locatorSetCaptor.getValue();

        assertNotNull(request);
        assertEquals("Locator", new String(request.locatorSetName));
        assertEquals(1, request.isAdd);
    }

    @Test
    public void testUpdateCurrentAttributes() throws WriteFailedException {
        final InstanceIdentifier<LocatorSet> identifier = InstanceIdentifier.create(LocatorSet.class);
        try {
            customizer
                    .updateCurrentAttributes(identifier, mock(LocatorSet.class), mock(LocatorSet.class), writeContext);
        } catch (WriteFailedException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
            assertEquals(identifier, e.getFailedId());
            return;
        }
        fail("Test should have failed");
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteCurrentAttributesNullData() throws WriteFailedException {
        customizer.deleteCurrentAttributes(null, null, writeContext);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteCurrentAttributesBadData() throws WriteFailedException {
        customizer.deleteCurrentAttributes(null, mock(LocatorSet.class), writeContext);
    }

    @Test
    public void testDeleteCurrentAttributes() throws InterruptedException, ExecutionException, WriteFailedException {
        LocatorSet locatorSet = new LocatorSetBuilder()
                .setName("Locator")
                .build();

        ArgumentCaptor<LispAddDelLocatorSet> locatorSetCaptor = ArgumentCaptor.forClass(LispAddDelLocatorSet.class);

        when(api.lispAddDelLocatorSet(any(LispAddDelLocatorSet.class)))
                .thenReturn(future(new LispAddDelLocatorSetReply()));

        customizer.deleteCurrentAttributes(null, locatorSet, writeContext);

        verify(api, times(1)).lispAddDelLocatorSet(locatorSetCaptor.capture());

        LispAddDelLocatorSet request = locatorSetCaptor.getValue();

        assertNotNull(request);
        assertEquals("Locator", new String(request.locatorSetName));
        assertEquals(0, request.isAdd);
    }
}
