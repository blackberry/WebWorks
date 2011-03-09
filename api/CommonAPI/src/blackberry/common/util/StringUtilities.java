/*
* Copyright 2010-2011 Research In Motion Limited.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package blackberry.common.util;

/**
 * @author yiwu
 * 
 * A utility class for handling string.
 */
public class StringUtilities {

    /**
     * Checks if the given string is <code>null</code> or empty.
     * 
     * @param s The string to check
     * @return <code>true</code> if yes; otherwise returns <code>false</code>
     */
    public static boolean isNullOrEmpty( String s ) {
        return ( s == null ) || ( s.length() == 0 );
    }

    /**
     * Splits the given string that are delimited by the given delimiter into an string array.
     * 
     * @param strString The string to be split 
     * @param strDelimiter The string delimiter
     * @return The string array The split array 
     */
    public static String[] split( String strString, String strDelimiter ) {
        String[] strArray;
        int iOccurrences = 0;
        int iIndexOfInnerString = 0;
        int iIndexOfDelimiter = 0;
        int iCounter = 0;

        // Check for null input strings.
        if( strString == null ) {
            throw new IllegalArgumentException( "Input string cannot be null." );
        }
        // Check for null or empty delimiter strings.
        if( strDelimiter == null || strDelimiter.length() == 0 ) {
            throw new IllegalArgumentException( "Delimeter cannot be null or empty." );
        }

        // strString must be in this format: (without {} )
        // "{str[0]}{delimiter}str[1]}{delimiter} ...
        // {str[n-1]}{delimiter}{str[n]}{delimiter}"

        // If strString begins with delimiter then remove it in order
        // to comply with the desired format.

        if( strString.startsWith( strDelimiter ) ) {
            strString = strString.substring( strDelimiter.length() );
        }

        // If strString does not end with the delimiter then add it
        // to the string in order to comply with the desired format.
        if( !strString.endsWith( strDelimiter ) ) {
            strString += strDelimiter;
        }

        // Count occurrences of the delimiter in the string.
        // Occurrences should be the same amount of inner strings.
        while( ( iIndexOfDelimiter = strString.indexOf( strDelimiter, iIndexOfInnerString ) ) != -1 ) {
            iOccurrences += 1;
            iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
        }

        // Declare the array with the correct size.
        strArray = new String[ iOccurrences ];

        // Reset the indices.
        iIndexOfInnerString = 0;
        iIndexOfDelimiter = 0;

        // Walk across the string again and this time add the
        // strings to the array.
        while( ( iIndexOfDelimiter = strString.indexOf( strDelimiter, iIndexOfInnerString ) ) != -1 ) {

            // Add string to array.
            strArray[ iCounter ] = strString.substring( iIndexOfInnerString, iIndexOfDelimiter );

            // Increment the index to the next character after
            // the next delimiter.
            iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();

            // Inc the counter.
            iCounter += 1;
        }

        return strArray;
    }
}
