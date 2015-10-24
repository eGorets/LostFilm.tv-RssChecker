/*
 * Copyright (C) 2011 Mats Hofman <http://matshofman.nl/contact/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.lostfilm.com.android_rss_reader_library;

import android.annotation.TargetApi;
import android.os.Build;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class RssReader {

    public static RssFeed read(URL url) throws SAXException, IOException {
        return read(url.openStream());

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static RssFeed read(InputStream stream) throws SAXException, IOException {

        try {
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream, Charset.forName("cp1251")));
            RssHandler handler = new RssHandler();
            InputSource is = new InputSource(in);
            parser.parse(is, handler);

            return handler.getResult();

        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }

    }

    public static RssFeed read(String source) throws SAXException, IOException {
        return read(new ByteArrayInputStream(source.getBytes()));
    }

}