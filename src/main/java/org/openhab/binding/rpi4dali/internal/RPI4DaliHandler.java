/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.rpi4dali.internal;

import static org.openhab.binding.rpi4dali.internal.RPI4DaliBindingConstants.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RPI4DaliHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class RPI4DaliHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RPI4DaliHandler.class);
    private @Nullable InputStream initialStream;
    private @Nullable ScheduledFuture<?> refresh;
    private @Nullable ScheduledFuture<?> scanning;
    private boolean isScanRunning;

    public RPI4DaliHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel chn = getThing().getChannel(channelUID.getId());
        String strCh = null;
        if (chn != null) {
            strCh = chn.getAcceptedItemType();
            if (strCh != null && strCh.equals(DIMMER)) {
                String DaliCommand = "";
                DaliCommand = String.format("%02X", Math.round(Integer.parseInt(command.toString()) * 2.54));
                Configuration channelConfig = null;
                channelConfig = chn.getConfiguration();
                String SendingData = channelConfig.get("sequence").toString() + channelConfig.get("address").toString()
                        + DaliCommand;
                sendData(SendingData);
            }
        }
    }

    private void sendData(String sendingData) {
        // Path outputFile = Paths.get("/dev/dali");
        // try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
        // writer.write(sendingData);
        // } catch (IOException ex) {
        // }
    }

    @Override
    public void initialize() {
        isScanRunning = false;
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, "Initializing...");
        refresh = scheduler.scheduleWithFixedDelay(this::refresh, 0, 10, TimeUnit.SECONDS);
        scanning = scheduler.schedule(this::scanDALI, 0, TimeUnit.SECONDS);
        // scheduler.execute(this::scanDALI);
        updateStatus(ThingStatus.ONLINE);
    }

    private void refresh() {
        logger.debug("Refreshing...");
    }

    @Override
    public void dispose() {
        refresh.cancel(true);
        scanning.cancel(true);
        InputStream initialStream = this.initialStream;
        try {
            if (initialStream != null) {
                initialStream.close();
            }
        } catch (IOException ignored) {
        }
        super.dispose();
    }

    void scanDALI() {
        logger.debug("Start scanning");
        if (!isScanRunning) {
            BufferedReader br = null;
            try {
                initialStream = new FileInputStream("/dev/dali");
                br = new BufferedReader(new InputStreamReader(initialStream));
                // noinspection InfiniteLoopStatement
                while (true) {
                    String line = br.readLine();
                    while (line != null) {
                        logger.debug("Receive: {}", line);
                    }
                    logger.debug("Empty");
                }
            } catch (IOException e) {
                logger.debug("error int: {}", e.getLocalizedMessage());
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                }
            }
            isScanRunning = true;
        }
    }
}
