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

package io.fd.hc2vpp.lisp.translate.read;


import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import com.google.common.collect.ImmutableSet;
import io.fd.hc2vpp.common.translate.util.FutureJVppCustomizer;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.lisp.translate.read.dump.executor.params.LocatorDumpParams;
import io.fd.hc2vpp.lisp.translate.read.dump.executor.params.LocatorDumpParams.LocatorDumpParamsBuilder;
import io.fd.hc2vpp.lisp.translate.read.init.LispInitPathsMapper;
import io.fd.hc2vpp.lisp.translate.read.trait.LocatorReader;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.fd.honeycomb.translate.spi.read.InitializingListReaderCustomizer;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.util.read.cache.DumpCacheManager;
import io.fd.honeycomb.translate.util.read.cache.TypeAwareIdentifierCacheKeyFactory;
import io.fd.jvpp.core.dto.OneLocatorDetails;
import io.fd.jvpp.core.dto.OneLocatorDetailsReplyDump;
import io.fd.jvpp.core.future.FutureJVppCore;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.locator.sets.grouping.locator.sets.LocatorSet;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.locator.sets.grouping.locator.sets.LocatorSetBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.locator.sets.grouping.locator.sets.locator.set.Interface;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.locator.sets.grouping.locator.sets.locator.set.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.locator.sets.grouping.locator.sets.locator.set.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;


/**
 * Customizer for reading {@code Interface}<br> Currently not supported by jvpp
 */
public class InterfaceCustomizer
        extends FutureJVppCustomizer
        implements InitializingListReaderCustomizer<Interface, InterfaceKey, InterfaceBuilder>, LocatorReader, LispInitPathsMapper {

    private final NamingContext interfaceContext;
    private final NamingContext locatorSetContext;
    private final DumpCacheManager<OneLocatorDetailsReplyDump, LocatorDumpParams> dumpCacheManager;

    public InterfaceCustomizer(@Nonnull final FutureJVppCore futureJvpp, @Nonnull final NamingContext interfaceContext,
                               @Nonnull final NamingContext locatorSetContext) {
        super(futureJvpp);
        this.interfaceContext = checkNotNull(interfaceContext, "Interface context cannot be null");
        this.locatorSetContext = checkNotNull(locatorSetContext, "Locator set context cannot be null");
        this.dumpCacheManager =
                new DumpCacheManager.DumpCacheManagerBuilder<OneLocatorDetailsReplyDump, LocatorDumpParams>()
                        .withExecutor(createLocatorDumpExecutor(futureJvpp))
                        // must be cached per locator set
                        .withCacheKeyFactory(new TypeAwareIdentifierCacheKeyFactory(OneLocatorDetailsReplyDump.class, ImmutableSet.of(LocatorSet.class)))
                        .build();
    }

    @Override
    public InterfaceBuilder getBuilder(InstanceIdentifier<Interface> id) {
        return new InterfaceBuilder();
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Interface> id, InterfaceBuilder builder, ReadContext ctx)
            throws ReadFailedException {

        final String locatorSetName = id.firstKeyOf(LocatorSet.class).getName();
        final String referencedInterfaceName = id.firstKeyOf(Interface.class).getInterfaceRef();

        checkState(interfaceContext.containsIndex(referencedInterfaceName, ctx.getMappingContext()),
                "No interface mapping for name %s", referencedInterfaceName);
        checkState(locatorSetContext.containsIndex(locatorSetName, ctx.getMappingContext()),
                "No locator set mapping for name %s", locatorSetName);

        final int locatorSetIndexIndex = locatorSetContext.getIndex(locatorSetName, ctx.getMappingContext());
        final int referencedInterfaceIndex =
                interfaceContext.getIndex(referencedInterfaceName, ctx.getMappingContext());

        final LocatorDumpParams params =
                new LocatorDumpParamsBuilder().setLocatorSetIndex(locatorSetIndexIndex).build();

        final Optional<OneLocatorDetailsReplyDump> reply =
                dumpCacheManager.getDump(id, ctx.getModificationCache(), params);

        if (!reply.isPresent() || reply.get().oneLocatorDetails.isEmpty()) {
            return;
        }

        final OneLocatorDetails details = reply.get()
                .oneLocatorDetails
                .stream()
                .filter(a -> a.swIfIndex == referencedInterfaceIndex)
                .collect(RWUtils.singleItemCollector());

        final String interfaceRef = interfaceContext.getName(details.swIfIndex, ctx.getMappingContext());

        builder.setPriority((short) Byte.toUnsignedInt(details.priority));
        builder.setWeight((short) Byte.toUnsignedInt(details.weight));
        builder.setInterfaceRef(interfaceRef);
        builder.withKey(new InterfaceKey(interfaceRef));
    }

    @Override
    public List<InterfaceKey> getAllIds(InstanceIdentifier<Interface> id, ReadContext context)
            throws ReadFailedException {

        checkState(id.firstKeyOf(LocatorSet.class) != null, "Cannot find reference to parent locator set");
        final String name = id.firstKeyOf(LocatorSet.class).getName();

        checkState(locatorSetContext.containsIndex(name, context.getMappingContext()), "No mapping for %s", name);
        final LocatorDumpParams params = new LocatorDumpParamsBuilder()
                .setLocatorSetIndex(locatorSetContext.getIndex(name, context.getMappingContext())).build();

        final Optional<OneLocatorDetailsReplyDump> reply =
                dumpCacheManager.getDump(id, context.getModificationCache(), params);

        if (!reply.isPresent() || reply.get().oneLocatorDetails.isEmpty()) {
            return Collections.emptyList();
        }

        return reply.get()
                .oneLocatorDetails
                .stream()
                .map(a -> new InterfaceKey(interfaceContext.getName(a.swIfIndex, context.getMappingContext())))
                .collect(Collectors.toList());
    }

    @Override
    public void merge(Builder<? extends DataObject> builder, List<Interface> readData) {
        ((LocatorSetBuilder) builder).setInterface(readData);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<Interface> instanceIdentifier, @Nonnull Interface anInterface, @Nonnull ReadContext readContext) {
        final KeyedInstanceIdentifier<Interface, InterfaceKey> identifier = lispLocatorSetPath(instanceIdentifier)
                .child(Interface.class, instanceIdentifier.firstKeyOf(Interface.class));
        return Initialized.create(identifier, anInterface);
    }
}
