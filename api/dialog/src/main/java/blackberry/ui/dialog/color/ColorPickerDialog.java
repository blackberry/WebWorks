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
package blackberry.ui.dialog.color;

import blackberry.ui.dialog.IWebWorksDialog;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.util.MathUtilities;
import net.rim.device.api.util.NumberUtilities;

/**
 * Implementation of color picker dialog
 * 
 * @author jachoi
 * 
 */
public class ColorPickerDialog extends Field implements IWebWorksDialog {
    private int _selectedColor;

    public ColorPickerDialog( int initialColor ) {
        super( Field.FOCUSABLE );
        _selectedColor = initialColor;
        setPadding( 0, 0, 0, 0 );
    }

    public void setColor( int color ) {
        _selectedColor = color;
    }

    public int getPreferredWidth() {
        return (int) Math.ceil( getFont().getHeight() * 1.618 );
    }

    public int getPreferredHeight() {
        return getFont().getHeight();
    }

    protected void layout( int width, int height ) {
        setExtent( getPreferredWidth(), getPreferredHeight() );
    }

    protected void paint( Graphics g ) {
        int oldColor = g.getColor();
        try {
            // Color
            g.setColor( _selectedColor );
            g.fillRect( 0, 0, getWidth(), getHeight() );

            // Border
            g.setStrokeWidth( 1 );
            g.setColor( 0x000000 );
            g.drawRect( 0, 0, getWidth(), getHeight() );
        } finally {
            g.setColor( oldColor );
        }
    }

    protected void drawFocus( Graphics g, boolean on ) {
        int oldColor = g.getColor();
        try {
            g.setColor( 0x0000FF );
            g.drawRect( 0, 0, getWidth(), getHeight() );
            g.drawRect( 1, 1, getWidth() - 2, getHeight() - 2 );
        } finally {
            g.setColor( oldColor );
        }
    }

    private String colorToString( int color ) throws IllegalArgumentException {
        StringBuffer sb = new StringBuffer( 7 );
        sb.append( "#" );
        NumberUtilities.appendNumber( sb, color & 0x00FFFFFF, 16, 6 );
        return sb.toString();
    }

    public boolean show() {
        ColorPickerPopup picker = new ColorPickerPopup( _selectedColor );
        boolean result = picker.doModal();
        if( result ) {
            _selectedColor = picker.getSelectedColor();
            invalidate();
            fieldChangeNotify( 0 );
        }
        return result;
    }

    public Object getSelectedValue() {
        return colorToString( _selectedColor );
    }

    protected void paintBackground( Graphics g ) {
    }

    public void setDirty( boolean dirty ) {
        // We never want to be dirty or muddy
    }

    public void setMuddy( boolean muddy ) {
        // We never want to be dirty or muddy
    }
}

class BaseColorChooser extends Field {
    private static final int FIELD_HEIGHT = 100;
    private static final int FIELD_WIDTH = 40;
    private static final int NUM_COLOURS = 7;
    private static final int FRAME_THICKNESS = 2;
    private static final int FRAME_HEIGHT = 10;

    private int _selectedColor;

    private int[] _xcoords;
    private int[] _ycoords;
    private int[] _colors;
    private Bitmap _rainbow;

    private boolean _editing;
    private int _y;

    BaseColorChooser() {
        _y = FRAME_THICKNESS;
        int yStep = FIELD_HEIGHT / ( NUM_COLOURS - 1 );

        _xcoords = new int[] { 0, 0, 0, 0, 0, 0, 0, FIELD_WIDTH, FIELD_WIDTH, FIELD_WIDTH, FIELD_WIDTH, FIELD_WIDTH, FIELD_WIDTH,
                FIELD_WIDTH };
        _ycoords = new int[] { 0, yStep, 2 * yStep, 3 * yStep, 4 * yStep, 5 * yStep, FIELD_HEIGHT, FIELD_HEIGHT, 5 * yStep,
                4 * yStep, 3 * yStep, 2 * yStep, yStep, 0 };
        _colors = new int[] { 0xff0000, 0xff00ff, 0x0000ff, 0x00ffff, 0x00ff00, 0xffff00, 0xff0000, 0xff0000, 0xffff00, 0x00ff00,
                0x00ffff, 0x0000ff, 0xff00ff, 0xff0000 };
    }

    public int getPreferredWidth() {
        return FIELD_WIDTH;
    }

    public int getPreferredHeight() {
        return FIELD_HEIGHT;
    }

    public int getSelectedColor() {
        return _selectedColor;
    }

    protected void layout( int width, int height ) {
        width = getPreferredWidth();
        height = getPreferredHeight();

        _rainbow = new Bitmap( width, height );
        Graphics rainbowGraphics = Graphics.create( _rainbow );
        rainbowGraphics.drawShadedFilledPath( _xcoords, _ycoords, null, _colors, null );

        setExtent( width, height );
    }

    protected void paint( Graphics g ) {
        int oldColor = g.getColor();
        try {
            // Rainbow
            g.drawBitmap( 0, 0, _rainbow.getWidth(), _rainbow.getHeight(), _rainbow, 0, 0 );

            // Border
            g.setColor( 0x000000 );
            g.drawRect( 0, 0, FIELD_WIDTH, FIELD_HEIGHT );
            if( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ) {
                g.setColor( Color.WHITE );
                g.drawRect( 1, 1, FIELD_WIDTH - 2, FIELD_HEIGHT - 2 );
            }

            int frameY = _y - ( FRAME_HEIGHT >> 1 );

            // draw the selector shadow
            frameY++;
            g.setColor( 0x000000 );
            g.fillRect( 0, frameY, FIELD_WIDTH, FRAME_THICKNESS ); // top
            g.fillRect( 0, frameY + FRAME_HEIGHT - FRAME_THICKNESS, FIELD_WIDTH, FRAME_THICKNESS ); // bottom
            frameY--;

            // draw selector foreground
            g.setColor( Color.WHITE );
            g.fillRect( 0, frameY, FIELD_WIDTH, FRAME_THICKNESS ); // top
            g.fillRect( 0, frameY + FRAME_HEIGHT - FRAME_THICKNESS, FIELD_WIDTH, FRAME_THICKNESS ); // bottom
            g.fillRect( 0, frameY, FRAME_THICKNESS, FRAME_HEIGHT ); // left
            g.fillRect( FIELD_WIDTH - FRAME_THICKNESS, frameY, FRAME_THICKNESS, FRAME_HEIGHT ); // right
        } finally {
            g.setColor( oldColor );
        }
    }

    protected void paintBackground( Graphics g ) {
    }

    protected void drawFocus( Graphics g, boolean on ) {
        boolean oldDrawStyleFocus = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS );
        try {
            g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, true );
            paint( g );
        } finally {
            g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, oldDrawStyleFocus );
        }
    }

    public boolean isFocusable() {
        return true;
    }

    protected boolean keyChar( char key, int status, int time ) {
        if( key == Characters.ESCAPE ) {
            if( _editing ) {
                return handleClick();
            }
        } else if( key == Characters.ENTER ) {
            return handleClick();
        }
        return super.keyChar( key, status, time );
    }

    protected boolean navigationMovement( int dx, int dy, int status, int time ) {
        return handleMovement( dx, dy );
    }

    protected boolean trackwheelRoll( int amount, int status, int time ) {
        if( ( status & KeypadListener.STATUS_ALT ) != 0 ) {
            return handleMovement( amount, 0 );
        } else {
            return handleMovement( 0, amount );
        }
    }

    private boolean handleMovement( int dx, int dy ) {
        if( _editing ) {
            int yMovement = dy * FIELD_HEIGHT / 20;

            _y = MathUtilities.clamp( 0, _y + yMovement, FIELD_HEIGHT - 1 );

            int[] argbData = new int[ 1 ];
            _rainbow.getARGB( argbData, 0, 1, 0, _y, 1, 1 );
            _selectedColor = argbData[ 0 ];

            invalidate( 0, 0, getWidth(), getHeight() );
            fieldChangeNotify( FieldChangeListener.PROGRAMMATIC );
            return true;
        }
        return false;
    }

    protected boolean navigationClick( int status, int time ) {
        return handleClick();
    }

    protected boolean trackwheelClick( int status, int time ) {
        return handleClick();
    }

    private boolean handleClick() {
        _editing = !_editing;
        invalidate( 0, 0, getWidth(), getHeight() );
        return true;
    }

    protected boolean touchEvent( TouchEvent message ) {
        if( message == null ) {
            throw new IllegalArgumentException( "ColorPickerField.touchEvent: TouchEvent message is null." );
        }
        boolean isConsumed = false;
        boolean isOutOfBounds = false;
        int x = message.getX( 1 );
        int y = message.getY( 1 );
        int[] argbData = new int[ 1 ];
        // Check to ensure point is within this field
        if( x <= 0 || y <= 0 || x > ( FIELD_WIDTH - 1 ) || y > ( FIELD_HEIGHT - 1 ) ) {
            isOutOfBounds = true;
        }
        switch( message.getEvent() ) {
            case TouchEvent.CLICK:
            case TouchEvent.MOVE:
                if( isOutOfBounds ) {
                    return true; // consume
                }
                _editing = true; // Pressed effect
                // update color
                _y = y;
                _rainbow.getARGB( argbData, 0, 1, 0, _y, 1, 1 );
                _selectedColor = argbData[ 0 ];
                invalidate( 0, 0, getWidth(), getHeight() );
                fieldChangeNotify( FieldChangeListener.PROGRAMMATIC );
                isConsumed = true;
                break;
            case TouchEvent.UNCLICK:
                if( isOutOfBounds ) {
                    _editing = false; // Reset presssed effect
                    return true;
                }

                // A field change notification is only sent on UNCLICK to allow for recovery
                // should the user cancel, i.e. click and move off the button

                _editing = false; // Reset pressed effect

                // update color
                _y = y;
                _rainbow.getARGB( argbData, 0, 1, 0, _y, 1, 1 );
                _selectedColor = argbData[ 0 ];
                invalidate( 0, 0, getWidth(), getHeight() );
                fieldChangeNotify( FieldChangeListener.PROGRAMMATIC );
                isConsumed = true;

                break;
        }
        return isConsumed;
    }

}

class TintChooser extends Field {
    private static final int FIELD_HEIGHT = 100;
    private static final int FIELD_WIDTH = 100;
    private static final int FRAME_SIZE = 10;
    private static final int FRAME_THICKNESS = 2;

    private int _baseColor;
    private int _selectedColor;

    private int[] _xcoords;
    private int[] _ycoords;
    private int[] _colors;
    private Bitmap _backgroundBitmap;

    private boolean _editing;

    private int _x;
    private int _y;

    TintChooser( int baseColor ) {
        _baseColor = baseColor;

        // init cursor position
        _x = FIELD_WIDTH - FRAME_SIZE / 2;
        _y = FRAME_SIZE;
    }

    public int getPreferredWidth() {
        return FIELD_WIDTH;
    }

    public int getPreferredHeight() {
        return FIELD_HEIGHT;
    }

    protected void layout( int width, int height ) {
        width = getPreferredWidth();
        height = getPreferredHeight();

        updateBitmap();

        setExtent( width, height );
    }

    public void setColor( int newColor ) {
        _baseColor = newColor;
        updateBitmap();
        invalidate();
    }

    private void updateBitmap() {
        int width = getPreferredWidth();
        int height = getPreferredHeight();

        _xcoords = new int[] { 0, 0, width, width };
        _ycoords = new int[] { 0, height, height, 0 };
        _colors = new int[] { 0xFFFFFF, 0x000000, 0x000000, _baseColor };

        // make new bitmap
        _backgroundBitmap = new Bitmap( width, height );// Bitmap.ROWWISE_MONOCHROME , width, height);// Bitmap.DEFAULT_TYPE,
                                                        // width, height );
        Graphics bitmapGraphics = Graphics.create( _backgroundBitmap );
        bitmapGraphics.drawShadedFilledPath( _xcoords, _ycoords, null, _colors, null );

        // Update selected color
        int[] argbData = new int[ 1 ];
        _backgroundBitmap.getARGB( argbData, 0, 1, _x, _y, 1, 1 );
        _selectedColor = argbData[ 0 ];
    }

    protected void paint( Graphics g ) {
        // Rainbow
        g.drawBitmap( 0, 0, _backgroundBitmap.getWidth(), _backgroundBitmap.getHeight(), _backgroundBitmap, 0, 0 );

        // Border
        g.setColor( 0x000000 );
        g.drawRect( 0, 0, FIELD_WIDTH, FIELD_HEIGHT );
        if( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ) {
            g.setColor( Color.WHITE );
            g.drawRect( 1, 1, FIELD_WIDTH - 2, FIELD_HEIGHT - 2 );
        }

        // Selector
        int frameX = _x;
        int frameY = _y;

        int oldColor = g.getColor();
        try {
            // draw the selector
            g.setColor( 0x000000 );
            frameX++;
            frameY++;

            // Shadow Left, Right, Top, Bottom
            g.fillRect( frameX - FRAME_SIZE / 2, frameY - FRAME_THICKNESS / 2, FRAME_SIZE / 2 - FRAME_THICKNESS, FRAME_THICKNESS );
            g.fillRect( frameX + FRAME_THICKNESS, frameY - FRAME_THICKNESS / 2, FRAME_SIZE / 2 - FRAME_THICKNESS, FRAME_THICKNESS );
            g.fillRect( frameX - FRAME_THICKNESS / 2, frameY - FRAME_SIZE / 2, FRAME_THICKNESS, FRAME_SIZE / 2 - FRAME_THICKNESS );
            g.fillRect( frameX - FRAME_THICKNESS / 2, frameY + FRAME_THICKNESS, FRAME_THICKNESS, FRAME_SIZE / 2 - FRAME_THICKNESS );

            frameX--;
            frameY--;

            g.setColor( Color.WHITE );
            // Left, Right, Top, Bottom
            g.fillRect( frameX - FRAME_SIZE / 2, frameY - FRAME_THICKNESS / 2, FRAME_SIZE / 2 - FRAME_THICKNESS, FRAME_THICKNESS );
            g.fillRect( frameX + FRAME_THICKNESS, frameY - FRAME_THICKNESS / 2, FRAME_SIZE / 2 - FRAME_THICKNESS, FRAME_THICKNESS );
            g.fillRect( frameX - FRAME_THICKNESS / 2, frameY - FRAME_SIZE / 2, FRAME_THICKNESS, FRAME_SIZE / 2 - FRAME_THICKNESS );
            g.fillRect( frameX - FRAME_THICKNESS / 2, frameY + FRAME_THICKNESS, FRAME_THICKNESS, FRAME_SIZE / 2 - FRAME_THICKNESS );
        } finally {
            g.setColor( oldColor );
        }
    }

    protected void paintBackground( Graphics g ) {
    }

    protected void drawFocus( Graphics g, boolean on ) {
        boolean oldDrawStyleFocus = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS );
        try {
            g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, true );
            paint( g );
        } finally {
            g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, oldDrawStyleFocus );
        }
    }

    public boolean isFocusable() {
        return true;
    }

    protected boolean keyChar( char key, int status, int time ) {
        if( key == Characters.ESCAPE ) {
            if( _editing ) {
                return handleClick();
            }
        } else if( key == Characters.ENTER ) {
            return handleClick();
        }
        return super.keyChar( key, status, time );
    }

    protected boolean navigationMovement( int dx, int dy, int status, int time ) {
        return handleMovement( dx, dy );
    }

    protected boolean trackwheelRoll( int amount, int status, int time ) {
        if( ( status & KeypadListener.STATUS_ALT ) != 0 ) {
            return handleMovement( amount, 0 );
        } else {
            return handleMovement( 0, amount );
        }
    }

    private boolean handleMovement( int dx, int dy ) {
        if( _editing ) {
            int xMovement = dx * FIELD_WIDTH / 20;
            int yMovement = dy * FIELD_HEIGHT / 20;

            _x = MathUtilities.clamp( 0, _x + xMovement, FIELD_WIDTH - 1 );
            _y = MathUtilities.clamp( 0, _y + yMovement, FIELD_HEIGHT - 1 );

            int[] argbData = new int[ 1 ];
            _backgroundBitmap.getARGB( argbData, 0, 1, _x, _y, 1, 1 );
            _selectedColor = argbData[ 0 ];

            invalidate( 0, 0, getWidth(), getHeight() );
            fieldChangeNotify( FieldChangeListener.PROGRAMMATIC );
            return true;
        }
        return false;
    }

    public int getSelectedColor() {
        return _selectedColor;
    }

    protected boolean navigationClick( int status, int time ) {
        return handleClick();
    }

    protected boolean trackwheelClick( int status, int time ) {
        return handleClick();
    }

    private boolean handleClick() {
        _editing = !_editing;
        invalidate( 0, 0, getWidth(), getHeight() );
        return true;
    }

    protected boolean touchEvent( TouchEvent message ) {
        if( message == null ) {
            throw new IllegalArgumentException( "ButtonField.touchEvent: TouchEvent message is null." );
        }
        boolean isConsumed = false;
        boolean isOutOfBounds = false;
        int x = message.getX( 1 );
        int y = message.getY( 1 );
        // Check to ensure point is within this field
        if( x <= 0 || y <= 0 || x > ( FIELD_WIDTH - 1 ) || y > ( FIELD_HEIGHT - 1 ) ) {
            isOutOfBounds = true;
        }
        int[] argbData = new int[ 1 ];
        switch( message.getEvent() ) {
            case TouchEvent.CLICK:
            case TouchEvent.MOVE:
                if( isOutOfBounds ) {
                    return true; // consume
                }
                _editing = true; // Pressed effect
                // update color
                _x = x;
                _y = y;
                _backgroundBitmap.getARGB( argbData, 0, 1, _x, _y, 1, 1 );
                _selectedColor = argbData[ 0 ];
                invalidate( 0, 0, getWidth(), getHeight() );
                fieldChangeNotify( FieldChangeListener.PROGRAMMATIC );
                isConsumed = true;
                break;
            case TouchEvent.UNCLICK:
                if( isOutOfBounds ) {
                    _editing = false; // Reset presssed effect
                    return true;
                }

                // A field change notification is only sent on UNCLICK to allow for recovery
                // should the user cancel, i.e. click and move off the button

                _editing = false; // Reset pressed effect

                // update state
                _x = x;
                _y = y;
                _backgroundBitmap.getARGB( argbData, 0, 1, _x, _y, 1, 1 );
                _selectedColor = argbData[ 0 ];
                invalidate( 0, 0, getWidth(), getHeight() );
                fieldChangeNotify( FieldChangeListener.PROGRAMMATIC );
                isConsumed = true;
                break;
        }
        return isConsumed;
    }
}

class ColorPreviewField extends Field {
    private static final int FIELD_WIDTH = 40;
    private static final int FIELD_HEIGHT = 30;

    private int _color;

    ColorPreviewField( int color ) {
        _color = color;
    }

    public int getPreferredWidth() {
        return FIELD_WIDTH;
    }

    public int getPreferredHeight() {
        return FIELD_HEIGHT;
    }

    protected void layout( int width, int height ) {
        setExtent( getPreferredWidth(), getPreferredHeight() );
    }

    public void setColor( int color ) {
        _color = color;
        invalidate();
    }

    protected void paint( Graphics g ) {
        int oldColor = g.getColor();
        try {
            g.setColor( _color );
            g.fillRect( 0, 0, FIELD_WIDTH, FIELD_HEIGHT );

            g.setColor( 0x000000 );
            g.drawRect( 0, 0, FIELD_WIDTH, FIELD_HEIGHT );
        } finally {
            g.setColor( oldColor );
        }
    }
}

class ColorPickerPopup extends PopupScreen implements FieldChangeListener {
    private BaseColorChooser _baseColorChooser;
    private TintChooser _tintChooser;
    private ColorPreviewField _previewField;
    private ButtonField _okButton;

    private final int PADDING = 4;
    private final int PADDING_BOTTOM = 39;
    private final String OK = "OK";

    public ColorPickerPopup( int initialColor ) {
        super( new HorizontalFieldManager() );
        setPadding( PADDING, PADDING, PADDING, PADDING );
        _baseColorChooser = new BaseColorChooser();
        _baseColorChooser.setPadding( PADDING, PADDING, PADDING, PADDING );
        _baseColorChooser.setChangeListener( this );
        add( _baseColorChooser );

        _tintChooser = new TintChooser( initialColor );
        _tintChooser.setPadding( PADDING, PADDING, PADDING, PADDING );
        _tintChooser.setChangeListener( this );
        add( _tintChooser );

        VerticalFieldManager previewPane = new VerticalFieldManager();

        _previewField = new ColorPreviewField( initialColor );
        _previewField.setPadding( PADDING, PADDING, PADDING + PADDING_BOTTOM, PADDING );
        previewPane.add( _previewField );

        _okButton = new ButtonField( OK );
        _okButton.setChangeListener( this );
        previewPane.add( _okButton );

        add( previewPane );
    }

    public void fieldChanged( Field field, int context ) {
        field.setDirty( false ); // don't want the save dialog
        if( field == _baseColorChooser ) {
            // Get color from base chooser
            // Update the tint chooser
            // Update the result color
            _tintChooser.setColor( _baseColorChooser.getSelectedColor() );
            _previewField.setColor( _tintChooser.getSelectedColor() );
        } else if( field == _tintChooser ) {
            // Update the result color
            _previewField.setColor( _tintChooser.getSelectedColor() );
        } else if( field == _okButton ) {
            close();
        }
    }

    protected boolean keyChar( char key, int status, int time ) {
        if( key == Characters.ESCAPE ) {
            close();
            return true;
        } else if( key == Characters.ENTER ) {
            close();
            return true;
        }
        return super.keyChar( key, status, time );
    }

    public int getSelectedColor() {
        return _tintChooser.getSelectedColor();
    }

    public boolean doModal() {
        UiApplication.getUiApplication().pushModalScreen( this );
        return true;
    }
}
