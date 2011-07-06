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
package blackberry.pim.category;

import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

import net.rim.device.api.script.Scriptable;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableObjectBase;
import blackberry.pim.contact.ContactObject;
import blackberry.pim.memo.MemoObject;
import blackberry.pim.task.TaskObject;

/**
 * This class represents a category namespace
 * 
 * @author dmateescu
 * 
 */
public final class CategoryNamespace extends ScriptableObjectBase {
    public static final String NAME = "blackberry.pim.category";

    /**
     * Constructs a CategoryNamespace
     */
    public CategoryNamespace() {
        addItem( new ScriptField( AddCategoryFunction.NAME, new AddCategoryFunction(), ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( DeleteCategoryFunction.NAME, new DeleteCategoryFunction(), ScriptField.TYPE_SCRIPTABLE, true,
                true ) );
        addItem( new ScriptField( GetCategoriesFunction.NAME, new GetCategoriesFunction(), ScriptField.TYPE_SCRIPTABLE, true,
                true ) );
    }

    /**
     * Helper method to get categories given a ScriptField that corresponds to the categories array
     * 
     * @param field
     *            ScriptField
     * @returns categories as a string array
     * @throws Exception
     *             when an exception occurs when getting the field or an element
     */
    public static String[] getCategoriesFromScriptField( ScriptField field ) throws Exception {
        String[] categories = null;

        Object categoriesObject = field.getValue();

        if( categoriesObject instanceof Scriptable ) {
            Scriptable categoriesArray = (Scriptable) categoriesObject;
            int length = ( (Integer) categoriesArray.getField( "length" ) ).intValue();
            categories = new String[ length ];

            for( int i = 0; i < length; i++ ) {
                String category = (String) categoriesArray.getElement( i );
                categories[ i ] = category;
            }
        } else if( categoriesObject instanceof String[] ) {
            categories = (String[]) categoriesObject;
        }
        return categories;
    }

    /**
     * Helper method to update categories field of Contact/Memo/Task on save.
     * 
     * @param item
     *            Internal PIMItem wrapped within the script object
     * @param pimList
     *            ContactList, MemoList or TodoList
     * @param pimObj
     *            The object that is being saved, must be an instance of ContactObject, MemoObject, or TaskObject
     */
    public static void updateCategories( PIMItem item, PIMList pimList, ScriptableObjectBase pimObj ) throws Exception {
        if( pimObj == null || item == null || pimList == null )
            return;

        String[] categories = null;

        if( pimObj instanceof TaskObject ) {
            categories = ( (TaskObject) pimObj ).getCategories();
        } else if( pimObj instanceof ContactObject ) {
            categories = ( (ContactObject) pimObj ).getCategories();
        } else if( pimObj instanceof MemoObject ) {
            categories = ( (MemoObject) pimObj ).getCategories();
        } else {
            // no categories support for other PIM objects
            return;
        }

        // remove old categories
        String[] existingCategories = item.getCategories();
        if( existingCategories != null ) {
            for( int j = 0; j < existingCategories.length; j++ ) {
                item.removeFromCategory( existingCategories[ j ] );
            }
        }

        // categories = pimObj.getCategories();
        if( categories != null ) {
            for( int j = 0; j < categories.length; j++ ) {
                String category = categories[ j ];

                if( category != null && pimList.isCategory( category ) ) {
                    item.addToCategory( category );
                }
            }
        }
    }
}
