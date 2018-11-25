/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal.connector;

/**
 * Compares values from a current and a previous meter snapshot to find out whether power is consumed or produced.
 *
 * @author pfaffmann - Initial contribution
 *
 */
public class PowerDirectionHelper {

    private MeterSnapshot previousSnapshot;

    public PowerDirection getDirectionFor(MeterSnapshot currentSnapshot) {
        PowerDirection direction = PowerDirection.UNKNOWN;

        if (previousSnapshot != null) {
            if (this.isIncreasedConsumption(currentSnapshot)) {
                direction = PowerDirection.IN;
            } else if (this.isIncreasedProduction(currentSnapshot)) {
                direction = PowerDirection.OUT;
            } else {
                direction = PowerDirection.NONE;
            }
        }
        this.previousSnapshot = currentSnapshot;
        return direction;
    }

    private boolean isIncreasedConsumption(MeterSnapshot currentSnapshot) {
        return previousSnapshot.getObis180().compareTo(currentSnapshot.getObis180()) < 0;
    }

    private boolean isIncreasedProduction(MeterSnapshot currentSnapshot) {
        return previousSnapshot.getObis280().compareTo(currentSnapshot.getObis280()) < 0;
    }

}
