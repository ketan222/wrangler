/*
 * Copyright © 2023 CDAP
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.core.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.TokenGroup;
import io.cdap.wrangler.parser.DirectivesBaseVisitor;
import io.cdap.wrangler.parser.DirectivesParser;

public class DirectiveParserVisitor extends DirectivesBaseVisitor<TokenGroup> {
    @Override
    public TokenGroup visitByteSizeValue(DirectivesParser.BYTE_SIZE ctx) {
        String text = ctx.BYTE_SIZE().getText();
        return new TokenGroup(new ByteSize(text));
    }

    @Override
    public TokenGroup visitTimeDurationValue(DirectivesParser.TIME_DURATION ctx) {
        String text = ctx.TIME_DURATION().getText();
        return new TokenGroup(new TimeDuration(text));
    }

    // Remove the existing visitValue() override
}