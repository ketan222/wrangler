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
import com.google.gson.JsonPrimitive;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a byte size parser for wrangler transformations.
 */
public class ByteSize implements Token {
    private final long bytes;
    private final String original;

    public ByteSize(String input) {
        this.original = input.trim();
        String normalized = original.toUpperCase();

        // Improved regex pattern with group names
        Pattern pattern = Pattern.compile("^(?<value>\\d+(\\.\\d+)?)\\s*(?<unit>[A-Za-z]+)$");
        Matcher matcher = pattern.matcher(normalized);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid byte size format. Valid formats: '10B', '1.5KB', '2MB'. Received: " + input);
        }

        double value = Double.parseDouble(matcher.group("value"));
        String unit = matcher.group("unit").toUpperCase();

        switch (unit) {
            case "B":
                bytes = (long) value;
                break;
            case "KB":
                bytes = (long) (value * 1024);
                break;
            case "MB":
                bytes = (long) (value * 1024 * 1024);
                break;
            case "GB":
                bytes = (long) (value * 1024 * 1024 * 1024);
                break;
            case "TB":
                bytes = (long) (value * 1024L * 1024 * 1024 * 1024);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported byte unit: " + unit + ". Valid units: B, KB, MB, GB, TB");
        }

        if (bytes < 0) {
            throw new IllegalArgumentException("Byte size cannot be negative: " + input);
        }
    }

    // Rest of the class remains the same
    public long getBytes() {
        return this.bytes;
    }

    @Override
    public Object value() {
        return bytes;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(bytes);
    }
}
