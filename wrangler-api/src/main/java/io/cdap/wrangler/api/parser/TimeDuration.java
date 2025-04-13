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
 * Token class representing a time duration with unit like ms, s, m, h.
 */
public class TimeDuration implements Token {
    private final double value;
    private final String unit;

    public TimeDuration(String value) {
        // super(TokenType.TIME_DURATION, value);
        value = value.trim().toLowerCase();

        int unitIndex = findFirstLetter(value);
        this.value = Double.parseDouble(value.substring(0, unitIndex));
        this.unit = value.substring(unitIndex);

        if (this.value < 0) {
            throw new IllegalArgumentException("Time duration can not be negative: " + value);
        }

        if (!unit.matches("(?i)(ms|s|m|h|d)")) {
            throw new IllegalArgumentException("Invalid time unit: " + unit);
        }
    }

    public long getMilliseconds() {
        switch (unit) {
            case "ms":
                return (long) value;
            case "s":
                return (long) (value * 1000);
            case "m":
                return (long) (value * 1000 * 60);
            case "h":
                return (long) (value * 1000 * 60 * 60);
            case "d":
                return (long) (value * 1000 * 60 * 60 * 24);
            default:
                throw new IllegalStateException("Unexpected unit: " + unit);
        }
    }

    @Override
    public Object value() {
        return getMilliseconds();
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getMilliseconds());
    }

    private int findFirstLetter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException("No unit found in TimeDuration: " + s);
    }
}
