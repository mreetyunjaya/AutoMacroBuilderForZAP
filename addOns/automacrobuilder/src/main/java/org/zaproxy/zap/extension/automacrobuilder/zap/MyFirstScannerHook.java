/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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

import java.lang.reflect.Field;
import java.util.logging.Level;
import org.parosproxy.paros.core.scanner.AbstractPlugin;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Scanner;
import org.parosproxy.paros.core.scanner.ScannerHook;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.automacrobuilder.CastUtils;
import org.zaproxy.zap.network.HttpRequestConfig;
import org.zaproxy.zap.network.HttpRequestConfig.Builder;

public class MyFirstScannerHook implements ScannerHook {

    private static final org.apache.logging.log4j.Logger LOGGER4J =
            org.apache.logging.log4j.LogManager.getLogger();

    private StartedActiveScanContainer startedcon = null;

    MyFirstScannerHook(StartedActiveScanContainer startedcon) {
        this.startedcon = startedcon;
    }

    @Override
    public void afterScan(HttpMessage arg0, AbstractPlugin arg1, Scanner arg2) {
        // TODO Auto-generated method stub

        LOGGER4J.debug("MyFirstScannerHook afterScan Called. URL[" + getURL(arg0) + "]");
    }

    @Override
    public void beforeScan(HttpMessage arg0, AbstractPlugin arg1, Scanner arg2) {
        // TODO Auto-generated method stub
        LOGGER4J.debug("MyFirstScannerHook beforeScan Called. URL[" + getURL(arg0) + "]");

        if (this.startedcon.isStartedActiveScan(
                arg2)) { // only call following methods when Scanner.start(Target) is
            // called by ExtensionActiveScanWrapper
            LOGGER4J.info("same scanId:" + arg2.getId());
            HostProcess hpros = arg1.getParent();
            // always disable followRedirects.
            hpros.getHttpSender().setMaxRedirects(0);
            hpros.getHttpSender().setFollowRedirect(false);
            // forceUser set to null for disabling authentication
            hpros.getHttpSender().setUser(null);
            this.startedcon
                    .addTheadid(); // Add the thread ID that belongs to Start ActiveScan for telling
            this.startedcon.addParmGenMacroTraceParams(arg2);
            // to senderListner.
        } else {
            LOGGER4J.debug("differ scanId:" + arg2.getId());
        }
    }

    @Override
    public void scannerComplete() {
        // TODO Auto-generated method stub
        LOGGER4J.debug("MyFirstScannerHook scannerComplete Called. ");
    }

    /**
     * Create HttpRequestConfig, set followRedirect to false, and then set it to
     * HostProcess.redirectRequestConfig. this function will be used when there is no way to set
     * FolloRedirect(false) Currently no used.
     *
     * @param hpros HostProcess
     */
    private void disableFollowRedirection(HostProcess hpros) {
        Class<HostProcess> clazz = CastUtils.castToType(hpros.getClass());
        try {
            Field rconfigfield = clazz.getDeclaredField("redirectRequestConfig");
            rconfigfield.setAccessible(true);
            HttpRequestConfig oldconfig = CastUtils.castToType(rconfigfield.get(hpros));
            if (oldconfig == null || oldconfig.isFollowRedirects()) {
                Builder builder = HttpRequestConfig.builder();
                builder.setFollowRedirects(false);
                HttpRequestConfig newconfig = builder.build();
                rconfigfield.set(hpros, newconfig);
            }
        } catch (NoSuchFieldException ex) {
            java.util.logging.Logger.getLogger(MyFirstScannerHook.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            java.util.logging.Logger.getLogger(MyFirstScannerHook.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            java.util.logging.Logger.getLogger(MyFirstScannerHook.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MyFirstScannerHook.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    public String getURL(HttpMessage hm) {
        String url = "";
        if (hm != null) {
            HttpRequestHeader hd = hm.getRequestHeader();
            url = hd.getURI().toString();
        }

        return url;
    }
}
