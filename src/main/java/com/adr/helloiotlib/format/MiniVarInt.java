//    HelloIoT is a dashboard creator for MQTT
//    Copyright (C) 2018-2019 Adrián Romero Corchado.
//
//    This file is part of HelloIot.
//
//    HelloIot is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    HelloIot is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with HelloIot.  If not, see <http://www.gnu.org/licenses/>.
//
package com.adr.helloiotlib.format;

/**
 *
 * @author adrian
 */
public class MiniVarInt implements MiniVar {

    public final static MiniVar NULL = new MiniVarInt(null);
    
    public final Integer value;
    
    public MiniVarInt(Integer value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value == null ? "" : value.toString();
    }

    @Override
    public double asDouble() {
        return value == null ? 0.0 : value.doubleValue();
    }
    
    @Override
    public int asInt() {
        return value == null ? 0 : value;
    }    
    
    @Override
    public long asLong() {
         return value == null ? 0L : value.longValue();
    }

    @Override
    public boolean asBoolean() {
        return value == null ? false : value != 0;
    }

    @Override
    public byte[] asBytes() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    @Override
    public boolean isEmpty() {
        return value == null;
    }        
}
