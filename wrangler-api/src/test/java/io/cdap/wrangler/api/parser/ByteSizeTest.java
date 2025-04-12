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
 * Unit tests for the {@link ByteSize} class.
 * This class verifies the parsing and conversion of byte size strings into
 * their respective byte values.
 */
public class ByteSizeTest {

    @Test
    public void testValidSizes() {
        Assert.assertEquals(1024L, new ByteSize("1KB").getBytes());
        Assert.assertEquals(1572864L, new ByteSize("1.5MB").getBytes());
        Assert.assertEquals(1073741824L, new ByteSize("1GB").getBytes());
        Assert.assertEquals(1099511627776L, new ByteSize("1TB").getBytes());
        Assert.assertEquals(42L, new ByteSize("42B").getBytes());
    }

    @Test
    public void testLowercaseUnits() {
        Assert.assertEquals(10240L, new ByteSize("10kb").getBytes());
        Assert.assertEquals(15728640L, new ByteSize("15mb").getBytes());
        Assert.assertEquals(2147483648L, new ByteSize("2gb").getBytes());
    }

    @Test
    public void testMixedCaseUnits() {
        Assert.assertEquals(5120L, new ByteSize("5Kb").getBytes());
        Assert.assertEquals(10485760L, new ByteSize("10Mb").getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidUnit() {
        new ByteSize("10XY");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoUnit() {
        new ByteSize("100");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFormat() {
        new ByteSize("invalid");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeValue() {
        new ByteSize("-10KB");
    }
}
