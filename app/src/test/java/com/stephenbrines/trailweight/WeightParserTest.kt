package com.stephenbrines.trailweight

import com.stephenbrines.trailweight.service.WeightParser
import org.junit.Assert.*
import org.junit.Test

class WeightParserTest {

    @Test fun `grams only`() {
        assertEquals(119.0, WeightParser.parseToGrams("119g")!!, 0.01)
        assertEquals(119.0, WeightParser.parseToGrams("119 g")!!, 0.01)
        assertEquals(539.0, WeightParser.parseToGrams("539 grams")!!, 0.01)
    }

    @Test fun `ounces only`() {
        assertEquals(4 * 28.3495, WeightParser.parseToGrams("4 oz")!!, 0.01)
        assertEquals(4.2 * 28.3495, WeightParser.parseToGrams("4.2 oz")!!, 0.01)
        assertEquals(4 * 28.3495, WeightParser.parseToGrams("4 ounces")!!, 0.01)
    }

    @Test fun `pounds only`() {
        assertEquals(0.26 * 453.592, WeightParser.parseToGrams("0.26 lbs")!!, 0.01)
        assertEquals(453.592, WeightParser.parseToGrams("1 lb")!!, 0.01)
    }

    @Test fun `pounds and ounces`() {
        assertEquals(453.592 + 4 * 28.3495, WeightParser.parseToGrams("1 lb 4 oz")!!, 0.1)
        assertEquals(3 * 453.592 + 14 * 28.3495, WeightParser.parseToGrams("3 lbs. 14 oz.")!!, 0.1)
    }

    @Test fun `dual format prefers grams`() {
        assertEquals(113.0, WeightParser.parseToGrams("4 oz / 113g")!!, 0.01)
        assertEquals(539.0, WeightParser.parseToGrams("19.0 oz (539 g)")!!, 0.01)
        assertEquals(113.0, WeightParser.parseToGrams("4 oz (113 g)")!!, 0.01)
    }

    @Test fun `nil for unparseable`() {
        assertNull(WeightParser.parseToGrams("not a weight"))
        assertNull(WeightParser.parseToGrams(""))
    }

    @Test fun `display string format`() {
        // Under 1 oz — show grams
        assertTrue(WeightParser.displayString(10.0).endsWith("g"))
        // 1 oz – 1 lb range — show oz and grams
        assertTrue(WeightParser.displayString(200.0).contains("oz"))
        // 1 lb+ — show lbs
        assertTrue(WeightParser.displayString(500.0).contains("lbs"))
    }
}
