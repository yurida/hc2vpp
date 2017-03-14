/*
 * Copyright (c) 2017 Cisco and/or its affiliates.
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

package io.fd.hc2vpp.lisp.translate.write;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fd.hc2vpp.common.test.write.WriterCustomizerTest;
import io.fd.vpp.jvpp.core.dto.LispUsePetr;
import io.fd.vpp.jvpp.core.dto.LispUsePetrReply;
import java.util.Arrays;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170315.use.petr.cfg.grouping.PetrCfg;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev170315.use.petr.cfg.grouping.PetrCfgBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PetrCfgCustomizerTest extends WriterCustomizerTest {

    private static final InstanceIdentifier<PetrCfg> ID = InstanceIdentifier.create(PetrCfg.class);

    private PetrCfgCustomizer customizer;
    private PetrCfg enabledCfg;
    private PetrCfg disabledCfg;

    @Captor
    private ArgumentCaptor<LispUsePetr> requestCaptor;

    @Override
    public void setUpTest() throws Exception {
        customizer = new PetrCfgCustomizer(api);
        enabledCfg = new PetrCfgBuilder().setPetrAddress(new IpAddress(new Ipv4Address("192.168.2.1"))).build();
        disabledCfg = new PetrCfgBuilder().build();
        when(api.lispUsePetr(any(LispUsePetr.class))).thenReturn(future(new LispUsePetrReply()));
    }

    @Test
    public void testWriteCurrentAttributes() throws Exception {
        customizer.writeCurrentAttributes(ID, enabledCfg, writeContext);
        verifyEnabledInvoked();
    }

    @Test
    public void testUpdateCurrentAttributesToEnabled() throws Exception {
        customizer.updateCurrentAttributes(ID, disabledCfg, enabledCfg, writeContext);
        verifyEnabledInvoked();
    }

    @Test
    public void testUpdateCurrentAttributesToDisabled() throws Exception {
        customizer.updateCurrentAttributes(ID, enabledCfg, disabledCfg, writeContext);
        verifyDisabledInvoked();
    }

    @Test
    public void testDeleteCurrentAttributes() throws Exception {
        customizer.deleteCurrentAttributes(ID, disabledCfg, writeContext);
        verifyDisabledInvoked();
    }

    private void verifyEnabledInvoked() {
        verify(api, times(1)).lispUsePetr(requestCaptor.capture());

        final LispUsePetr cfg = requestCaptor.getValue();
        assertEquals(1, cfg.isIp4);
        assertTrue(Arrays.equals(new byte[]{-64, -88, 2, 1}, cfg.address));
        assertEquals(1, cfg.isAdd);
    }

    private void verifyDisabledInvoked() {
        verify(api, times(1)).lispUsePetr(requestCaptor.capture());

        final LispUsePetr cfg = requestCaptor.getValue();
        assertNull(cfg.address);
        assertEquals(0, cfg.isAdd);
    }
}