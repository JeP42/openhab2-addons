/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlmeter.internal;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.smlmeter.internal.connector.ConnectorException;
import org.openhab.binding.smlmeter.internal.connector.MeterSnapshot;
import org.openhab.binding.smlmeter.internal.connector.PowerDirectionHelper;
import org.openhab.binding.smlmeter.internal.connector.SmlMeterConnector;
import org.openhab.binding.smlmeter.internal.connector.SmlMeterConnectorFactory;
import org.openhab.binding.smlmeter.internal.connector.SmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmlMeterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jens Pfaffmann - Initial contribution
 */
@NonNullByDefault
public class SmlMeterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmlMeterHandler.class);

    @Nullable
    private SmlMeterConfiguration config;

    @Nullable
    ScheduledFuture<?> refreshJob;

    @Nullable
    private ScheduledFuture<?> initJob;

    PowerDirectionHelper powerDirectionHelper = new PowerDirectionHelper();

    public SmlMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");

        config = getConfigAs(SmlMeterConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            SmlMeterConnector connector;
            try {
                connector = SmlMeterConnectorFactory.getConnector(config.host, config.port, config.type);

                if (connector.isDeviceReady()) {
                    updateStatus(ThingStatus.ONLINE);
                    this.startRefreshJob();
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            } catch (ConnectorException e) {
                this.logger.error(e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        this.refreshJob.cancel(true);
    }

    private void startRefreshJob() {
        this.refreshJob = this.scheduler.scheduleAtFixedRate(() -> {
            update();
        }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void update() {
        try {
            SmlMeterConnector connector = SmlMeterConnectorFactory.getConnector(config.host, config.port, config.type);

            // we get an SML file as String from the connector and pass it to the SML reader.
            // The SML reader parses the string and retrieves values which are then used to update state of the channels
            MeterSnapshot snapshot = new SmlReader().read(connector.getRawSmlFile());
            // when internal crc check fails null is returned... This happens from time to time, we can just ignore
            // it...
            if (snapshot != null) {
                updateStatus(ThingStatus.ONLINE);

                this.updateState(SmlMeterBindingConstants.CHANNEL_POWER_DIRECTION,
                        new StringType(powerDirectionHelper.getDirectionFor(snapshot).toString()));

                this.updateState(SmlMeterBindingConstants.CHANNEL_OBIS_1_8_0, new DecimalType(snapshot.getObis180()));
                this.updateState(SmlMeterBindingConstants.CHANNEL_OBIS_1_8_1, new DecimalType(snapshot.getObis181()));

                this.updateState(SmlMeterBindingConstants.CHANNEL_OBIS_2_8_0, new DecimalType(snapshot.getObis280()));
                this.updateState(SmlMeterBindingConstants.CHANNEL_OBIS_2_8_1, new DecimalType(snapshot.getObis281()));

                this.updateState(SmlMeterBindingConstants.CHANNEL_OBIS_15_7_0, new DecimalType(snapshot.getObis1570()));

                this.updateState(SmlMeterBindingConstants.CHANNEL_VENDOR, new StringType(snapshot.getVendorId()));
                this.updateState(SmlMeterBindingConstants.CHANNEL_DEVICE_ID, new StringType(snapshot.getDeviceId()));
            }

        } catch (ConnectorException e) {
            // this situation is unrecoverable, set thing OFFLINE and give up...
            this.logger.error("An unrecoverable error occurred during execution: {}", e.getMessage(), e);
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            this.refreshJob.cancel(false);
        } catch (IOException e) {
            // this may be a temporary problem... log the message and continue...
            this.logger.warn("An error occured during execution, will try again. Message was: {}", e.getMessage());
        }
    }

}
