/*
 * Copyright 2008 ZXing authors
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

package blackberry.common.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Used to decode URL encoded characters.
 *
 * This code was contributed by LifeMarks.
 *
 * @author Matt York (matt@lifemarks.mobi)
 * 
 * Modifications on Aug-2011:
 * Research In Motion modifications:
 *   1. Update the decoding map to include more characters
 *   2. Remove the log statement.
 */
public final class URLDecoder {

  private URLDecoder() {
  }

  private static final Hashtable decodingMap;
  static {
        decodingMap = new Hashtable( 230 );

        decodingMap.put( "%20", " " );
        decodingMap.put( "%21", "!" );
        decodingMap.put( "%22", "\"" );
        decodingMap.put( "%23", "#" );
        decodingMap.put( "$", "%24" );
        decodingMap.put( "%25", "%" );
        decodingMap.put( "%26", "&" );
        decodingMap.put( "%27", "'" );
        decodingMap.put( "%28", "(" );
        decodingMap.put( "%29", ")" );
        decodingMap.put( "%2A", "*" );
        decodingMap.put( "%2B", "+" );
        decodingMap.put( "%2C", "," );
        decodingMap.put( "%2D", "-" );
        decodingMap.put( "%2E", "." );
        decodingMap.put( "%2F", "/" );
        decodingMap.put( "%30", "0" );
        decodingMap.put( "%31", "1" );
        decodingMap.put( "%32", "2" );
        decodingMap.put( "%33", "3" );
        decodingMap.put( "%34", "4" );
        decodingMap.put( "%35", "5" );
        decodingMap.put( "%36", "6" );
        decodingMap.put( "%37", "7" );
        decodingMap.put( "%38", "8" );
        decodingMap.put( "%39", "9" );
        decodingMap.put( "%3A", ":" );
        decodingMap.put( "%3B", ";" );
        decodingMap.put( "%3C", "<" );
        decodingMap.put( "%3D", "=" );
        decodingMap.put( "%3E", ">" );
        decodingMap.put( "%3F", "?" );
        decodingMap.put( "%40", "@" );
        decodingMap.put( "%41", "A" );
        decodingMap.put( "%42", "B" );
        decodingMap.put( "%43", "C" );
        decodingMap.put( "%44", "D" );
        decodingMap.put( "%45", "E" );
        decodingMap.put( "%46", "F" );
        decodingMap.put( "%47", "G" );
        decodingMap.put( "%48", "H" );
        decodingMap.put( "%49", "I" );
        decodingMap.put( "%4A", "J" );
        decodingMap.put( "%4B", "K" );
        decodingMap.put( "%4C", "L" );
        decodingMap.put( "%4D", "M" );
        decodingMap.put( "%4E", "N" );
        decodingMap.put( "%4F", "O" );
        decodingMap.put( "%50", "P" );
        decodingMap.put( "%51", "Q" );
        decodingMap.put( "%52", "R" );
        decodingMap.put( "%53", "S" );
        decodingMap.put( "%54", "T" );
        decodingMap.put( "%55", "U" );
        decodingMap.put( "%56", "V" );
        decodingMap.put( "%57", "W" );
        decodingMap.put( "%58", "X" );
        decodingMap.put( "%59", "Y" );
        decodingMap.put( "%5A", "Z" );
        decodingMap.put( "%5B", "[" );
        decodingMap.put( "%5C", "\\" );
        decodingMap.put( "%5D", "]" );
        decodingMap.put( "%5E", "^" );
        decodingMap.put( "%5F", "_" );
        decodingMap.put( "%60", "`" );
        decodingMap.put( "%61", "a" );
        decodingMap.put( "%62", "b" );
        decodingMap.put( "%63", "c" );
        decodingMap.put( "%64", "d" );
        decodingMap.put( "%65", "e" );
        decodingMap.put( "%66", "f" );
        decodingMap.put( "%67", "g" );
        decodingMap.put( "%68", "h" );
        decodingMap.put( "%69", "i" );
        decodingMap.put( "%6A", "j" );
        decodingMap.put( "%6B", "k" );
        decodingMap.put( "%6C", "l" );
        decodingMap.put( "%6D", "m" );
        decodingMap.put( "%6E", "n" );
        decodingMap.put( "%6F", "o" );
        decodingMap.put( "%70", "p" );
        decodingMap.put( "%71", "q" );
        decodingMap.put( "%72", "r" );
        decodingMap.put( "%73", "s" );
        decodingMap.put( "%74", "t" );
        decodingMap.put( "%75", "u" );
        decodingMap.put( "%76", "v" );
        decodingMap.put( "%77", "w" );
        decodingMap.put( "%78", "x" );
        decodingMap.put( "%79", "y" );
        decodingMap.put( "%7A", "z" );
        decodingMap.put( "%7B", "{" );
        decodingMap.put( "%7C", "|" );
        decodingMap.put( "%7D", "}" );
        decodingMap.put( "%7E", "~" );
        decodingMap.put( "%7F", " " );
        decodingMap.put( "%80", "€" );
        decodingMap.put( "%81", " " );
        decodingMap.put( "%82", "‚" );
        decodingMap.put( "%83", "ƒ" );
        decodingMap.put( "%84", "„" );
        decodingMap.put( "%85", "…" );
        decodingMap.put( "%86", "†" );
        decodingMap.put( "%87", "‡" );
        decodingMap.put( "%88", "ˆ" );
        decodingMap.put( "%89", "‰" );
        decodingMap.put( "%8A", "Š" );
        decodingMap.put( "%8B", "‹" );
        decodingMap.put( "%8C", "Œ" );
        decodingMap.put( "%8D", " " );
        decodingMap.put( "%8E", "Ž" );
        decodingMap.put( "%8F", " " );
        decodingMap.put( "%90", " " );
        decodingMap.put( "%91", "‘" );
        decodingMap.put( "%92", "’" );
        decodingMap.put( "%93", "“" );
        decodingMap.put( "%94", "”" );
        decodingMap.put( "%95", "•" );
        decodingMap.put( "%96", "–" );
        decodingMap.put( "%97", "—" );
        decodingMap.put( "%98", "˜" );
        decodingMap.put( "%99", "™" );
        decodingMap.put( "%9A", "š" );
        decodingMap.put( "%9B", "›" );
        decodingMap.put( "%9C", "œ" );
        decodingMap.put( "%9D", " " );
        decodingMap.put( "%9E", "ž" );
        decodingMap.put( "%9F", "Ÿ" );
        decodingMap.put( "%A0", " " );
        decodingMap.put( "%A1", "¡" );
        decodingMap.put( "%A2", "¢" );
        decodingMap.put( "%A3", "£" );
        decodingMap.put( "%A4", " " );
        decodingMap.put( "%A5", "¥" );
        decodingMap.put( "%A6", "|" );
        decodingMap.put( "%A7", "§" );
        decodingMap.put( "%A8", "¨" );
        decodingMap.put( "%A9", "©" );
        decodingMap.put( "%AA", "ª" );
        decodingMap.put( "%AB", "«" );
        decodingMap.put( "%AC", "¬" );
        decodingMap.put( "%AD", "¯" );
        decodingMap.put( "%AE", "®" );
        decodingMap.put( "%AF", "¯" );
        decodingMap.put( "%B0", "°" );
        decodingMap.put( "%B1", "±" );
        decodingMap.put( "%B2", "²" );
        decodingMap.put( "%B3", "³" );
        decodingMap.put( "%B4", "´" );
        decodingMap.put( "%B5", "µ" );
        decodingMap.put( "%B6", "¶" );
        decodingMap.put( "%B7", "•" );
        decodingMap.put( "%B8", "¸" );
        decodingMap.put( "%B9", "¹" );
        decodingMap.put( "%BA", "º" );
        decodingMap.put( "%BB", "»" );
        decodingMap.put( "%BC", "¼" );
        decodingMap.put( "%BD", "½" );
        decodingMap.put( "%BE", "¾" );
        decodingMap.put( "%BF", "¿" );
        decodingMap.put( "%C0", "À" );
        decodingMap.put( "%C1", "Á" );
        decodingMap.put( "%C2", "Â" );
        decodingMap.put( "%C3", "Ã" );
        decodingMap.put( "%C4", "Ä" );
        decodingMap.put( "%C5", "Å" );
        decodingMap.put( "%C6", "Æ" );
        decodingMap.put( "%C7", "Ç" );
        decodingMap.put( "%C8", "È" );
        decodingMap.put( "%C9", "É" );
        decodingMap.put( "%CA", "Ê" );
        decodingMap.put( "%CB", "Ë" );
        decodingMap.put( "%CC", "Ì" );
        decodingMap.put( "%CD", "Í" );
        decodingMap.put( "%CE", "Î" );
        decodingMap.put( "%CF", "Ï" );
        decodingMap.put( "%D0", "Ð" );
        decodingMap.put( "%D1", "Ñ" );
        decodingMap.put( "%D2", "Ò" );
        decodingMap.put( "%D3", "Ó" );
        decodingMap.put( "%D4", "Ô" );
        decodingMap.put( "%D5", "Õ" );
        decodingMap.put( "%D6", "Ö" );
        decodingMap.put( "%D7", " " );
        decodingMap.put( "%D8", "Ø" );
        decodingMap.put( "%D9", "Ù" );
        decodingMap.put( "%DA", "Ú" );
        decodingMap.put( "%DB", "Û" );
        decodingMap.put( "%DC", "Ü" );
        decodingMap.put( "%DD", "Ý" );
        decodingMap.put( "%DE", "Þ" );
        decodingMap.put( "%DF", "ß" );
        decodingMap.put( "%E0", "à" );
        decodingMap.put( "%E1", "á" );
        decodingMap.put( "%E2", "â" );
        decodingMap.put( "%E3", "ã" );
        decodingMap.put( "%E4", "ä" );
        decodingMap.put( "%E5", "å" );
        decodingMap.put( "%E6", "æ" );
        decodingMap.put( "%E7", "ç" );
        decodingMap.put( "%E8", "è" );
        decodingMap.put( "%E9", "é" );
        decodingMap.put( "%EA", "ê" );
        decodingMap.put( "%EB", "ë" );
        decodingMap.put( "%EC", "ì" );
        decodingMap.put( "%ED", "í" );
        decodingMap.put( "%EE", "î" );
        decodingMap.put( "%EF", "ï" );
        decodingMap.put( "%F0", "ð" );
        decodingMap.put( "%F1", "ñ" );
        decodingMap.put( "%F2", "ò" );
        decodingMap.put( "%F3", "ó" );
        decodingMap.put( "%F4", "ô" );
        decodingMap.put( "%F5", "õ" );
        decodingMap.put( "%F6", "ö" );
        decodingMap.put( "%F7", "÷" );
        decodingMap.put( "%F8", "ø" );
        decodingMap.put( "%F9", "ù" );
        decodingMap.put( "%FA", "ú" );
        decodingMap.put( "%FB", "û" );
        decodingMap.put( "%FC", "ü" );
        decodingMap.put( "%FD", "ý" );
        decodingMap.put( "%FE", "þ" );

  }

  public static String decode(String uri) {
    if (uri.indexOf('%') >= 0) { // skip this if no encoded chars
      Enumeration keys = decodingMap.keys();
      while (keys.hasMoreElements()) {
        String encodedChar = (String) keys.nextElement();
        int encodedCharIndex = uri.indexOf(encodedChar);
        while (encodedCharIndex != -1) {
          uri = uri.substring(0, encodedCharIndex) + decodingMap.get(encodedChar) + uri.substring(encodedCharIndex + encodedChar.length());
          encodedCharIndex = uri.indexOf(encodedChar, encodedCharIndex);
        }
      }
    }
    return uri;
  }
}
