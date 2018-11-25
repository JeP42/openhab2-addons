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

/**
 * Exception class used for exceptions related to the SmlMeterConnector
 *
 * @author pfaffmann - Initial contribution
 *
 */
public class ConnectorException extends Exception {

    private static final long serialVersionUID = -615572953968979350L;

    public ConnectorException(String msg) {
        super(msg);
    }

    public ConnectorException(Throwable t) {
        super(t);
    }

    public ConnectorException(String msg, IOException t) {
        super(msg, t);
    }

}
