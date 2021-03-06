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

package io.fd.hc2vpp.vpp.classifier.read.acl;

import io.fd.hc2vpp.vpp.classifier.context.VppClassifierContextManager;
import io.fd.honeycomb.translate.MappingContext;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.Ip4Acl;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.Ip4AclBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.Ip6Acl;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.Ip6AclBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.L2Acl;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.L2AclBuilder;

interface AclReader {

    default L2Acl readL2Acl(final int l2TableId, @Nonnull final VppClassifierContextManager classifyTableContext,
                            @Nonnull final MappingContext mappingContext) {
        if (l2TableId == ~0) {
            return null;
        }
        return new L2AclBuilder()
            .setClassifyTable(classifyTableContext.getTableName(l2TableId, mappingContext)).build();
    }

    default Ip4Acl readIp4Acl(final int ip4TableId, @Nonnull final VppClassifierContextManager classifyTableContext,
                              @Nonnull final MappingContext mappingContext) {
        if (ip4TableId == ~0) {
            return null;
        }
        return new Ip4AclBuilder()
            .setClassifyTable(classifyTableContext.getTableName(ip4TableId, mappingContext)).build();
    }

    default Ip6Acl readIp6Acl(final int ip6TableId, @Nonnull final VppClassifierContextManager classifyTableContext,
                              @Nonnull final MappingContext mappingContext) {
        if (ip6TableId == ~0) {
            return null;
        }
        return new Ip6AclBuilder()
            .setClassifyTable(classifyTableContext.getTableName(ip6TableId, mappingContext)).build();
    }
}
