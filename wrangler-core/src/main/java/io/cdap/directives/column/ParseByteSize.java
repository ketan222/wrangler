/*
 *  Copyright © 2017-2019 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */


package io.cdap.directives.column;

import java.util.List;
/**
 * A directive for merging two columns and creates a third column.
 */
public class ParseByteSize {

    
    /**
     * Computes the sum of a list of integers.
     *
     * @param values List of integers
     * @return Sum of all integers
     */
    public static long computeSum(List<Integer> values) {
        long sum = 0;
        for (int val : values) {
            sum += val;
        }
        return sum;
    }

    /**
     * Computes the average of a list of integers.
     *
     * @param values List of integers
     * @return Average as double
     * @throws IllegalArgumentException if list is empty
     */
    public static double computeAverage(List<Integer> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Cannot compute average of empty list.");
        }

        long sum = computeSum(values);
        return (double) sum / values.size();
    }
}
