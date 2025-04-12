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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the {@link TimeDuration} class.
 * This class verifies the parsing and conversion of time duration strings into
 * their respective time values.
 */
public class TimeDurationTest {

    @Test
    public void testValidDurations() {
        Assert.assertEquals(5000L, new TimeDuration("5s").getMilliseconds());
        Assert.assertEquals(120000L, new TimeDuration("2m").getMilliseconds());
        Assert.assertEquals(7200000L, new TimeDuration("2h").getMilliseconds());
        Assert.assertEquals(172800000L, new TimeDuration("2d").getMilliseconds());
    }

    @Test
    public void testLowercaseUnits() {
        Assert.assertEquals(3000L, new TimeDuration("3s").getMilliseconds());
        Assert.assertEquals(180000L, new TimeDuration("3m").getMilliseconds());
    }

    @Test
    public void testMixedCaseUnits() {
        Assert.assertEquals(45000L, new TimeDuration("45S").getMilliseconds());
        Assert.assertEquals(3600000L, new TimeDuration("1H").getMilliseconds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnit() {
        new TimeDuration("10XY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoUnit() {
        new TimeDuration("100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() {
        new TimeDuration("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeValue() {
        new TimeDuration("-10s");
    }
}
