/*
 * Copyright 2018 Deutsche Nationalbibliothek
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.metafacture.xml;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.ObjectReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class SerializeEncoderTest
{
    private SerializeEncoder encoder;

    @Mock
    private ObjectReceiver<String> receiver;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        encoder = new SerializeEncoder();
        encoder.setReceiver(receiver);
    }

    @Test
    public void serialize()
    {
        encoder.startRecord("1");
        encoder.literal("id", "1");
        encoder.startEntity("<>");
        encoder.literal("name", "joe");
        encoder.endEntity();
        encoder.endRecord();

        encoder.startRecord("1");
        encoder.literal("id", "1");
        encoder.startEntity("<>");
        encoder.literal("name", "joe");
        encoder.endEntity();
        encoder.endRecord();

        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ordered.verify(receiver).process("<stream>");
        ordered.verify(receiver).process("  <record id=\"1\">");
        ordered.verify(receiver).process("    <literal name=\"id\">1</literal>");
        ordered.verify(receiver).process("    <entity name=\"&lt;&gt;\">");
        ordered.verify(receiver).process("      <literal name=\"name\">joe</literal>");
        ordered.verify(receiver).process("    </entity>");
        ordered.verify(receiver).process("  </record>");
        ordered.verify(receiver).process("  <record id=\"1\">");
        ordered.verify(receiver).process("    <literal name=\"id\">1</literal>");
        ordered.verify(receiver).process("    <entity name=\"&lt;&gt;\">");
        ordered.verify(receiver).process("      <literal name=\"name\">joe</literal>");
        ordered.verify(receiver).process("    </entity>");
        ordered.verify(receiver).process("  </record>");
        ordered.verify(receiver).process("</stream>");
    }

    @Test
    public void serializeWithoutPrettyPrinting()
    {
        encoder.setPrettyPrint(false);

        encoder.startRecord("1");
        encoder.literal("id", "1");
        encoder.startEntity("names");
        encoder.literal("name", "joe");
        encoder.endEntity();
        encoder.endRecord();

        encoder.startRecord("1");
        encoder.literal("id", "1");
        encoder.startEntity("names");
        encoder.literal("name", "joe");
        encoder.endEntity();
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ordered.verify(receiver).process("<stream>");
        ordered.verify(receiver, times(2)).process(
                "<record id=\"1\">" +
                "<literal name=\"id\">1</literal>" +
                "<entity name=\"names\"><literal name=\"name\">joe</literal></entity>" +
                "</record>"
        );
        ordered.verify(receiver).process("</stream>");
    }

    @Test
    public void serializeWithNullValue()
    {
        encoder.setPrettyPrint(false);

        encoder.startRecord("1");
        encoder.literal("value", null);
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ordered.verify(receiver).process("<stream>");
        ordered.verify(receiver).process("<record id=\"1\"><literal name=\"value\"></literal></record>");
        ordered.verify(receiver).process("</stream>");
    }

    @Test
    public void omitDeclaration()
    {
        encoder.setPrettyPrint(false);
        encoder.setOmitDeclaration(true);

        encoder.startRecord("1");
        encoder.literal("value", null);
        encoder.endRecord();
        encoder.closeStream();

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).process("<stream>");
        ordered.verify(receiver).process("<record id=\"1\"><literal name=\"value\"></literal></record>");
        ordered.verify(receiver).process("</stream>");
    }

    @Test
    public void omitRoot()
    {
        encoder.setPrettyPrint(false);
        encoder.setOmitDeclaration(true);
        encoder.setOmitRoot(true);

        encoder.startRecord("1");
        encoder.literal("value", null);
        encoder.endRecord();
        encoder.closeStream();

        verify(receiver).process("<record id=\"1\"><literal name=\"value\"></literal></record>");
    }
}
