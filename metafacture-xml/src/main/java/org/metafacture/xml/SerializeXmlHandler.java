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

import org.metafacture.framework.FluxCommand;
import org.metafacture.framework.MetafactureException;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.XmlReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultXmlPipe;
import org.xml.sax.Attributes;

@Description("Deserialize a XML encoded Metafacture stream.")
@In(XmlReceiver.class)
@Out(StreamReceiver.class)
@FluxCommand("handle-serialize-xml")
public class SerializeXmlHandler extends DefaultXmlPipe<StreamReceiver>
{

    final private String ID = "id";
    final private String NAME = "name";

    private int streamTagCount;
    private String currentTag;
    private String currentLiteralName;
    private StringBuilder stringBuilder;

    public SerializeXmlHandler()
    {
        streamTagCount = 0;
        stringBuilder = new StringBuilder();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
    {
        currentTag = localName.toUpperCase();
        switch (currentTag)
        {
            case "STREAM":
                if (streamTagCount > 0)
                {
                    throw new MetafactureException("Root tag 'stream' opened a second time.");
                }
                streamTagCount += 1;
            case "RECORD":
                String identifier = attributes.getValue(ID);
                getReceiver().startRecord(identifier);
                break;
            case "ENTITY":
                String name = attributes.getValue(NAME);
                getReceiver().startEntity(name);
                break;
            case "LITERAL":
                currentLiteralName = attributes.getValue(NAME);
                break;
            default:
                String message = "Unknown tag '%s'. Expected 'stream', 'record', 'entity' or 'literal'.";
                throw new MetafactureException(String.format(message, currentTag));
        }
    }

    @Override
    public void characters(final char[] chars, final int start, final int length)
    {
        if (currentTag.equals("LITERAL"))
        {
            this.stringBuilder.append(chars, start, length);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
    {
        currentTag = localName.toUpperCase();
        switch (currentTag)
        {
            case "STREAM":
                streamTagCount -= 1;
                if (streamTagCount != 0)
                {
                    throw new MetafactureException("Root tag 'stream' closed a second time.");
                }
                else
                {
                    getReceiver().closeStream();
                }
                break;
            case "RECORD":
                getReceiver().endRecord();
                break;
            case "ENTITY":
                getReceiver().endEntity();
                break;
            case "LITERAL":
                String currentLiteralValue = stringBuilder.toString().trim();
                getReceiver().literal(currentLiteralName, currentLiteralValue);
                stringBuilder = new StringBuilder();
                break;
            default:
                String message = "Unknown tag '%s'. Expected 'stream', 'record', 'entity' or 'literal'.";
                throw new MetafactureException(String.format(message, currentTag));
        }
    }
}
