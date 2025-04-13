/*
 * Copyright © 2025 Cask Data, Inc.
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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a time duration parser for wrangler transformations.
 */

public class TimeDuration implements Token {
    private final String original;
    private final long nanoseconds;
    private static final Pattern DURATION_PATTERN = Pattern
            .compile("^(?<value>-?\\d+(\\.\\d+)?)\\s*(?<unit>ns|μs|ms|s|m|h|d)$", Pattern.CASE_INSENSITIVE);

    public TimeDuration(String input) {
        this.original = input.trim();

        Matcher matcher = DURATION_PATTERN.matcher(original);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid time duration format. Valid examples: '100ms', '1.5h', '2d'. Received: " + input);
        }

        try {
            BigDecimal value = new BigDecimal(matcher.group("value"));
            String unit = matcher.group("unit").toLowerCase();

            if (value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Time duration cannot be negative: " + input);
            }

            switch (unit) {
                case "ns":
                    nanoseconds = value.longValue();
                    break;
                case "μs":
                    nanoseconds = value.multiply(BigDecimal.valueOf(1_000L)).longValue();
                    break;
                case "ms":
                    nanoseconds = value.multiply(BigDecimal.valueOf(1_000_000L)).longValue();
                    break;
                case "s":
                    nanoseconds = value.multiply(BigDecimal.valueOf(1_000_000_000L)).longValue();
                    break;
                case "m":
                    nanoseconds = value.multiply(BigDecimal.valueOf(60L * 1_000_000_000L)).longValue();
                    break;
                case "h":
                    nanoseconds = value.multiply(BigDecimal.valueOf(3600L * 1_000_000_000L)).longValue();
                    break;
                case "d":
                    nanoseconds = value.multiply(BigDecimal.valueOf(86400L * 1_000_000_000L)).longValue();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported time unit: " + unit);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value in duration: " + input, e);
        }
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public double getSeconds() {
        return nanoseconds / 1_000_000_000.0;
    }

    @Override
    public Object value() {
        return nanoseconds;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("original", original);
        obj.addProperty("nanoseconds", nanoseconds);
        obj.addProperty("human_readable", toString());
        return obj;
    }

    @Override
    public String toString() {
        if (nanoseconds < 1_000) {
            return nanoseconds + "ns";
        } else if (nanoseconds < 1_000_000) {
            return String.format("%.1fμs", nanoseconds / 1_000.0);
        } else if (nanoseconds < 1_000_000_000) {
            return String.format("%.1fms", nanoseconds / 1_000_000.0);
        } else if (nanoseconds < 60_000_000_000L) {
            return String.format("%.1fs", nanoseconds / 1_000_000_000.0);
        } else if (nanoseconds < 3_600_000_000_000L) {
            return String.format("%.1fm", nanoseconds / 60_000_000_000.0);
        } else if (nanoseconds < 86_400_000_000_000L) {
            return String.format("%.1fh", nanoseconds / 3_600_000_000_000.0);
        } else {
            return String.format("%.1fd", nanoseconds / 86_400_000_000_000.0);
        }
    }
}
