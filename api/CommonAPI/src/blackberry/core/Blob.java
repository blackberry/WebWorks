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
package blackberry.core;

/**
 * Blob interface - replacing net.rim.device.api.script.Blob
 * The standard Blob interface is not publicly open, so we're using this as a placeholder
 * 
 */
public interface Blob {
    
    /**
     * Extracts a subset of the current Blob and returns it as a new Blob.
     * 
     * @param offset
     *            the position of the first byte to extract.
     * @param length
     *            the number of bytes to extract.
     * @return a new Blob containing the specified subset.
     * @throws IllegalArgumentException
     *             if the offset + length is greater than the length of the blob.
     */
    public Blob slice( int offset, int length );

    /**
     * Retrieves the length of the blob in bytes
     *
     * @param offset
     *           the blob index to begin slicing bytes from
     *
     * @param length
     *           the number of bytes to slice
     * 
     * @return the length of the blob in bytes
     */
    public int size();

    /**
     * Returns the contents of the blob in a byte array
     * 
     * @return the contents of the blog as a byte array
     */
    public byte[] getBytes();
}
