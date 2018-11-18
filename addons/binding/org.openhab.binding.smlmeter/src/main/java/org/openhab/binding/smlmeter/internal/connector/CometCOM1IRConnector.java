/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Comet Smart Meter Gateway COM-1 with IR reader to retrieve data from a power meters IR
 * interface according to IEC 62056-21
 *
 * @author pfaffmann
 *
 */
public class CometCOM1IRConnector implements SmlMeterConnector {

    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 2000;

    protected Logger logger = LoggerFactory.getLogger(CometCOM1IRConnector.class);

    private Socket socket;

    protected String host;

    protected int port;

    public CometCOM1IRConnector(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean isDeviceReady() {
        try {
            connect();
            return true;
        } catch (IOException e) {
            this.logger.error("IOEception while trying to connect to the gateway. Message: " + e.getMessage(), e);
            return false;
        } finally {
            closeConnection();
        }
    }

    private void connect() throws UnknownHostException, IOException {
        if (this.socket == null) {
            this.socket = SocketFactory.getDefault().createSocket();
        }

        if (!this.socket.isConnected()) {
            this.logger.debug("Connecting to " + this.host + ":" + this.port);
            this.socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_IN_MILLISECONDS);
        }
    }

    private void closeConnection() {
        try {
            this.logger.debug("Closing socket connection...");
            if (this.socket != null && socket.isConnected()) {
                this.socket.close();
                this.socket = null;
            }
        } catch (IOException e) {
            throw new RuntimeException("An unexpected exception occured while closing ressources.", e);
        }
    }

    @Override
    public String getRawSmlFile() throws IOException {
        try {
            connect();
            return readRawSmlFileFromSocket();
        } finally {
            closeConnection();
        }
    }

    private String readRawSmlFileFromSocket() throws IOException {
        if (this.socket == null) {
            throw new IllegalArgumentException("Socket connection not properly initialized");
        }
        InputStream ins = this.socket.getInputStream();

        boolean doRead = false;

        StringBuffer smlMessage = new StringBuffer();
        byte[] buffer = new byte[1];

        this.logger.debug("Searching for raw SML file...");
        while (ins.read(buffer) == 1) {
            if ("(".equals(new String(buffer))) {
                doRead = true;
                smlMessage = new StringBuffer();
            } else {
                if ((doRead) && (")".equals(new String(buffer)))) {
                    doRead = false;
                    this.logger.debug("Read the raw SML file: " + smlMessage.toString());
                    return smlMessage.toString();
                }
                if (doRead) {
                    smlMessage.append(new String(buffer));
                }
            }
        }
        return null;
    }

}
