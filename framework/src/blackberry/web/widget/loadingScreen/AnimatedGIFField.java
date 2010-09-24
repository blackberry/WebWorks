/*
* Copyright 2010 Research In Motion Limited.
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

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BitmapField;

//A field that displays an animated GIF. 

public class AnimatedGIFField extends BitmapField {
        private GIFEncodedImage _image; // The image to draw.
        private int _currentFrame; // The current frame in the animation sequence.
        //private int _width; // The width of the image (background frame).
        //private int _height; // The height of the image (background frame).
        private AnimatorThread _animatorThread;

        public AnimatedGIFField(GIFEncodedImage image) {
                this(image, 0);
        }

        public AnimatedGIFField(GIFEncodedImage image, long style) {
                // Call super to setup the field with the specified style.
                // The image is passed in as well for the field to
                // configure its required size.
                super(image.getBitmap(), style);

                // Store the image and it's dimensions.
                _image = image;
                //_width = image.getWidth();
                //_height = image.getHeight();

                // Start the animation thread.
                _animatorThread = new AnimatorThread(this);
                _animatorThread.start();
        }

        protected void paint(Graphics graphics) {
                // Call super.paint. This will draw the first background
                // frame and handle any required focus drawing.
                super.paint(graphics);

                // Don't redraw the background if this is the first frame.
                if (_currentFrame != 0) {
                        // Draw the animation frame.
                        graphics
                        .drawImage(_image.getFrameLeft(_currentFrame), _image
                                        .getFrameTop(_currentFrame), _image
                                        .getFrameWidth(_currentFrame), _image
                                        .getFrameHeight(_currentFrame), _image,
                                        _currentFrame, 0, 0);
                }
        }

        // Stop the animation thread when the screen the field is on is
        // popped off of the display stack.
        protected void onUndisplay() {
                _animatorThread.stopAnimation();
                super.onUndisplay();
        }
        
        protected void onDisplay() {
                _animatorThread.startAnimation();
                super.onUndisplay();            
        }        

        // A thread to handle the animation.
        private class AnimatorThread extends Thread {
                private AnimatedGIFField _theField;
                private boolean _keepGoing = false;
                private int _totalFrames; // The total number of frames in the image.
                private int _loopCount; // The number of times the animation has looped
                // (completed).
                private int _totalLoops; // The number of times the animation should
                // loop (set in the image).

                public AnimatorThread(AnimatedGIFField theField) {
                        _theField = theField;
                        _totalFrames = _image.getFrameCount();
                        _totalLoops = _image.getIterations();

                }

                public synchronized void stopAnimation() {
                        _keepGoing = false;
                }

                public synchronized void startAnimation() {
                        _keepGoing = true;
                }
                
                public void run() {
                    while(true) {
                        while (_keepGoing) {
                                // Invalidate the field so that it is redrawn.
                                UiApplication.getUiApplication().invokeAndWait(new Runnable() {
                                        public void run() {
                                                _theField.invalidate();
                                        }
                                });

                                try {
                                        // Sleep for the current frame delay before
                                        // the next frame is drawn.
                                        sleep(_image.getFrameDelay(_currentFrame) * 10);
                                } catch (InterruptedException iex) {
                                } // Couldn't sleep.

                                // Increment the frame.
                                ++_currentFrame;

                                if (_currentFrame == _totalFrames) {
                                        // Reset back to frame 0 if we have reached the end.
                                        _currentFrame = 0;

                                        ++_loopCount;

                                        // Check if the animation should continue.
                                        if (_loopCount == _totalLoops) {
                                                _keepGoing = false;
                                        }
                                }
                        }
                        
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }
        }
}
