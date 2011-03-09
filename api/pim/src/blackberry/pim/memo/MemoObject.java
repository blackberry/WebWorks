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
package blackberry.pim.memo;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryMemo;
import net.rim.blackberry.api.pdap.BlackBerryMemoList;
import net.rim.blackberry.api.pdap.BlackBerryPIM;
import blackberry.core.ScriptField;
import blackberry.core.ScriptableFunctionBase;
import blackberry.core.ScriptableObjectBase;
import blackberry.identity.service.ServiceObject;
import blackberry.pim.category.CategoryNamespace;

/**
 * This class represents a Memo
 * 
 * @author dmateescu
 * 
 */
public class MemoObject extends ScriptableObjectBase {
    private BlackBerryMemo _memo;
    private String _serviceName;

    private MemoSaveScriptableFunction _save;
    private MemoRemoveScriptableFunction _remove;

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_NOTE = "note";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_CATEGORIES = "categories";

    public static final String METHOD_SAVE = "save";
    public static final String METHOD_REMOVE = "remove";

    /**
     * Default constructor of a Memo object
     */
    public MemoObject() {
        super();
        _memo = null;
        _serviceName = "";
        initial();
    }

    /**
     * Constructor of a Memo object based on a given a BlackBerryMemo object
     * 
     * @param m
     *            the BlackBerryMemo
     */
    public MemoObject( BlackBerryMemo m ) {
        super();
        _memo = m;
        _serviceName = "";
        initial();
    }

    /**
     * Constructor of a Memo object based on a given a ServiceObject
     * 
     * @param s
     *            the ServiceObject
     */
    public MemoObject( ServiceObject s ) {
        super();
        _memo = null;
        _serviceName = s.getName();
        initial();
    }

    /**
     * Constructor of a Memo object based on a given a BlackBerryMemo object and a ServiceObject
     * 
     * @param m
     *            the BlackBerryMemo object
     * @param s
     *            the ServiceObject
     */
    public MemoObject( BlackBerryMemo m, ServiceObject s ) {
        super();
        _memo = m;
        _serviceName = s.getName();
        initial();
    }

    /**
     * This class represents the implementation of the save function of a Memo object
     * 
     */
    public class MemoSaveScriptableFunction extends ScriptableFunctionBase {
        private final MemoObject _outer;

        /**
         * Default constructor of MemoSaveScriptableFunction
         */
        public MemoSaveScriptableFunction() {
            super();
            _outer = MemoObject.this;
        }

        /**
         * This method updates the memo field of a MemoObject when the memo is saved
         * 
         * @throws Exception
         */
        public void update() throws Exception {
            // open the handheld memos database for save
            BlackBerryMemoList memoList;
            if( _serviceName.length() == 0 ) {
                memoList = (BlackBerryMemoList) BlackBerryPIM.getInstance().openPIMList( BlackBerryPIM.MEMO_LIST, PIM.READ_WRITE );
            } else {
                memoList = (BlackBerryMemoList) BlackBerryPIM.getInstance().openPIMList( BlackBerryPIM.MEMO_LIST, PIM.READ_WRITE,
                        _serviceName );
            }

            if( _memo == null ) {
                _memo = (BlackBerryMemo) memoList.createMemo();
            }

            // title
            String value;
            value = _outer.getItem( MemoObject.FIELD_TITLE ).getStringValue();
            if( _memo.countValues( BlackBerryMemo.TITLE ) == 0 ) {
                if( value.length() > 0 )
                    _memo.addString( BlackBerryMemo.TITLE, PIMItem.ATTR_NONE, value );
            } else {
                if( value.length() > 0 )
                    _memo.setString( BlackBerryMemo.TITLE, 0, PIMItem.ATTR_NONE, value );
                else
                    _memo.removeValue( BlackBerryMemo.TITLE, 0 );
            }

            // note
            value = _outer.getItem( MemoObject.FIELD_NOTE ).getStringValue();
            if( _memo.countValues( BlackBerryMemo.NOTE ) == 0 ) {
                if( value.length() > 0 )
                    _memo.addString( BlackBerryMemo.NOTE, PIMItem.ATTR_NONE, value );
            } else {
                if( value.length() > 0 )
                    _memo.setString( BlackBerryMemo.NOTE, 0, PIMItem.ATTR_NONE, value );
                else
                    _memo.removeValue( BlackBerryMemo.NOTE, 0 );
            }

            // categories
            CategoryNamespace.updateCategories( _memo, memoList, _outer );
        }

        /**
         * Commits the save
         * 
         * @throws Exception
         */
        public void commit() throws Exception {
            if( _memo == null ) {
                return;
            }

            // commit the memo
            _memo.commit();

            // uid
            final String uid = _memo.getString( BlackBerryMemo.UID, 0 );
            _outer.getItem( MemoObject.FIELD_UID ).setValue( uid );
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {
            update();
            commit();
            return UNDEFINED;
        }
    }

    /**
     * This class represents the implementation of the remove function of a Memo object
     */
    public class MemoRemoveScriptableFunction extends ScriptableFunctionBase {

        /**
         * Default constructor of MemoRemoveScriptableFunction
         */
        public MemoRemoveScriptableFunction() {
            super();
        }

        /**
         * @see blackberry.core.ScriptableFunctionBase#execute(java.lang.Object, java.lang.Object[])
         */
        public Object execute( Object innerThiz, Object[] innerArgs ) throws Exception {

            if( _memo == null ) {
                throw new PIMException( "PIMItem not found." );
            }

            // open the handheld memos database for remove
            BlackBerryMemoList memoList;
            if( _serviceName.length() == 0 ) {
                memoList = (BlackBerryMemoList) BlackBerryPIM.getInstance().openPIMList( BlackBerryPIM.MEMO_LIST, PIM.WRITE_ONLY );
            } else {
                memoList = (BlackBerryMemoList) BlackBerryPIM.getInstance().openPIMList( BlackBerryPIM.MEMO_LIST, PIM.WRITE_ONLY,
                        _serviceName );
            }

            memoList.removeMemo( _memo );
            _memo = null;

            return UNDEFINED;
        }
    }

    private void initial() {
        if( _memo != null ) {
            // title
            if( _memo.countValues( BlackBerryMemo.TITLE ) > 0 )
                addItem( new ScriptField( FIELD_TITLE, _memo.getString( BlackBerryMemo.TITLE, 0 ), ScriptField.TYPE_STRING,
                        false, false ) );
            else
                addItem( new ScriptField( FIELD_TITLE, "", ScriptField.TYPE_STRING, false, false ) );

            // note
            if( _memo.countValues( BlackBerryMemo.NOTE ) > 0 )
                addItem( new ScriptField( FIELD_NOTE, _memo.getString( BlackBerryMemo.NOTE, 0 ), ScriptField.TYPE_STRING, false,
                        false ) );
            else
                addItem( new ScriptField( FIELD_NOTE, "", ScriptField.TYPE_STRING, false, false ) );

            // uid
            if( _memo.countValues( BlackBerryMemo.UID ) > 0 )
                addItem( new ScriptField( FIELD_UID, _memo.getString( BlackBerryMemo.UID, 0 ), ScriptField.TYPE_STRING, true,
                        false ) );
            else
                addItem( new ScriptField( FIELD_UID, "", ScriptField.TYPE_STRING, true, false ) );

            // categories
            addItem( new ScriptField( FIELD_CATEGORIES, _memo.getCategories(), ScriptField.TYPE_SCRIPTABLE, false, false ) );
        } else {
            addItem( new ScriptField( FIELD_TITLE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_NOTE, "", ScriptField.TYPE_STRING, false, false ) );
            addItem( new ScriptField( FIELD_UID, "", ScriptField.TYPE_STRING, true, false ) );
            addItem( new ScriptField( FIELD_CATEGORIES, null, ScriptField.TYPE_SCRIPTABLE, false, false ) );
        }

        _save = new MemoSaveScriptableFunction();
        _remove = new MemoRemoveScriptableFunction();

        addItem( new ScriptField( METHOD_SAVE, _save, ScriptField.TYPE_SCRIPTABLE, true, true ) );
        addItem( new ScriptField( METHOD_REMOVE, _remove, ScriptField.TYPE_SCRIPTABLE, true, true ) );
    }

    /**
     * @see blackberry.core.ScriptableObjectBase#verify(blackberry.core.ScriptField, java.lang.Object)
     */
    protected boolean verify( ScriptField field, Object newValue ) throws Exception {
        return true;
    }

    /**
     * Returns the memo field of a Memo object
     * 
     * @return the memo field
     */
    public BlackBerryMemo getMemo() {
        return _memo;
    }

    /**
     * Returns the categories of a Memo object
     * 
     * @return the categories
     * @throws Exception
     *             when the categories cannot be obtained from a ScriptField
     */
    public String[] getCategories() throws Exception {
        return CategoryNamespace.getCategoriesFromScriptField( getItem( FIELD_CATEGORIES ) );
    }

    /**
     * This method updates the Memo object
     * 
     * @throws Exception
     */
    public void update() throws Exception {
        _save.update();
    }

    /**
     * This method saves the Memo object
     * 
     * @throws Exception
     */
    public void save() throws Exception {
        _save.execute( null, null );
    }

}
