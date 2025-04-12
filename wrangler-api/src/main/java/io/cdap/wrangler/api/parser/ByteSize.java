/*
 * Copyright © 2024 <Zeotap HarshilShukla>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Token class representing a byte size value with unit like KB, MB, GB, etc.
 */
public class ByteSize implements Token {
    private final double value;
    private final String unit;

    public ByteSize(String value) {
        // super(TokenType.BYTE_SIZE, value);
        value = value.trim().toUpperCase();

        int unitIndex = findFirstLetter(value);
        this.value = Double.parseDouble(value.substring(0, unitIndex));
        this.unit = value.substring(unitIndex);

        if (this.value < 0) {
            throw new IllegalArgumentException("Byte size can not be negative: " + value);
        }

        if (!unit.matches("(?i)(B|KB|MB|GB|TB)")) {
            throw new IllegalArgumentException("Invalid byte size unit: " + unit);
        }
    }

    public long getBytes() {
        switch (unit) {
            case "B":
                return (long) value;
            case "KB":
                return (long) (value * 1024);
            case "MB":
                return (long) (value * 1024 * 1024);
            case "GB":
                return (long) (value * 1024 * 1024 * 1024);
            case "TB":
                return (long) (value * 1024L * 1024 * 1024 * 1024);
            default:
                throw new IllegalStateException("Unexpected unit: " + unit);
        }
    }

    @Override
    public Object value() {
        return getBytes();
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getBytes());
    }

    private int findFirstLetter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException("No unit found in ByteSize: " + s);
    }
}
