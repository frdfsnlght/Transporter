/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter.api;

/**
 * Represents a local gate of the BLOCK type.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public interface LocalBlockGate extends LocalGate {

    /**
     * Returns the name of the design this gate is based on.
     *
     * @return the name of the design
     */
    public String getDesignName();

    /**
     * Returns the value of the "restoreOnClose" option.
     *
     * @return  the option value
     */
    public boolean getRestoreOnClose();

    /**
     * Sets the "restoreOnClose" option.
     *
     * @param b     the option value
     */
    public void setRestoreOnClose(boolean b);

}
