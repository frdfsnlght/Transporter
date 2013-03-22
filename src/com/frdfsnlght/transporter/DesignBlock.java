/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
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
package com.frdfsnlght.transporter;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class DesignBlock {

    private DesignBlockDetail detail;
    private int x, y, z;

    public DesignBlock(int x, int y, int z, DesignBlockDetail detail) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.detail = detail;
    }

    public DesignBlockDetail getDetail() {
        return detail;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("DesignBlock[");
        buf.append(x).append(",");
        buf.append(y).append(",");
        buf.append(z).append(":");
        buf.append(detail.toString());
        buf.append("]");
        return buf.toString();
    }

}
