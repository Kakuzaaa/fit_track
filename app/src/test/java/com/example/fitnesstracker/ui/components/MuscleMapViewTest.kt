package com.example.fitnesstracker.ui.components

import org.junit.Assert.assertTrue
import org.junit.Test

class MuscleMapViewTest {

    @Test
    fun parseMuscleGroups_mapsPectoralsToChest() {
        val result = parseMuscleGroups(listOf("Большие грудные мышцы"))
        assertTrue(result.contains(MuscleGroup.CHEST))
    }

    @Test
    fun parseMuscleGroups_distinguishesBicepsAndHamstrings() {
        val resultArms = parseMuscleGroups(listOf("Двуглавая мышца плеча (бицепс)"))
        assertTrue(resultArms.contains(MuscleGroup.BICEPS))
        assertTrue(!resultArms.contains(MuscleGroup.HAMSTRINGS))

        val resultLegs = parseMuscleGroups(listOf("Бицепс бедра"))
        assertTrue(resultLegs.contains(MuscleGroup.HAMSTRINGS))
        assertTrue(!resultLegs.contains(MuscleGroup.BICEPS))
    }

    @Test
    fun parseMuscleGroups_mapsBackAndLats() {
        val result = parseMuscleGroups(listOf("Широчайшие мышцы спины", "Трапециевидные"))
        assertTrue(result.contains(MuscleGroup.LATS))
        assertTrue(result.contains(MuscleGroup.TRAPS))
    }

    @Test
    fun parseMuscleGroups_mapsAbsAndObliques() {
        val result = parseMuscleGroups(listOf("Прямая мышца живота (пресс)", "Косые мышцы"))
        assertTrue(result.contains(MuscleGroup.ABS))
        assertTrue(result.contains(MuscleGroup.OBLIQUES))
    }
}
