/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.automacrobuilder.zap;

import java.awt.CardLayout;
import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTrace;
import org.zaproxy.zap.extension.automacrobuilder.ParmGenMacroTraceProvider;
import org.zaproxy.zap.extension.automacrobuilder.generated.MacroBuilderUI;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * An example ZAP extension which adds a top level menu item, a pop up menu item and a status panel.
 *
 * <p>{@link ExtensionAdaptor} classes are the main entry point for adding/loading functionalities
 * provided by the add-ons.
 *
 * @see #hook(ExtensionHook)
 */
public class ExtensionAutoMacroBuilder extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionAutoMacroBuilder";

    // The i18n prefix, by default the package name - defined in one place to make it easier
    // to copy and change this example
    protected static final String PREFIX = "autoMacroBuilder";

    /**
     * Relative path (from add-on package) to load add-on resources.
     *
     * @see Class#getResource(String)
     */
    private static final String RESOURCES =
            "/org/zaproxy/zap/extension/automacrobuilder/zap/resources";

    private static final ImageIcon ICON =
            new ImageIcon(ExtensionAutoMacroBuilder.class.getResource(RESOURCES + "/cake.png"));

    // private static final String EXAMPLE_FILE = "example/ExampleFile.txt";

    private ZapMenuItem menuExample = null;
    private RightClickMsgMenu popupMsgMenuExample = null;
    private AbstractPanel statusPanel = null;
    private PopupMenuAdd2MacroBuilder popupadd2MacroBuilder = null;
    private ParmGenMacroTrace pmt = ParmGenMacroTraceProvider.getOriginalBase();
    private MacroBuilderUI mbui = null;

    // private SimpleExampleAPI api;

    private static final org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();

    public ExtensionAutoMacroBuilder() {
        super(NAME);
        setI18nPrefix(PREFIX);

        if (this.mbui == null) {
            this.mbui = new MacroBuilderUI(this.pmt);
        }
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        // this.api = new SimpleExampleAPI(this);
        // extensionHook.addApiImplementor(this.api);
        ExtensionActiveScanWrapper extwrapper = new ExtensionActiveScanWrapper();

        // As long as we're not running as a daemon
        if (getView() != null) {
            // extensionHook.getHookMenu().addToolsMenuItem(getMenuExample());
            // extensionHook.getHookMenu().addPopupMenuItem(getPopupMsgMenuExample());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAdd2MacroBuilder());
            // extensionHook.getHookView().addStatusPanel(getStatusPanel());
            extensionHook
                    .getHookView()
                    .addWorkPanel(
                            new MyWorkPanel(
                                    extwrapper,
                                    this.mbui,
                                    this.pmt,
                                    "MacroBuilder",
                                    extensionHook));
            // extensionHook.getHookView().addStatusPanel(new MyWorkPanel("StatusPanel", LOGGER));
            // extensionHook.getHookView().addSelectPanel(new MyWorkPanel("SelectPanel", LOGGER));
        }
        // add ScannerHook...
        LOGGER4J.debug("MyFirstScannerHook addScannerHook..");
        extensionHook.addScannerHook(
                new MyFirstScannerHook(extwrapper.getStartedActiveScanContainer()));
        // add listener
        extensionHook.addHttpSenderListener(
                new MyFirstSenderListener(extwrapper.getStartedActiveScanContainer()));
    }

    @Override
    public boolean canUnload() {
        // The extension can be dynamically unloaded, all resources used/added can be freed/removed
        // from core.
        return true;
    }

    @Override
    public void unload() {
        super.unload();

        // In this example it's not necessary to override the method, as there's nothing to unload
        // manually, the components added through the class ExtensionHook (in hook(ExtensionHook))
        // are automatically removed by the base unload() method.
        // If you use/add other components through other methods you might need to free/remove them
        // here (if the extension declares that can be unloaded, see above method).
    }

    private AbstractPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new AbstractPanel();
            statusPanel.setLayout(new CardLayout());
            statusPanel.setName(Constant.messages.getString(PREFIX + ".panel.title"));
            // System.out.println("getString():" + Constant.messages.getString(PREFIX + ".gorua"));
            statusPanel.setIcon(ICON);
            JTextPane pane = new JTextPane();
            pane.setEditable(false);
            // Obtain (and set) a font with the size defined in the options
            pane.setFont(FontUtils.getFont("Dialog", Font.PLAIN));
            pane.setContentType("text/html");
            pane.setText(Constant.messages.getString(PREFIX + ".panel.msg"));
            statusPanel.add(pane);
        }
        return statusPanel;
    }

    private ZapMenuItem getMenuExample() {
        if (menuExample == null) {
            menuExample = new ZapMenuItem(PREFIX + ".topmenu.tools.title");

            menuExample.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent ae) {
                            // This is where you do what you want to do.
                            // In this case we'll just show a popup message.
                            View.getSingleton()
                                    .showMessageDialog(
                                            Constant.messages.getString(
                                                    PREFIX + ".topmenu.tools.msg"));
                            // And display a file included with the add-on in the Output tab
                            displayFile("");
                        }
                    });
        }
        return menuExample;
    }

    private void displayFile(String file) {
        if (!View.isInitialised()) {
            // Running in daemon mode, shouldnt have been called
            return;
        }
        try {
            /*
            File f = new File(Constant.getZapHome(), file);
            if (!f.exists()) {
                // This is something the user should know, so show a warning dialog
                View.getSingleton()
                        .showWarningDialog(
                                Constant.messages.getString(
                                        ExtensionSimpleExample.PREFIX + ".error.nofile",
                                        f.getAbsolutePath()));
                return;
            }
            */
            // Quick way to read a small text file
            String contents = new String("brah Brah ...");
            // Write to the output panel
            View.getSingleton().getOutputPanel().append(contents);
            // Give focus to the Output tab
            View.getSingleton().getOutputPanel().setTabFocus();
        } catch (Exception e) {
            // Something unexpected went wrong, write the error to the log
            LOGGER4J.error(e.getMessage(), e);
        }
    }

    private RightClickMsgMenu getPopupMsgMenuExample() {
        if (popupMsgMenuExample == null) {
            popupMsgMenuExample =
                    new RightClickMsgMenu(
                            this, Constant.messages.getString(PREFIX + ".popup.title"));
        }
        return popupMsgMenuExample;
    }

    private PopupMenuAdd2MacroBuilder getPopupMenuAdd2MacroBuilder() {
        if (popupadd2MacroBuilder == null) {
            popupadd2MacroBuilder =
                    new PopupMenuAdd2MacroBuilder(
                            this.mbui,
                            this.pmt,
                            Constant.messages.getString(
                                    PREFIX + ".popup.title.PopupMenuAdd2MacroBuilder"));
        }
        return popupadd2MacroBuilder;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString(PREFIX + ".desc");
    }

    @Override
    public URL getURL() {
        try {
            return new URL(Constant.ZAP_EXTENSIONS_PAGE);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
