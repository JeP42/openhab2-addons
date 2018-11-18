/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SmlMeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jens Pfaffmann - Initial contribution
 */
@NonNullByDefault
public class SmlMeterBindingConstants {

    private static final String BINDING_ID = "smlmeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SMLMETER = new ThingTypeUID(BINDING_ID, "smlmeter");

    // List of all Channel ids
    public static final String CHANNEL_OBIS_1_8_0 = "180";

    public static final String CHANNEL_OBIS_1_8_1 = "181";

    public static final String CHANNEL_OBIS_2_8_0 = "280";

    public static final String CHANNEL_OBIS_2_8_1 = "281";

    public static final String CHANNEL_OBIS_15_7_0 = "1570";

    public static final String CHANNEL_VENDOR = "vendor";

    public static final String CHANNEL_DEVICE_ID = "deviceid";

    public static final String CHANNEL_POWER_DIRECTION = "powerdirection";
}
