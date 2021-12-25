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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
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

    private @Nullable RPI4DaliConfiguration config;

    public RPI4DaliHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (DIMMER.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }
            // Path outputFile = Paths.get("/dev/dali");
            // try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            // writer.write("230240");
            // } catch (IOException ex) {
            // }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(RPI4DaliConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::scanDALI);
    }

    void scanDALI() {
        BufferedReader br = null;
        try {
            InputStream initialStream = new FileInputStream("/dev/dali");
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
    }
}
