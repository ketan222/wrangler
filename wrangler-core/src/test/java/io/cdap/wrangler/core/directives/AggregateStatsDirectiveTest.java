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

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.test.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsDirectiveTest {
    @Test
    public void testAggregation() throws Exception {
        List<Row> input = Arrays.asList(
            new Row().add("data_size", "1MB").add("response_time", "1s"),
            new Row().add("data_size", "2MB").add("response_time", "2s")
        );

        String[] recipe = {
            "aggregate-stats :data_size :response_time total_data total_time GB s total"
        };

        List<Row> output = TestingRig.execute(recipe, input);
        Assert.assertEquals(1, output.size());
        
        Row result = output.get(0);
        // 1MB + 2MB = 3MB = 0.0029296875 GB (1024-based)
        Assert.assertEquals(0.0029296875, result.getValue("total_data"), 0.0001);
        // 1s + 2s = 3s
        Assert.assertEquals(3.0, result.getValue("total_time"), 0.001);
    }
}
