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

package io.fd.honeycomb.v3po.translate.v3po.interfacesstate;

import io.fd.honeycomb.v3po.translate.read.ReadContext;
import io.fd.honeycomb.v3po.translate.read.ReadFailedException;
import io.fd.honeycomb.v3po.translate.spi.read.ChildReaderCustomizer;
import io.fd.honeycomb.v3po.translate.v3po.util.FutureJVppCustomizer;
import io.fd.honeycomb.v3po.translate.v3po.util.NamingContext;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.VppInterfaceStateAugmentationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.interfaces.state._interface.L2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.v3po.rev150105.interfaces.state._interface.L2Builder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.openvpp.jvpp.future.FutureJVpp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customizer for reading ietf-interfaces:interfaces-state/interface/iface_name/v3po:l2
 */
public class L2Customizer extends FutureJVppCustomizer implements ChildReaderCustomizer<L2, L2Builder> {

    private static final Logger LOG = LoggerFactory.getLogger(L2Customizer.class);
    private final InterconnectionReadUtils icReadUtils;

    public L2Customizer(@Nonnull final FutureJVpp futureJvpp,
                        @Nonnull final NamingContext interfaceContext,
                        @Nonnull final NamingContext bridgeDomainContext) {
        super(futureJvpp);
        this.icReadUtils = new InterconnectionReadUtils(futureJvpp, interfaceContext, bridgeDomainContext);
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder, @Nonnull final L2 readValue) {
        ((VppInterfaceStateAugmentationBuilder) parentBuilder).setL2(readValue);
    }

    @Nonnull
    @Override
    public L2Builder getBuilder(@Nonnull final InstanceIdentifier<L2> id) {
        return new L2Builder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<L2> id, @Nonnull final L2Builder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        LOG.debug("Reading attributes for L2: {}", id);
        final InterfaceKey key = id.firstKeyOf(Interface.class);
        final String ifaceName = key.getName();

        builder.setInterconnection(icReadUtils.readInterconnection(ifaceName, ctx));
    }
}