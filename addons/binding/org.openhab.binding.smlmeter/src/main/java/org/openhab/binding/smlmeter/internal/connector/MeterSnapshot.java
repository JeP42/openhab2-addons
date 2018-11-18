/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal.connector;

import java.math.BigDecimal;

/**
 *
 * Represents a snapshot of the meter values at a particular time
 *
 * @author pfaffmann
 *
 */
public class MeterSnapshot {

    private long time;

    private String vendorId;
    private String deviceId;

    private BigDecimal obis180;
    private BigDecimal obis181;
    private BigDecimal obis280;
    private BigDecimal obis281;
    private BigDecimal obis1570;

    public MeterSnapshot() {
        super();
        this.time = System.currentTimeMillis();
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public BigDecimal getObis180() {
        return obis180;
    }

    public void setObis180(BigDecimal obis180) {
        this.obis180 = obis180;
    }

    public BigDecimal getObis181() {
        return obis181;
    }

    public void setObis181(BigDecimal obis181) {
        this.obis181 = obis181;
    }

    public BigDecimal getObis280() {
        return obis280;
    }

    public void setObis280(BigDecimal obis280) {
        this.obis280 = obis280;
    }

    public BigDecimal getObis281() {
        return obis281;
    }

    public void setObis281(BigDecimal obis281) {
        this.obis281 = obis281;
    }

    public BigDecimal getObis1570() {
        return obis1570;
    }

    public void setObis1570(BigDecimal obis1570) {
        this.obis1570 = obis1570;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
