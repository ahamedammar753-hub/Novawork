package com.example

import com.example.ui.editors.spreadsheet.FormulaEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFormulaEngineBasicMath() {
        val mockData = mapOf(
            "A1" to "10",
            "A2" to "20",
            "A3" to "30",
            "B1" to "5"
        )
        val resolver: (String) -> String = { coord -> mockData[coord] ?: "" }

        // Test SUM
        assertEquals("60", FormulaEngine.evaluate("=SUM(A1:A3)", resolver))

        // Test AVERAGE
        assertEquals("20", FormulaEngine.evaluate("=AVERAGE(A1:A3)", resolver))

        // Test MIN / MAX
        assertEquals("10", FormulaEngine.evaluate("=MIN(A1:A3)", resolver))
        assertEquals("30", FormulaEngine.evaluate("=MAX(A1:A3)", resolver))

        // Test COUNT
        assertEquals("3", FormulaEngine.evaluate("=COUNT(A1:A3)", resolver))

        // Test IF
        assertEquals("YES", FormulaEngine.evaluate("=IF(A1>B1, YES, NO)", resolver))
        assertEquals("NO", FormulaEngine.evaluate("=IF(A1<B1, YES, NO)", resolver))
    }
}
