/*
 *  Copyright © Zeotap Harshil Shukla.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.directives.transformation;

import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientStore;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.annotations.PublicEvolving;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive to aggregate byte size and time duration across rows.
 */

@PublicEvolving
public class AggregateSizeAndTime implements Directive {

    private String sourceSizeCol;
    private String sourceTimeCol;
    private String targetSizeCol;
    private String targetTimeCol;
    private String sizeUnit = "B"; // Default is bytes
    private String timeUnit = "ms"; // Default is milliseconds
    private String aggType = "total"; // or "average"

    @Override
    public UsageDefinition define() {
        // Ensure proper usage of UsageDefinition.Builder
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-size-and-time");
        builder.define("sourceSize", TokenType.COLUMN_NAME);
        builder.define("sourceTime", TokenType.COLUMN_NAME);
        builder.define("targetSize", TokenType.COLUMN_NAME);
        builder.define("targetTime", TokenType.COLUMN_NAME);
        builder.define("sizeUnit", TokenType.TEXT);
        builder.define("timeUnit", TokenType.TEXT);
        builder.define("aggType", TokenType.TEXT);
        return builder.build(); // Ensure build() returns a UsageDefinition object
    }

    @Override
    public void initialize(Arguments args) {
        sourceSizeCol = ((ColumnName) args.value("sourceSize")).value();
        sourceTimeCol = ((ColumnName) args.value("sourceTime")).value();
        targetSizeCol = ((ColumnName) args.value("targetSize")).value();
        targetTimeCol = ((ColumnName) args.value("targetTime")).value();

        if (args.contains("sizeUnit")) {
            sizeUnit = ((Text) args.value("sizeUnit")).value().toUpperCase();
        }
        if (args.contains("timeUnit")) {
            timeUnit = ((Text) args.value("timeUnit")).value().toLowerCase();
        }
        if (args.contains("aggType")) {
            aggType = ((Text) args.value("aggType")).value().toLowerCase();
        }
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext ctx) throws DirectiveExecutionException {
        TransientStore store = ctx.getTransientStore();
        List<Row> result = new java.util.ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            Object sizeObj = row.getValue(sourceSizeCol);
            Object timeObj = row.getValue(sourceTimeCol);

            long bytes = parseByteSize(sizeObj);
            long millis = parseTimeDuration(timeObj);

            long sizeTotal = store.get("sizeTotal") == null ? 0L : (long) store.get("sizeTotal");
            long timeTotal = store.get("timeTotal") == null ? 0L : (long) store.get("timeTotal");
            int count = store.get("count") == null ? 0 : (int) store.get("count");

            store.set(TransientVariableScope.GLOBAL, "sizeTotal", sizeTotal + bytes);
            store.set(TransientVariableScope.GLOBAL, "timeTotal", timeTotal + millis);
            store.set(TransientVariableScope.GLOBAL, "count", count + 1);

            // If it's the last row, emit the aggregate row
            if (i == rows.size() - 1) {
                long finalSize = sizeTotal + bytes;
                long finalTime = timeTotal + millis;
                int finalCount = count + 1;

                if ("average".equalsIgnoreCase(aggType) && finalCount > 0) {
                    finalSize = finalSize / finalCount;
                    finalTime = finalTime / finalCount;
                }

                Row aggregateRow = new Row();
                aggregateRow.add(targetSizeCol, convertSize(finalSize, sizeUnit));
                aggregateRow.add(targetTimeCol, convertTime(finalTime, timeUnit));

                result.add(aggregateRow);
            }
        }

        return result;
    }

    // @Override
    // public void finalize() throws DirectiveExecutionException {
    // TransientStore store = ctx.getTransientStore();

    // long sizeTotal = store.get("sizeTotal") == null ? 0L : (long)
    // store.get("sizeTotal");
    // long timeTotal = store.get("timeTotal") == null ? 0L : (long)
    // store.get("timeTotal");
    // int count = store.get("count") == null ? 0 : (int) store.get("count");

    // long finalSize = sizeTotal;
    // long finalTime = timeTotal;

    // if ("average".equalsIgnoreCase(aggType) && count > 0) {
    // finalSize = sizeTotal / count;
    // finalTime = timeTotal / count;
    // }

    // Row result = new Row();
    // result.add(targetSizeCol, convertSize(finalSize, sizeUnit));
    // result.add(targetTimeCol, convertTime(finalTime, timeUnit));

    // return List.of(result); //
    // }

    @Override
    public void destroy() {
        // No resources to clean up
    }

    private long parseByteSize(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return new ByteSize((String) value).getBytes();
        }
        return 0;
    }

    private long parseTimeDuration(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return new TimeDuration((String) value).getMilliseconds();
        }
        return 0;
    }

    private long convertSize(long bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "KB":
                return bytes / 1024;
            case "MB":
                return bytes / (1024 * 1024);
            case "GB":
                return bytes / (1024 * 1024 * 1024);
            default:
                return bytes;
        }
    }

    private long convertTime(long millis, String unit) {
        switch (unit.toLowerCase()) {
            case "s":
            case "sec":
            case "seconds":
                return millis / 1000;
            case "m":
            case "min":
            case "minutes":
                return millis / (60 * 1000);
            case "h":
            case "hr":
            case "hours":
                return millis / (60 * 60 * 1000);
            default:
                return millis;
        }
    }

    public AggregateSizeAndTime() {
        // required by Reflections for loading system directives
    }

}
