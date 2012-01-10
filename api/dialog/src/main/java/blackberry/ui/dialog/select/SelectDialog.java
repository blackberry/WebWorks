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
package blackberry.ui.dialog.select;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import blackberry.ui.dialog.SelectAsyncFunction;
import blackberry.ui.dialog.IWebWorksDialog;

/**
 * Implementation of selection dialog
 * 
 * @author jachoi
 * 
 */
public class SelectDialog extends PopupScreen implements FieldChangeListener, IWebWorksDialog {

    private ButtonField _doneButton;
    private VerticalFieldManager _vfm;
    private SelectListField _list;

    private ListItem[] _listItems;
    private Vector _response;

    private int _choiceLength;
    private boolean _allowMultiple;
    private int _selectedIndex;
    
    private boolean _dialogAccepted;

    public SelectDialog( boolean allowMultiple, String[] labels, boolean[] enableds, boolean[] selecteds, int[] types ) {
        super( new VerticalFieldManager( VERTICAL_SCROLL | VERTICAL_SCROLLBAR ) );
        _choiceLength = labels.length;
        _allowMultiple = allowMultiple;
        _response = new Vector();
        _selectedIndex = -1;
        _dialogAccepted = false;

        _listItems = new ListItem[ _choiceLength ];
        int indexAssignment = 0;
        for( int index = 0; index < _choiceLength; index++ ) {
            if( _selectedIndex == -1 && selecteds[ index ] && enableds[ index ] ) {
                _selectedIndex = index;
            }
            if( types[ index ] == SelectAsyncFunction.POPUP_ITEM_TYPE_OPTION ) {
                _listItems[ index ] = new ListItem( labels[ index ], enableds[ index ], selecteds[ index ], types[ index ],
                        indexAssignment );
                indexAssignment++;
            } else {
                _listItems[ index ] = new ListItem( labels[ index ], enableds[ index ], selecteds[ index ], types[ index ], -1 );
            }
        }

        _list = new SelectListField();
        _list.setChangeListener( this );
        _vfm = new VerticalFieldManager( NO_HORIZONTAL_SCROLL | NO_HORIZONTAL_SCROLLBAR | VERTICAL_SCROLL | VERTICAL_SCROLLBAR );
        _vfm.add( _list );
        add( _vfm );

        if( _allowMultiple ) {
            _doneButton = new ButtonField( "Done", Field.FIELD_HCENTER );
            _doneButton.setChangeListener( this );

            add( new SeparatorField() );
            add( _doneButton );
        }
    }
    
    public boolean show() {
        UiApplication.getUiApplication().pushModalScreen( this );
        return _dialogAccepted;
    }
    
    private void close(boolean changeAccepted) {
        _dialogAccepted = changeAccepted;
        super.close();
    }

    public Object getSelectedValue() {
        return _response;
    }

    public void fieldChanged( Field field, int arg1 ) {
        for( int index = 0; index < _listItems.length; index++ ) {
            if( _listItems[ index ].isSelected() ) {
                _response.addElement( new Integer( _listItems[ index ].getIndex() ) );
            }
        }

        close( true );
    }

    private void updateCurrentSelection( char keyChar ) {
        // Ensure we were passed a valid key to locate.
        if( keyChar == '\u0000' ) {
            return;
        } else {
            int lastFieldIndex = _choiceLength - 1;
            int indexWithFocus = _list.getSelectedIndex();

            for( int i = indexWithFocus == lastFieldIndex ? 0 : indexWithFocus + 1; i != indexWithFocus; i++ ) {
                String label = _listItems[ i ].toString();
                if( label.indexOf( keyChar ) == 0 || label.substring( 0, 1 ).toLowerCase().indexOf( keyChar ) == 0 ) {
                    _list.setSelectedIndex( i );
                    break;
                }
                // Wrap around.
                if( i == _choiceLength - 1 ) {
                    i = -1;
                }
            }
        }
    }

    /* @Override */
    protected boolean touchEvent( TouchEvent message ) {
        switch( message.getEvent() ) {
            case TouchEvent.GESTURE:
                if( _allowMultiple && message.getGesture().getEvent() == TouchGesture.NAVIGATION_SWIPE ) {
                    int swipeDirection = message.getGesture().getSwipeDirection();
                    Field field = getLeafFieldWithFocus();
                    if( field instanceof ListField ) {
                        switch( swipeDirection ) {
                            case TouchGesture.SWIPE_EAST:
                                _doneButton.setFocus();
                                return true;
                        }
                    } else if( field instanceof ButtonField ) {
                        switch( swipeDirection ) {
                            case TouchGesture.SWIPE_NORTH:
                            case TouchGesture.SWIPE_WEST:
                                _list.setFocus();
                                _list.setSelectedIndex( _list._previousSelected );
                                return true;
                        }
                    }
                }
        }
        return super.touchEvent( message );
    }

    /* @Override */
    protected boolean keyChar( char c, int status, int time ) {
        switch( c ) {
            case Characters.ENTER:
                Field field = getLeafFieldWithFocus();
                if( field == _doneButton ) {
                    fieldChanged( _doneButton, -1 );
                } else {
                    _list.invokeAction( Field.ACTION_INVOKE );
                }
                return true;
            case Characters.ESCAPE:
                close(false);
                return true;
            default:
                updateCurrentSelection( c );
                break;
        }
        return super.keyChar( c, status, time );
    }

    private final class SelectListField extends ListField implements ListFieldCallback {
        private static final int PADDING = 10;
        public int _previousSelected = -1;

        SelectListField() {
            setCallback( this );
            setSize( _choiceLength );
            setSelectedIndex( _selectedIndex );
        }

        protected void onUnfocus() {
            _previousSelected = this.getSelectedIndex();
            super.onUnfocus();
        }

        protected boolean invokeAction( int action ) {
            if( action == Field.ACTION_INVOKE ) {
                int selectedIndex = getSelectedIndex();
                ListItem listItem = (ListItem) get( this, selectedIndex );

                if( !listItem.isEnabled() || listItem.getType() == SelectAsyncFunction.POPUP_ITEM_TYPE_GROUP
                        || listItem.getType() == SelectAsyncFunction.POPUP_ITEM_TYPE_SEPARATOR ) {
                    return true;
                }

                if( !_allowMultiple ) {
                    if( _selectedIndex != -1 ) {
                        _listItems[ _selectedIndex ].setSelected( false );
                    }
                    listItem.setSelected( true );
                    _selectedIndex = selectedIndex;
                    fieldChanged( null, -1 );
                } else {
                    listItem.setSelected( !listItem.isSelected() );
                    invalidate();
                }
                return true;
            }
            return false;
        }

        public void drawListRow( ListField listField, Graphics graphics, int index, int y, int w ) {
            Object obj = get( listField, index );
            if( obj instanceof ListItem ) {
                paintListItem( (ListItem) obj, listField, graphics, index, y, w );
            }
        }

        private void paintListItem( ListItem listItem, ListField listField, Graphics graphics, int index, int y, int width ) {
            String text = listItem.toString().trim();

            int type = listItem.getType();
            Font font = graphics.getFont();
            final int checkboxSize = font.getHeight() - 6;
            final int textStart = PADDING + checkboxSize + 10;

            Bitmap checkWhite = GPATools.ResizeTransparentBitmap( Bitmap.getBitmapResource( "chk-white.png" ), checkboxSize,
                    checkboxSize, Bitmap.FILTER_BOX, Bitmap.SCALE_TO_FILL );
            Bitmap checkBlue = GPATools.ResizeTransparentBitmap( Bitmap.getBitmapResource( "chk-blue.png" ), checkboxSize,
                    checkboxSize, Bitmap.FILTER_BILINEAR, Bitmap.SCALE_TO_FILL );
            Bitmap boxEmpty = GPATools.ResizeTransparentBitmap( Bitmap.getBitmapResource( "box-empty.png" ), checkboxSize,
                    checkboxSize, Bitmap.FILTER_BILINEAR, Bitmap.SCALE_TO_FILL );

            switch( type ) {
                case SelectAsyncFunction.POPUP_ITEM_TYPE_SEPARATOR:
                    graphics.setColor( Color.GRAY );
                    int lineY = y + listField.getRowHeight() / 3;
                    graphics.drawLine( PADDING, lineY, width - PADDING, lineY );
                    break;
                case SelectAsyncFunction.POPUP_ITEM_TYPE_GROUP:
                    graphics.setColor( Color.GRAY );
                    font = font.derive( Font.BOLD );
                    graphics.setFont( font );
                    graphics.drawText( text, PADDING, y, DrawStyle.ELLIPSIS, width - PADDING ); // no fudge added to y coordinate
                    break;
                case SelectAsyncFunction.POPUP_ITEM_TYPE_OPTION:
                    boolean enabled = listItem.isEnabled();
                    if( !enabled ) {
                        graphics.setColor( Color.GRAY );
                    }

                    if( _allowMultiple ) {
                        graphics.drawBitmap( PADDING, y + 3, checkboxSize, checkboxSize, boxEmpty, 0, 0 );
                    }

                    if( listItem.isSelected() ) {
                        if( _allowMultiple ) {
                            graphics.drawBitmap( PADDING, y + 3, checkboxSize, checkboxSize, checkBlue, 0, 0 );
                        } else {
                            graphics.drawBitmap( PADDING, y + 3, checkboxSize, checkboxSize, checkWhite, 0, 0 );
                        }
                    }

                    graphics.drawText( text, textStart, y, DrawStyle.ELLIPSIS, width - textStart ); // no fudge added to y
                                                                                                    // coordinate
                    break;
                default:
            }
        }

        public Object get( ListField list, int index ) {
            return _listItems[ index ];
        }

        public int getPreferredWidth( ListField arg0 ) {
            return Display.getWidth();
        }

        public int indexOfList( ListField arg0, String arg1, int arg2 ) {
            return -1;
        }
    }

    /*
     * Store choice information.
     */
    private static final class ListItem {
        private final String _label;
        private boolean _selected;
        private boolean _enabled;
        private int _type;
        private int _index;

        public ListItem( String label, boolean enabled, boolean selected, int type, int index ) {
            _label = label;
            _selected = selected;
            _enabled = enabled;
            _type = type;
            _index = index;
        }

        /* @Override */
        public String toString() {
            return _label;
        }

        public void setSelected( boolean value ) {
            _selected = value;
        }

        public boolean isSelected() {
            return _selected;
        }

        public boolean isEnabled() {
            return _enabled;
        }

        public int getType() {
            return _type;
        }

        public int getIndex() {
            return _index;
        }
    }
}
