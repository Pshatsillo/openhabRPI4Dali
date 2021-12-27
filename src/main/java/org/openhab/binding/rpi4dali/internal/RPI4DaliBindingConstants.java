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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RPI4DaliBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class RPI4DaliBindingConstants {

    // List of all Channel ids
    public static final String DIMMER = "Dimmer";
    public static final String SWITCH = "Switch";
    private static final String BINDING_ID = "rpi4dali";
    // List of all Thing Type UIDs
    public static final ThingTypeUID DALI_BUS = new ThingTypeUID(BINDING_ID, "dalibus");
}
