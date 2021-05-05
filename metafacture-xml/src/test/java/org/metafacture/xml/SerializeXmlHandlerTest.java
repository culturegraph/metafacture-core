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
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.xml.sax.helpers.AttributesImpl;
import static org.mockito.Mockito.inOrder;

public class SerializeXmlHandlerTest
{

    private SerializeXmlHandler handler;

    @Mock
    private StreamReceiver receiver;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        handler = new SerializeXmlHandler();
        handler.setReceiver(receiver);
    }

    @Test(expected = MetafactureException.class)
    public void complainAboutMissingRootTagNamedStream()
    {
        handler.startElement("", "notStream", "notStream", new AttributesImpl());
    }

    @Test(expected = MetafactureException.class)
    public void complainAboutASecondlyOpenedRootTag()
    {
        handler.startElement("", "stream", "stream", new AttributesImpl());
        handler.startElement("", "stream", "stream", new AttributesImpl());
    }

    @Test(expected = MetafactureException.class)
    public void complainAboutAboutASecondlyClosedRootTag()
    {
        handler.endElement("", "stream", "stream");
        handler.endElement("", "stream", "stream");
    }

    @Test
    public void readXmlStream()
    {
        // <stream>
        handler.startElement("", "stream", "stream", new AttributesImpl());

        // <record id="1">
        final AttributesImpl recordAttributes = new AttributesImpl();
        recordAttributes.addAttribute("", "id", "id", "ID", "1");
        handler.startElement("", "record", "record", recordAttributes);

        // <literal name="id">1</literal>
        final AttributesImpl literal1Attributes = new AttributesImpl();
        literal1Attributes.addAttribute("", "name", "name", "CDATA", "id");
        handler.startElement("", "literal", "literal", literal1Attributes);
        handler.characters("1".toCharArray(), 0, 1);
        handler.endElement("", "literal", "literal");

        // <entity name="names">
        final AttributesImpl entityAttributes = new AttributesImpl();
        entityAttributes.addAttribute("", "name", "name", "CDATA", "names");
        handler.startElement("", "entity", "entity", entityAttributes);

        // <literal name="name">"joe"</literal>
        final AttributesImpl literal2Attributes = new AttributesImpl();
        literal2Attributes.addAttribute("", "name", "name", "CDATA", "name");
        handler.startElement("", "literal", "literal", literal2Attributes);
        handler.characters("\"joe\"".toCharArray(), 0, "\"joe\"".length());
        handler.endElement("", "literal", "literal");

        // </entity>
        handler.endElement("", "entity", "entity");

        // </record>
        handler.endElement("", "record", "record");

        // <stream>
        handler.endElement("", "stream", "stream");

        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startRecord("1");
        ordered.verify(receiver).literal("id", "1");
        ordered.verify(receiver).startEntity("names");
        ordered.verify(receiver).literal("name", "\"joe\"");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endRecord();
        ordered.verify(receiver).closeStream();
        ordered.verifyNoMoreInteractions();
    }
}
