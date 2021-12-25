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

/**
 * The {@link RPI4DaliConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Petr Shatsillo - Initial contribution
 */
@NonNullByDefault
public class RPI4DaliConfiguration {

    /**
     * Sample configuration parameters. Replace with your own.
     */
    public String address = "";
    public String sequence = "";
    public int refreshInterval;
}
