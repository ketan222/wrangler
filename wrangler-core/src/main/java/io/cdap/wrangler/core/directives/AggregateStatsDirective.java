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

 package io.cdap.wrangler.core.directives;

 import io.cdap.cdap.api.annotation.Description;
 import io.cdap.cdap.api.annotation.Name;
 import io.cdap.cdap.api.annotation.Plugin;
 import io.cdap.wrangler.api.Directive;
 import io.cdap.wrangler.api.ExecutorContext;
 import io.cdap.wrangler.api.Row;
 import io.cdap.wrangler.api.DirectiveContext;
 import io.cdap.wrangler.api.DirectiveParseException;
 import io.cdap.wrangler.api.DirectiveExecutionException;
 import io.cdap.wrangler.api.TransientStore;
 import io.cdap.wrangler.api.annotations.Categories;
 import io.cdap.wrangler.api.parser.ByteSize;
 import io.cdap.wrangler.api.parser.Identifier;
 import io.cdap.wrangler.api.parser.Text;
 import io.cdap.wrangler.api.parser.TimeDuration;
 import io.cdap.wrangler.api.parser.Token;
 import io.cdap.wrangler.api.parser.TokenType;
 import io.cdap.wrangler.api.parser.UsageDefinition;
 
 import java.util.Collections;
 import java.util.List;
 
 @Plugin(type = Directive.TYPE)
 @Name("aggregate-stats")
 @Categories({"aggregate"})
 public class AggregateStatsDirective implements Directive {
     private String srcSizeCol;
     private String srcTimeCol;
     private String targetSizeCol;
     private String targetTimeCol;
     private String sizeUnit = "MB";
     private String timeUnit = "s";
     private String aggType = "total";
 
     @Override
     public UsageDefinition define() {
         return UsageDefinition.builder()
                 .use("Aggregates byte sizes and time durations")
                 .define("srcSizeCol", TokenType.COLUMN_NAME)
                 .define("srcTimeCol", TokenType.COLUMN_NAME)
                 .define("targetSizeCol", TokenType.COLUMN_NAME)
                 .define("targetTimeCol", TokenType.COLUMN_NAME)
                 .define("sizeUnit", TokenType.TEXT, io.cdap.wrangler.api.Optional.of("MB"))
                 .define("timeUnit", TokenType.TEXT, io.cdap.wrangler.api.Optional.of("s"))
                 .define("aggType", TokenType.TEXT, io.cdap.wrangler.api.Optional.of("total"))
                 .build();
     }
 
     @Override
     public void initialize(DirectiveContext context) throws DirectiveParseException {
         List<Token> tokens = context.getTokenGroup().getTokens();
         srcSizeCol = ((Identifier) tokens.get(0)).value();
         srcTimeCol = ((Identifier) tokens.get(1)).value();
         targetSizeCol = ((Identifier) tokens.get(2)).value();
         targetTimeCol = ((Identifier) tokens.get(3)).value();
         
         if(tokens.size() > 4) sizeUnit = ((Text) tokens.get(4)).value();
         if(tokens.size() > 5) timeUnit = ((Text) tokens.get(5)).value();
         if(tokens.size() > 6) aggType = ((Text) tokens.get(6)).value();
         
         validateUnits();
     }
 
     private void validateUnits() throws DirectiveParseException {
         try {
             new ByteSize("1" + sizeUnit);
         } catch (Exception e) {
             throw new DirectiveParseException("Invalid size unit: " + sizeUnit);
         }
         try {
             new TimeDuration("1" + timeUnit);
         } catch (Exception e) {
             throw new DirectiveParseException("Invalid time unit: " + timeUnit);
         }
     }
 
     @Override
     public List<Row> execute(List<Row> rows, ExecutorContext ctx) throws DirectiveExecutionException {
         TransientStore store = ctx.getTransientStore();
         
         for (Row row : rows) {
             processRow(row, store);
         }
         
         return Collections.emptyList();
     }
 
     @Override
     public void destroy() {
         // Cleanup if needed
     }
 
     private void processRow(Row row, TransientStore store) throws DirectiveExecutionException {
         try {
             // Process byte size
             String sizeVal = (String) row.getValue(srcSizeCol);
             ByteSize size = new ByteSize(sizeVal);
             store.increment("totalBytes", size.getBytes());
             
             // Process time duration
             String timeVal = (String) row.getValue(srcTimeCol);
             TimeDuration duration = new TimeDuration(timeVal);
             store.increment("totalNanos", duration.getNanoseconds());
             
             store.increment("rowCount", 1L);
         } catch (Exception e) {
             throw new DirectiveExecutionException(e.getMessage(), e);
         }
     }
 
     @Override
     public List<Row> finalize(ExecutorContext ctx) throws DirectiveExecutionException {
         TransientStore store = ctx.getTransientStore();
         long totalBytes = store.get("totalBytes", 0L);
         long totalNanos = store.get("totalNanos", 0L);
         long count = store.get("rowCount", 0L);
 
         // Convert to target units
         ByteSize sizeUnitObj = new ByteSize("1" + sizeUnit);
         TimeDuration timeUnitObj = new TimeDuration("1" + timeUnit);
         
         double finalSize = calculateAggregation(totalBytes, sizeUnitObj.getBytes(), count);
         double finalTime = calculateAggregation(totalNanos, timeUnitObj.getNanoseconds(), count);
 
         Row result = new Row();
         result.add(targetSizeCol, finalSize);
         result.add(targetTimeCol, finalTime);
         
         return Collections.singletonList(result);
     }
 
     private double calculateAggregation(long totalValue, long unitValue, long count) {
         if (aggType.equalsIgnoreCase("average")) {
             return (totalValue / (double) unitValue) / count;
         }
         return totalValue / (double) unitValue;
     }
 }