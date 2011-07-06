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
package blackberry.web.widget.loadingScreen;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

import blackberry.web.widget.impl.WidgetConfigImpl;

public class LoadingScreen extends MainScreen {
    private HorizontalFieldManager _hfm;
    private BitmapField _foregroundImage;
    private WidgetConfigImpl _widgetConfigImpl;

    private PageManager _pageManager;

    public LoadingScreen( WidgetConfigImpl widgetConfigImpl, PageManager pageManager ) {
        super( Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL | Field.USE_ALL_HEIGHT );

        _hfm = null;
        _widgetConfigImpl = widgetConfigImpl;
        _pageManager = pageManager;

        int bgColor = processColorString( _widgetConfigImpl.getLoadingScreenColor() );
        
        if( _widgetConfigImpl.getBackgroundImage().length() != 0 ) {
            // Set background image
            EncodedImage backgroundImage = EncodedImage.getEncodedImageResource( _widgetConfigImpl.getBackgroundImage() );
            if( backgroundImage != null ) {
            	
            	// Resize the image to the display size.
            	int scaleX = Fixed32.div( Fixed32.toFP( backgroundImage.getWidth() ), Fixed32.toFP( Display.getWidth() ) );
            	int scaleY = Fixed32.div( Fixed32.toFP( backgroundImage.getHeight() ), Fixed32.toFP( Display.getHeight() ) );
            	EncodedImage scaledBgImage = backgroundImage.scaleImage32(scaleX, scaleY);

            	// Using the scaled bg image draw onto the blank white bitmap.
            	Bitmap bgImg = new Bitmap( scaledBgImage.getScaledWidth(), scaledBgImage.getScaledHeight() );
                Graphics g = new Graphics( bgImg );
                if( bgColor != -1 ) {
                	g.setColor( bgColor );
                } else {
                	g.setColor( Color.WHITE );
                }
                g.fillRect( 0, 0, scaledBgImage.getScaledWidth(), scaledBgImage.getScaledHeight() );
                g.drawImage( 0, 0, scaledBgImage.getScaledWidth(), scaledBgImage.getScaledHeight(),
                		scaledBgImage, 0, 0, 0 );

                Background bg = BackgroundFactory.createBitmapBackground( bgImg,
                        Background.POSITION_X_CENTER, Background.POSITION_Y_CENTER, Background.REPEAT_SCALE_TO_FIT );
                this.setBackground( bg );
                this.getMainManager().setBackground( bg );
            }
        } else {
            // -1 denotes an invalid color
            if( bgColor != -1 ) {
                Background color = BackgroundFactory.createSolidBackground( bgColor );
                this.setBackground( color );
                this.getMainManager().setBackground( color );
            }
        }

        if( _widgetConfigImpl.getForegroundImage().length() != 0 ) {
            EncodedImage foregroundImage = EncodedImage.getEncodedImageResource( _widgetConfigImpl.getForegroundImage() );
            if( foregroundImage != null ) {
                _hfm = new HorizontalFieldManager( Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL
                        | Field.NON_FOCUSABLE | Field.FIELD_HCENTER );

                if( foregroundImage instanceof GIFEncodedImage ) {
                    _foregroundImage = new AnimatedGIFField( (GIFEncodedImage) foregroundImage );
                    ( ( AnimatedGIFField) _foregroundImage ).startAnimation();
                } else {
                    _foregroundImage = new BitmapField( foregroundImage.getBitmap() );
                }

                // Add the _foregroundImage field
                _hfm.add( _foregroundImage );

                int topEmptySpace = ( Display.getHeight() - _hfm.getPreferredHeight() ) / 2;
                _hfm.setMargin( topEmptySpace, 0, 0, 0 );
                add( _hfm );
            }
        }
    }

    protected void sublayout( int width, int height ) {
        // Set _hfm vertically center of the screen by setting its top margin
        if( _hfm != null ) {
            int topEmptySpace = ( Display.getHeight() - _hfm.getPreferredHeight() ) / 2;
            _hfm.setMargin( topEmptySpace, 0, 0, 0 );
        }

        super.sublayout( width, height );
    }

    /*
     * Process a string in the format "#000000" and return the hex int value. -1 denotes an invalid color
     */
    private int processColorString( String colorString ) {
        // Remove leading #
        if( colorString != null && colorString.startsWith( "#" ) && colorString.length() == 7 ) {
            colorString = colorString.substring( 1 );

            // Attempt to convert string to hex
            try {
                return Integer.parseInt( colorString, 16 );
            } catch( Exception e ) {
                return -1;
            }
        } else {
            // Failed to determine color
            return -1;
        }
    }

    public boolean onMenu( int instance ) {
        return false;
    }

    public boolean onClose() {
        return false;
    }

    /**
     * Handle the escape button if it was not previously handled
     */
    protected boolean keyCharUnhandled( char key, int status, int time ) {
        // Only catch the 'back' button
        if( key == Characters.ESCAPE ) {
            if( _pageManager.isGoingBackSafe() ) {
                _pageManager.cancelNewPage();
            }

            return false;
        }

        return super.keyCharUnhandled( key, status, time );
    }
}
