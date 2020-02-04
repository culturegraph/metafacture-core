/*
 * Copyright 2020 Fabian Steeg, hbz
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
package org.metafacture.html;

import static org.mockito.Mockito.inOrder;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.metafacture.framework.StreamReceiver;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for class {@link HtmlDecoder}.
 *
 * @author Fabian Steeg
 *
 */
public final class HtmlDecoderTest {

    @Mock
    private StreamReceiver receiver;

    private HtmlDecoder htmlDecoder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        htmlDecoder = new HtmlDecoder();
        htmlDecoder.setReceiver(receiver);
    }

    @Test
    public void htmlElementsAsEntities() {
        htmlDecoder.process(new StringReader("<h1>Header</h1><p>Paragraph</p>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("h1");
        ordered.verify(receiver).literal("value", "Header");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).startEntity("p");
        ordered.verify(receiver).literal("value", "Paragraph");
        ordered.verify(receiver).endEntity();
    }

    @Test
    public void nestedEntities() {
        htmlDecoder.process(new StringReader("<ul><li>Item</li><ul>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("ul");
        ordered.verify(receiver).startEntity("li");
        ordered.verify(receiver).literal("value", "Item");
        ordered.verify(receiver).endEntity();
        ordered.verify(receiver).endEntity();
    }

    @Test
    public void htmlAttributesAsLiterals() {
        htmlDecoder.process(new StringReader("<p class=lead>Text"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("p");
        ordered.verify(receiver).literal("class", "lead");
        ordered.verify(receiver).literal("value", "Text");
        ordered.verify(receiver).endEntity();
    }
    
    @Test
    public void htmlScriptElementData() {
        htmlDecoder.process(new StringReader("<script type=application/ld+json>{\"id\":\"theId\"}</script>"));
        final InOrder ordered = inOrder(receiver);
        ordered.verify(receiver).startEntity("script");
        ordered.verify(receiver).literal("type", "application/ld+json");
        ordered.verify(receiver).literal("value", "{\"id\":\"theId\"}");
        ordered.verify(receiver).endEntity();
    }

}
