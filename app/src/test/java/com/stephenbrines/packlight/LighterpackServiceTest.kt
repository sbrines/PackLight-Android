package com.stephenbrines.packlight

import com.stephenbrines.packlight.service.LighterpackResult
import com.stephenbrines.packlight.service.LighterpackService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LighterpackServiceTest {

    private lateinit var service: LighterpackService

    @Before
    fun setUp() {
        service = LighterpackService()
    }

    @Test
    fun `import parses standard lighterpack CSV`() {
        val csv = """
            Item Name,Category,desc,qty,weight,unit,url,price,worn,consumable
            Tarptent Stratospire Li,Shelter,,1,680,g,https://tarptent.com,0,0,0
            Enlightened Equipment Revelation,Sleep,,1,450,g,,0,0,0
            Trail runners,Footwear,,1,285,g,,0,1,0
        """.trimIndent()

        val result = service.import_(csv) as LighterpackResult.Success
        assertEquals(3, result.rows.size)
        assertEquals("Tarptent Stratospire Li", result.rows[0].name)
        assertEquals(680.0, result.rows[0].weightGrams, 0.01)
        assertEquals("Shelter", result.rows[0].category)
        assertFalse(result.rows[0].consumable)
        assertTrue(result.rows[2].worn)
    }

    @Test
    fun `import converts oz weights to grams`() {
        val csv = """
            Item Name,Category,desc,qty,weight,unit,url,price,worn,consumable
            Tent Stakes,Other,,6,0.4,oz,,0,0,0
        """.trimIndent()

        val result = service.import_(csv) as LighterpackResult.Success
        assertEquals(1, result.rows.size)
        assertEquals(0.4 * 28.3495, result.rows[0].weightGrams, 0.1)
    }

    @Test
    fun `import handles missing header row`() {
        val csv = "Puffy Jacket,Clothing,,1,285,g,,0,1,0"
        val result = service.import_(csv) as LighterpackResult.Success
        assertEquals(1, result.rows.size)
        assertEquals("Puffy Jacket", result.rows[0].name)
    }

    @Test
    fun `import handles quoted fields with commas`() {
        val csv = """
            Item Name,Category,desc,qty,weight,unit,url,price,worn,consumable
            "Knife, fork, spoon set",Cooking,"Multi-use set",1,45,g,,0,0,0
        """.trimIndent()

        val result = service.import_(csv) as LighterpackResult.Success
        assertEquals("Knife, fork, spoon set", result.rows[0].name)
        assertEquals("Multi-use set", result.rows[0].description)
    }

    @Test
    fun `export produces valid CSV`() {
        val items = listOf(
            com.stephenbrines.packlight.data.model.GearItem(
                name = "Test Tent",
                category = com.stephenbrines.packlight.data.model.GearCategory.SHELTER,
                weightGrams = 700.0,
                quantityOwned = 1,
            )
        )
        val csv = service.exportGearItems(items)
        assertTrue(csv.startsWith("Item Name,Category"))
        assertTrue(csv.contains("Test Tent"))
        assertTrue(csv.contains("700.00"))
        assertTrue(csv.contains(",g,"))
    }

    @Test
    fun `round-trip export then import preserves data`() {
        val items = listOf(
            com.stephenbrines.packlight.data.model.GearItem(
                name = "Rain Jacket",
                category = com.stephenbrines.packlight.data.model.GearCategory.CLOTHING,
                weightGrams = 320.0,
                quantityOwned = 1,
                isConsumable = false,
                notes = "My backup jacket",
            )
        )
        val csv = service.exportGearItems(items)
        val result = service.import_(csv) as LighterpackResult.Success
        assertEquals(1, result.rows.size)
        assertEquals("Rain Jacket", result.rows[0].name)
        assertEquals(320.0, result.rows[0].weightGrams, 0.01)
    }

    @Test
    fun `empty file returns error`() {
        val result = service.import_("")
        assertTrue(result is LighterpackResult.Error)
    }
}
