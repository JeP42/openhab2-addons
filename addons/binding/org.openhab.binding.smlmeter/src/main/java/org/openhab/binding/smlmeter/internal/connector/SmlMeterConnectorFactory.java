/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal.connector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to create a concrete type of meter connector. The connector is capable of reading SML from an energy meter.
 * Currently, there is only one type of connector supported: COMET COM-1 gateway with IR reader.
 *
 * @author pfaffmann
 *
 */
public class SmlMeterConnectorFactory {

    public static final String SML_METER_CONNECTOR_TYPE_COMET = "CometCom1";

    private static Map<String, SmlMeterConnector> connectors = new ConcurrentHashMap<>();

    public static SmlMeterConnector getConnector(String host, int port, String typeId) throws ConnectorException {
        String cacheKey = getCacheKey(host, port);
        SmlMeterConnector connector = connectors.get(cacheKey);
        if (connector == null) {
            connector = createConnectorFor(host, port, typeId);
            connectors.put(cacheKey, connector);
        }
        return connector;
    }

    private static SmlMeterConnector createConnectorFor(String host, int port, String typeId)
            throws ConnectorException {
        if (SML_METER_CONNECTOR_TYPE_COMET.equals(typeId)) {
            return new CometCOM1IRConnector(host, port);
        }
        throw new ConnectorException(String.format("The given connector type %s is not supported.", typeId));
    }

    private static String getCacheKey(String host, int port) {
        return host + ":" + port;
    }

}
