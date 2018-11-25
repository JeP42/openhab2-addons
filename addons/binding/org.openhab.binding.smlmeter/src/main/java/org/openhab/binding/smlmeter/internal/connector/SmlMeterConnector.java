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
 * Interface of an abstract device which is capable to connect with an energy meter which pushes its values via SML
 * protocol.
 *
 * @author pfaffmann - Initial contribution
 *
 */
public interface SmlMeterConnector {

    /**
     * Retrieve an SML File from the connected meter and return as String
     *
     * @return
     * @throws IOException
     */
    String getRawSmlFile() throws IOException;

    /**
     * Checks connection with the connector
     *
     * @return
     */
    boolean isDeviceReady();

}
