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

package io.fd.hc2vpp.vpp.classifier.write.acl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import io.fd.hc2vpp.common.test.write.WriterCustomizerTest;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.vpp.classifier.context.VppClassifierContextManager;
import io.fd.hc2vpp.vpp.classifier.write.acl.ingress.AclCustomizer;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.jvpp.VppBaseCallException;
import io.fd.jvpp.core.dto.InputAclSetInterface;
import io.fd.jvpp.core.dto.InputAclSetInterfaceReply;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp._interface.acl.rev190527.VppInterfaceAclAugmentation;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.L2Acl;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.acl.base.attributes.L2AclBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.vpp.acl.attributes.Acl;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.vpp.acl.attributes.acl.Ingress;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.vpp.classifier.acl.rev170503.vpp.acl.attributes.acl.IngressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclCustomizerTest extends WriterCustomizerTest {

    private static final String IFC_TEST_INSTANCE = "ifc-test-instance";
    private static final String IF_NAME = "local0";
    private static final int IF_INDEX = 1;
    private static final int ACL_TABLE_INDEX = 0;
    private static final String ACL_TABLE_NAME = "table0";
    @Mock
    private VppClassifierContextManager classifyTableContext;
    private AclCustomizer customizer;

    private static InputAclSetInterface generateInputAclSetInterface(final byte isAdd, final int ifIndex,
                                                                     final int l2TableIndex) {
        final InputAclSetInterface request = new InputAclSetInterface();
        request.isAdd = isAdd;
        request.l2TableIndex = l2TableIndex;
        request.ip4TableIndex = ~0;
        request.ip6TableIndex = ~0;
        request.swIfIndex = ifIndex;
        return request;
    }

    @Override
    public void setUpTest() {
        defineMapping(mappingContext, IF_NAME, IF_INDEX, IFC_TEST_INSTANCE);
        customizer = new AclCustomizer(api, new NamingContext("generatedInterfaceName", IFC_TEST_INSTANCE),
            classifyTableContext);
    }

    private InstanceIdentifier<Ingress> getAclId(final String name) {
        return InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(name)).augmentation(
            VppInterfaceAclAugmentation.class).child(Acl.class).child(Ingress.class);
    }

    private Ingress generateAcl(final String tableName) {
        final IngressBuilder builder = new IngressBuilder();
        final L2Acl l2Acl = new L2AclBuilder().setClassifyTable(tableName).build();
        builder.setL2Acl(l2Acl);
        return builder.build();
    }

    private void whenInputAclSetInterfaceThenSuccess() {
        doReturn(future(new InputAclSetInterfaceReply())).when(api)
            .inputAclSetInterface(any(InputAclSetInterface.class));
    }

    private void whenInputAclSetInterfaceThenFailure() {
        doReturn(failedFuture()).when(api).inputAclSetInterface(any(InputAclSetInterface.class));
    }

    @Test
    public void testCreate() throws Exception {
        final Ingress acl = generateAcl(ACL_TABLE_NAME);
        final InstanceIdentifier<Ingress> id = getAclId(IF_NAME);

        whenInputAclSetInterfaceThenSuccess();

        customizer.writeCurrentAttributes(id, acl, writeContext);

        verify(api).inputAclSetInterface(generateInputAclSetInterface((byte) 1, IF_INDEX, ACL_TABLE_INDEX));
    }

    @Test
    public void testCreateFailed() throws Exception {
        final Ingress acl = generateAcl(ACL_TABLE_NAME);
        final InstanceIdentifier<Ingress> id = getAclId(IF_NAME);

        whenInputAclSetInterfaceThenFailure();

        try {
            customizer.writeCurrentAttributes(id, acl, writeContext);
        } catch (WriteFailedException e) {
            assertTrue(e.getCause() instanceof VppBaseCallException);
            verify(api).inputAclSetInterface(generateInputAclSetInterface((byte) 1, IF_INDEX, ACL_TABLE_INDEX));
            return;
        }
        fail("WriteFailedException.CreateFailedException was expected");
    }

    @Test
    public void testDelete() throws Exception {
        final Ingress acl = generateAcl(ACL_TABLE_NAME);
        final InstanceIdentifier<Ingress> id = getAclId(IF_NAME);

        whenInputAclSetInterfaceThenSuccess();

        customizer.deleteCurrentAttributes(id, acl, writeContext);

        verify(api).inputAclSetInterface(generateInputAclSetInterface((byte) 0, IF_INDEX, ACL_TABLE_INDEX));
    }

    @Test
    public void testDeleteFailed() throws Exception {
        final Ingress acl = generateAcl(ACL_TABLE_NAME);
        final InstanceIdentifier<Ingress> id = getAclId(IF_NAME);

        whenInputAclSetInterfaceThenFailure();

        try {
            customizer.deleteCurrentAttributes(id, acl, writeContext);
        } catch (WriteFailedException e) {
            assertTrue(e.getCause() instanceof VppBaseCallException);
            verify(api).inputAclSetInterface(generateInputAclSetInterface((byte) 0, IF_INDEX, ACL_TABLE_INDEX));
            return;
        }
        fail("WriteFailedException.DeleteFailedException was expected");
    }
}
