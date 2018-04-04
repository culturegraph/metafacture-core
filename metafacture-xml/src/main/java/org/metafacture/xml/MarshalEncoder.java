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
import org.metafacture.framework.ObjectReceiver;
import org.metafacture.framework.StreamReceiver;
import org.metafacture.framework.annotations.Description;
import org.metafacture.framework.annotations.In;
import org.metafacture.framework.annotations.Out;
import org.metafacture.framework.helpers.DefaultStreamPipe;

@Description("MarshalEncoder a stream to XML.")
@In(StreamReceiver.class)
@Out(String.class)
@FluxCommand("marshal-to-xml")
public class MarshalEncoder extends DefaultStreamPipe<ObjectReceiver<String>>
{
    final private String xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    final private String spacer = "  ";

    private boolean initStream;
    private boolean prettyPrint;
    private int indentation;
    private StringBuilder stringBuilder;

    private boolean omitDeclaration = false;
    private boolean omitRoot = false;

    public MarshalEncoder()
    {
        this.indentation = 0;
        this.prettyPrint = true;
        this.initStream = true;
    }

    public void setPrettyPrint(boolean prettyPrint)
    {
        this.prettyPrint = prettyPrint;
    }

    public void setOmitDeclaration(boolean omitDeclaration)
    {
        this.omitDeclaration = omitDeclaration;
    }

    public void setOmitRoot(boolean omitRoot)
    {
        this.omitRoot = omitRoot;
    }

    @Override
    public void startRecord(final String identifier)
    {
        if (initStream)
        {
            if (!omitDeclaration)
            {
                getReceiver().process(xmlDeclaration);
            }
            if (!omitRoot)
            {
                getReceiver().process("<stream>");
            }
            initStream = false;
        }

        String elem = "<record id=\"" + identifier + "\">";
        if (prettyPrint)
        {
            indentation++;
            getReceiver().process(spacer + elem);
        }
        else
        {
            stringBuilder = new StringBuilder();
            stringBuilder.append(elem);
        }
    }

    @Override
    public void startEntity(final String name)
    {
        String elem = "<entity name=\"" + escape(name) + "\">";
        if (prettyPrint)
        {
            indentation++;
            getReceiver().process(separators(indentation, spacer) + elem);
        }
        else
        {
            stringBuilder.append(elem);
        }
    }

    @Override
    public void literal(final String name, final String value)
    {
        String elem = "<literal name=\"" + escape(name) + "\">" + escape(value) + "</literal>";
        if (prettyPrint)
        {
            getReceiver().process(separators(indentation+1, spacer) + elem);
        }
        else
        {
            stringBuilder.append(elem);
        }
    }

    @Override
    public void endEntity()
    {
        String elem = "</entity>";
        if (prettyPrint)
        {
            getReceiver().process(separators(indentation, spacer) + elem);
            indentation--;
        }
        else
        {
            stringBuilder.append(elem);
        }
    }

    @Override
    public void endRecord()
    {
        String elem = "</record>";
        if (prettyPrint)
        {
            getReceiver().process(spacer + "</record>");
            indentation--;
        }
        else
        {
            stringBuilder.append(elem);
            getReceiver().process(stringBuilder.toString().trim());
        }
    }

    @Override
    public void onCloseStream()
    {
        if (!omitRoot)
        {
            getReceiver().process("</stream>");
        }
    }

    @Override
    public void onResetStream()
    {
        this.indentation = 0;
        this.initStream = true;
    }

    private String separators(int amount, String separator)
    {
        StringBuilder sb = new StringBuilder(separator);
        for (int i = 0; i < amount - 1; i++)
        {
            sb.append(separator);
        }
        return sb.toString();
    }

    private String escape(final String s)
    {
        if (s == null || s.isEmpty())
        {
            return "";
        }

        StringBuilder result = new StringBuilder();
        final int len = s.length();
        for (int i = 0; i < len; ++i) {
            final char c = s.charAt(i);
            final String entityName;
            switch (c) {
                case '&':
                    entityName = "amp";
                    break;
                case '<':
                    entityName = "lt";
                    break;
                case '>':
                    entityName = "gt";
                    break;
                case '\'':
                    entityName = "apos";
                    break;
                case '"':
                    entityName = "quot";
                    break;
                default:
                    entityName = null;
                    break;
            }

            if (entityName == null) {
                result.append(c);
            } else {
                result.append('&');
                result.append(entityName);
                result.append(';');
            }
        }
        return result.toString().trim();
    }
}
