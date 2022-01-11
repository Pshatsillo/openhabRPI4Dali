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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
    private @Nullable ScheduledFuture<?> refresh;
    private @Nullable ScheduledFuture<?> scanning;
    private String dimmervalue = "";
    boolean disposed;
    boolean disposeout = false;
    private @Nullable BufferedReader br = null;

    public RPI4DaliHandler(Thing thing) {
        super(thing);
        disposed = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
        } else {
            Channel chn = getThing().getChannel(channelUID.getId());
            String strCh = null;
            if (chn != null) {
                strCh = chn.getAcceptedItemType();
                if (strCh != null && strCh.equals(DIMMER)) {
                    String DaliCommand = "";
                    if (command.toString().equals("OFF")) {
                        DaliCommand = "00";
                    } else if (command.toString().equals("ON")) {
                        DaliCommand = dimmervalue;
                    } else {
                        if (!command.toString().equals("0")) {
                            dimmervalue = String.format("%02X",
                                    Math.round(Integer.parseInt(command.toString()) * 2.54));
                            DaliCommand = dimmervalue;
                        } else {
                            DaliCommand = "00";
                        }
                    }
                    Configuration channelConfig = null;
                    channelConfig = chn.getConfiguration();
                    String SendingData = channelConfig.get("sequence").toString()
                            + channelConfig.get("address").toString() + DaliCommand;
                    sendData(SendingData);

                } else if (strCh != null && strCh.equals(SWITCH)) {
                    String DaliCommand = "";
                    if (command.equals(OnOffType.OFF)) {
                        DaliCommand = "00";
                    } else {
                        DaliCommand = "FE";
                    }
                    Configuration channelConfig = null;
                    channelConfig = chn.getConfiguration();
                    String SendingData = channelConfig.get("sequence").toString()
                            + channelConfig.get("address").toString() + DaliCommand;
                    sendData(SendingData);
                }
            }
        }
    }

    private void sendData(String sendingData) {
        Path outputFile = Paths.get("/dev/dali");
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            // try (PrintWriter p = new PrintWriter(new FileOutputStream("/dev/dali", true))) {
            writer.write(sendingData);
        } catch (IOException ex) {
            logger.warn("Error sending {}", ex.getLocalizedMessage());
        } finally {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        logger.warn("Sending {}", sendingData);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, "Initializing...");
        refresh = scheduler.scheduleWithFixedDelay(this::refresh, 1, 10, TimeUnit.SECONDS);
        // scanning = scheduler.schedule(this::scanDALI, 0, TimeUnit.SECONDS);
        scheduler.execute(this::scanDALI);
        updateStatus(ThingStatus.ONLINE);
    }

    private void refresh() {
        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID().getId())) {
                // channelConfig = null;
                Configuration channelConfig = channel.getConfiguration();
                int daliChannel = Integer.parseInt(channelConfig.get("address").toString(), 16);
                String data = channelConfig.get("sequence").toString() + String.format("%02X", (daliChannel + 1))
                        + "A0";
                sendData(data);
            }
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        while (!disposeout) {
            Thread.onSpinWait();
        }
        ScheduledFuture<?> refresh = this.refresh;
        if (refresh != null) {
            refresh.cancel(true);
        }
        ScheduledFuture<?> scanning = this.scanning;
        if (scanning != null) {
            scanning.cancel(true);
        }
        try {
            br.close();
        } catch (IOException e) {
        }
        super.dispose();
    }

    void scanDALI() {
        logger.debug("Start scanning");
        // Path path = Paths.get("/dev/dali");
        while (true) {
            // File path is passed as parameter
            File file = new File("/dev/dali");

            try {
                br = new BufferedReader(new FileReader(file));
                // Declaring a string variable
                String st;
                // Condition holds true till
                // there is character in a string
                while ((st = br.readLine()) != null) {
                    logger.debug("Receive: {}", st);
                    for (Channel channel : getThing().getChannels()) {
                        if (isLinked(channel.getUID().getId())) {
                            String seq = st.substring(0, 2);
                            String addr = st.substring(2, 4);
                            String val = st.substring(4, 6);
                            Configuration channelConfig = channel.getConfiguration();
                            String strCh = channel.getAcceptedItemType();
                            if (strCh != null && strCh.equals(DIMMER)) {
                                if (seq.equals(channelConfig.get("sequence"))) {
                                    try {
                                        int daliValue = Integer.parseInt(val, 16);
                                        int converted = (int) (daliValue / 2.54);
                                        updateState(channel.getUID().getId(),
                                                PercentType.valueOf(Integer.toString(converted)));
                                    } catch (Exception ex) {
                                        logger.debug("Warning: {}", ex.getLocalizedMessage());
                                    }
                                } else if ("ff".equals(seq) && addr.equals(channelConfig.get("address").toString())) {
                                    try {
                                        int daliValue = Integer.parseInt(val, 16);
                                        int converted = (int) (daliValue / 2.54);
                                        updateState(channel.getUID().getId(),
                                                PercentType.valueOf(Integer.toString(converted)));
                                    } catch (Exception ex) {
                                        logger.debug("Warning: {}", ex.getLocalizedMessage());
                                    }
                                }
                            } else if (strCh != null && strCh.equals(SWITCH)) {
                                if (seq.equals(channelConfig.get("sequence"))) {
                                    if (!val.equals("00")) {
                                        try {
                                            updateState(channel.getUID().getId(), OnOffType.ON);
                                        } catch (Exception ignored) {
                                        }
                                    } else {
                                        try {
                                            updateState(channel.getUID().getId(), OnOffType.OFF);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                } else if ("ff".equals(seq) && addr.equals(channelConfig.get("address").toString())) {
                                    if (val.equals("00")) {
                                        try {
                                            updateState(channel.getUID().getId(), OnOffType.OFF);
                                        } catch (Exception ex) {
                                            logger.debug("Warning: {}", ex.getLocalizedMessage());
                                        }
                                    } else {
                                        try {
                                            updateState(channel.getUID().getId(), OnOffType.ON);
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (disposed) {
                        disposeout = true;
                        break;
                    }
                }
            } catch (IOException e) {
                logger.debug("Error: {}", e.getMessage());
            }
            if (disposed) {
                disposeout = true;
                break;
            }
        }
    }
}
