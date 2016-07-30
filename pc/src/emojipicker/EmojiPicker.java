package emojipicker;

/*
 * Copyright (C) 2016 Matteo Lobello
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

import emojipicker.gui.EmojiPickerGui;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class EmojiPicker {
    
    /**
     * The GUI of the program
     */
    private static EmojiPickerGui frame;
    
    /**
     * The background thread that listens to new emojis sent from Android
     */
    private static ListenThread listenThread;

    /**
     * The main method
     * 
     * @param args
     * @throws IOException
     * @throws AWTException 
     */
    public static void main(String args[]) throws IOException, AWTException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    frame = new EmojiPickerGui();
                    frame.setVisible(true);
                    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    frame.setTitle(getClass().getSimpleName());
                    frame.setResizable(false);
                    frame.setPreferredSize(new Dimension(700, 436));
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                } catch (Exception ex) {
                    Logger.getLogger(EmojiPicker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
         
        listenThread = new ListenThread();
        listenThread.start();
    }

    /**
     * The background thread
     */
    private static class ListenThread extends Thread {

        private final Robot robot;

        private ServerSocket serverSocket;
        private String previousCopiedText;
        
        /**
         * The constructor
         * 
         * @throws AWTException 
         */
        private ListenThread() throws AWTException {
            this.robot = new Robot();
            
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                listen();
            }
        }

        /**
         * The method that keeps listeneing to new data from socket
         */
        private void listen() {
            try {
                if (serverSocket == null) {
                    serverSocket = new ServerSocket(8888);
                }
                Socket clientSocket = serverSocket.accept();

                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());

                String msgReceived = dataInputStream.readUTF();
                
                if (msgReceived.contains("Connected to:")) {                    
                    frame.updateImage("/images/connected_bg.png");
                } else {
                    previousCopiedText = (String) Toolkit.getDefaultToolkit()
                        .getSystemClipboard().getData(DataFlavor.stringFlavor);
                                        
                    handleNewReceivedEmoji(msgReceived);
                }
            } catch (IOException ex) {
                Logger.getLogger(EmojiPicker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (HeadlessException ex) {
                Logger.getLogger(EmojiPicker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedFlavorException ex) {
                Logger.getLogger(EmojiPicker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (AWTException ex) {
                Logger.getLogger(EmojiPicker.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(EmojiPicker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Handle a new received Emoji
         * 
         * @param msgReceived
         * @throws UnsupportedFlavorException
         * @throws IOException
         * @throws AWTException
         * @throws InterruptedException 
         */
        private void handleNewReceivedEmoji(String msgReceived) 
                throws UnsupportedFlavorException, IOException, AWTException, InterruptedException {
            copy(msgReceived);
            paste();
        }  
        
        /**
         * Copy the Emoji to clipboard
         * 
         * @param text
         * @throws UnsupportedFlavorException
         * @throws IOException 
         */
        private void copy(String text) throws UnsupportedFlavorException, IOException {            
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection selection = new StringSelection(text);
            clipboard.setContents(selection, selection);
        }

        /**
         * Paste the Emoji
         * 
         * @throws AWTException
         * @throws IOException
         * @throws UnsupportedFlavorException
         * @throws InterruptedException 
         */
        private void paste() throws AWTException, IOException,
                UnsupportedFlavorException, InterruptedException {    
            
            int control = KeyEvent.VK_CONTROL;
            int v = KeyEvent.VK_V;
            
            String osName = System.getProperty("os.name").toLowerCase();
            boolean isMacOs = osName.startsWith("mac os x");
            if (isMacOs) {
                // Use CMD button on macOS
                control = 157;
            }
                                              
            robot.keyPress(control);
            robot.keyPress(v);
            robot.keyRelease(control);
            robot.keyRelease(v);
            
            robot.delay(100);
            
            Thread.sleep(100);
            
            copy(previousCopiedText);
        }
    }
}