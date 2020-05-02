package io.dairyly.dairyly

import io.dairyly.dairyly.data.DiarylyGenerator
import io.dairyly.dairyly.utils.DATA_GEN_OPTION
import junit.framework.TestCase.assertEquals
import net.andreinc.mockneat.MockNeat
import org.junit.Test

class DiarylyGeneratorTest {

    private val totalUsers = DATA_GEN_OPTION["totalUsers"]!!
    private val totalFiles = DATA_GEN_OPTION["totalFiles"]!!
    private val totalEntryBlocks = DATA_GEN_OPTION["totalEntryBlocks"]!!
    private val totalEntries = DATA_GEN_OPTION["totalEntries"]!!
    private val totalTags = DATA_GEN_OPTION["totalTags"]!!

    private val gen = DiarylyGenerator(MockNeat.threadLocal(), totalUsers, totalFiles, totalEntryBlocks, totalEntries, totalTags)

    @Test
    fun getUsers() {
        println(gen.users)
        assertEquals(gen.totalUsers, gen.users.size)
    }

    @Test
    fun getFiles() {
        print(gen.files)
        assertEquals(gen.totalFiles, gen.files.size)
    }

    @Test
    fun getEntryBlocks() {
        print(gen.entryBlocks)
        assertEquals(gen.totalEntryBlocks, gen.entryBlocks.size)
    }

    @Test
    fun getEntries() {
        print(gen.entries)
        assertEquals(gen.totalEntries, gen.entries.size)
    }

    @Test
    fun getTags() {
        print(gen.tags)
        assertEquals(gen.totalTags, gen.tags.size)
    }
}