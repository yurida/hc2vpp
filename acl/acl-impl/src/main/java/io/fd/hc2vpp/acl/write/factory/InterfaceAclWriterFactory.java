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

package io.fd.hc2vpp.acl.write.factory;

import com.google.common.collect.ImmutableSet;
import io.fd.hc2vpp.acl.AclIIds;
import io.fd.hc2vpp.acl.write.InterfaceAclCustomizer;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.write.WriterFactory;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceAclWriterFactory extends AbstractAclWriterFactory implements WriterFactory {

    @Override
    public void init(@Nonnull final ModifiableWriterRegistryBuilder registry) {
        registry.subtreeAddAfter(AclIIds.aclHandledChildren(AclIIds.IFC_ACL),
                new GenericWriter<>(AclIIds.ACLS_AP_INT,
                        new InterfaceAclCustomizer(futureAclFacade, interfaceContext, standardAclContext,
                                macIpAclContext)),
                aclRequiredIids());
    }

    static Set<InstanceIdentifier<?>> aclRequiredIids() {
        return ImmutableSet.of(AclIIds.IFC, AclIIds.IFC_ACL, AclIIds.ACLS_ACL);
    }
}
