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
package blackberry.media;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;

/**
 * Listens to the file system for changes
 */
public abstract class JournalListener implements FileSystemJournalListener {
    private long _lastUSN;

    /**
     * Default constructor
     */
    public JournalListener() {
        // Since we want to preserve the order, iterate through the entries from the next
        // currently available USN to the one at capture
        _lastUSN = FileSystemJournal.getNextUSN();
    }

    protected abstract boolean pathMatches( String path );

    protected void onFileAdded( String path ) {
    }

    protected void onFileRemoved( String path ) {
    }

    protected void onFileChanged( String path ) {
    }

    protected void onFileRenamed( String oldPath, String newPath ) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.rim.device.api.io.file.FileSystemJournalListener#fileJournalChanged()
     */
    public void fileJournalChanged() {
        // next sequence number file system will use
        long nextUSN = FileSystemJournal.getNextUSN();

        for( long currentUSN = _lastUSN; currentUSN < nextUSN; currentUSN++ ) {
            FileSystemJournalEntry entry = FileSystemJournal.getEntry( currentUSN );
            if( entry == null ) {
                continue; // Journal entry no longer available. Skip it.
            }

            String path = entry.getPath();

            if( path != null ) {
                boolean matches = pathMatches( path );
                if( matches ) {
                    switch( entry.getEvent() ) {
                        case FileSystemJournalEntry.FILE_ADDED: {
                            onFileAdded( path );
                            break;
                        }
                        case FileSystemJournalEntry.FILE_DELETED: {
                            onFileRemoved( path );
                            break;
                        }
                        case FileSystemJournalEntry.FILE_CHANGED: {
                            onFileChanged( path );
                            break;
                        }
                        case FileSystemJournalEntry.FILE_RENAMED: {
                            onFileRenamed( entry.getOldPath(), path );
                            break;
                        }
                    } // switch(entry.getEvent()
                } // if (matches)
            } // if (path != null)

        }

        // Mark the last entry, and next time continue from there.
        _lastUSN = nextUSN;
    }
}
