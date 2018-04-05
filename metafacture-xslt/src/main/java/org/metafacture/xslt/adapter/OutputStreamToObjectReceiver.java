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
package org.metafacture.xslt.adapter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.metafacture.framework.ObjectReceiver;

public class OutputStreamToObjectReceiver extends OutputStream {

    private ObjectReceiver<String> receiver;
    private ByteArrayOutputStream byteArrayOutputStream;
    private Charset utf8 = StandardCharsets.UTF_8;

    public OutputStreamToObjectReceiver(ObjectReceiver<String> receiver)
    {
        this.receiver = receiver;
        int KB512 = 524288;
        this.byteArrayOutputStream = new ByteArrayOutputStream(KB512);
    }

    public OutputStreamToObjectReceiver(ObjectReceiver<String> receiver, Charset utf8)
    {
        this(receiver);
        this.utf8 = utf8;
    }

    @Override
    public void write(int i) throws IOException {
        byteArrayOutputStream.write(i);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        byteArrayOutputStream.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int i, int i1 ) {
        byteArrayOutputStream.write(bytes, i, i1);
    }

    @Override
    public void flush() throws IOException
    {
        byteArrayOutputStream.flush();
        receiver.process(byteArrayOutputStream.toString(utf8.name()));
        byteArrayOutputStream.reset();
    }

    @Override
    public void close() throws IOException
    {
        byteArrayOutputStream.close();
    }
}
