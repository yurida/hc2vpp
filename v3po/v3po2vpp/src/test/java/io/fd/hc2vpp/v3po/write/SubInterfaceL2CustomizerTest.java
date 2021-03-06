/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
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

package io.fd.hc2vpp.v3po.write;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fd.hc2vpp.common.test.write.WriterCustomizerTest;
import io.fd.hc2vpp.common.translate.util.ByteDataTranslator;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.jvpp.core.dto.SwInterfaceSetL2Bridge;
import io.fd.jvpp.core.dto.SwInterfaceSetL2BridgeReply;
import io.fd.jvpp.core.types.L2PortType;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.l2.config.attributes.interconnection.BridgeBasedBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.vlan.rev190527.SubinterfaceAugmentation;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.vlan.rev190527.interfaces._interface.SubInterfaces;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.vlan.rev190527.interfaces._interface.sub.interfaces.SubInterface;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.vlan.rev190527.interfaces._interface.sub.interfaces.SubInterfaceKey;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.vlan.rev190527.sub._interface.l2.config.attributes.L2;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.vlan.rev190527.sub._interface.l2.config.attributes.L2Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubInterfaceL2CustomizerTest extends WriterCustomizerTest implements ByteDataTranslator {
    private static final String IFACE_CTX_NAME = "interface-ctx";
    private static final String IF_NAME = "local0";
    private static final int IF_INDEX = 1;
    private static final String SUBIF_NAME = "local0.0";
    private static final int SUBIF_INDEX = 11;
    private static final long SUBIF_ID = 0;

    private static final InstanceIdentifier<L2> IID =
        InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(IF_NAME)).augmentation(
            SubinterfaceAugmentation.class).child(SubInterfaces.class)
            .child(SubInterface.class, new SubInterfaceKey(SUBIF_ID)).child(L2.class);


    private static final String BD_CTX_NAME = "bd-ctx";
    private static final String BD_NAME = "test_bd";
    private static final int BD_INDEX = 13;

    private SubInterfaceL2Customizer customizer;

    @Override
    protected void setUpTest() throws Exception {
        customizer = new SubInterfaceL2Customizer(api, new NamingContext("ifacePrefix", IFACE_CTX_NAME),
            new NamingContext("bdPrefix", BD_CTX_NAME));
        defineMapping(mappingContext, IF_NAME, IF_INDEX, IFACE_CTX_NAME);
        defineMapping(mappingContext, SUBIF_NAME, SUBIF_INDEX, IFACE_CTX_NAME);
        defineMapping(mappingContext, BD_NAME, BD_INDEX, BD_CTX_NAME);
    }

    @Test
    public void testWrite() throws WriteFailedException {
        final boolean bvi = true;
        when(api.swInterfaceSetL2Bridge(any())).thenReturn(future(new SwInterfaceSetL2BridgeReply()));
        customizer.writeCurrentAttributes(IID, l2(bvi), writeContext);
        verify(api).swInterfaceSetL2Bridge(bridgeRequest(bvi, true));
    }

    @Test
    public void testUpdate() throws WriteFailedException {
        final boolean bvi = false;
        when(api.swInterfaceSetL2Bridge(any())).thenReturn(future(new SwInterfaceSetL2BridgeReply()));
        customizer.updateCurrentAttributes(IID, l2(true), l2(bvi), writeContext);
        verify(api).swInterfaceSetL2Bridge(bridgeRequest(bvi, true));
    }

    @Test
    public void testDelete() throws WriteFailedException {
        final boolean bvi = true;
        when(api.swInterfaceSetL2Bridge(any())).thenReturn(future(new SwInterfaceSetL2BridgeReply()));
        customizer.deleteCurrentAttributes(IID, l2(bvi), writeContext);
        verify(api).swInterfaceSetL2Bridge(bridgeRequest(bvi, false));
    }

    private L2 l2(final boolean bvi) {
        return new L2Builder().setInterconnection(new BridgeBasedBuilder().setBridgedVirtualInterface(bvi)
            .setBridgeDomain(BD_NAME).setSplitHorizonGroup((short) 123).build()).build();
    }

    private SwInterfaceSetL2Bridge bridgeRequest(final boolean bvi, final boolean enable) {
        final SwInterfaceSetL2Bridge request = new SwInterfaceSetL2Bridge();
        request.bdId = BD_INDEX;
        request.rxSwIfIndex = SUBIF_INDEX;
        request.portType = bvi ? L2PortType.L2_API_PORT_TYPE_BVI : L2PortType.L2_API_PORT_TYPE_NORMAL;
        request.enable = booleanToByte(enable);
        request.shg = 123;
        return request;
    }
}
