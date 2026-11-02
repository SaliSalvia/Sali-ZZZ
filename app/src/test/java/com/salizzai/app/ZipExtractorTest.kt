package com.salizzai.app

import com.salizzai.app.util.ZipExtractor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Plain-JVM unit tests for the on-device ZIP extraction pipeline.
 * Build ZIP archives in memory and assert deterministic extraction results.
 */
class ZipExtractorTest {

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            entries.forEach { (path, content) ->
                val bytes = content.toByteArray(Charsets.UTF_8)
                val entry = ZipEntry(path)
                entry.size = bytes.size
                zos.putNextEntry(entry)
                zos.write(bytes)
                zos.closeEntry()
            }
        }
        return bos.toByteArray()
    }

    @Test
    fun countsFilesAndExtensions() {
        val bytes = zip(
            "project/README.md" to "# hello",
            "project/src/Main.kt" to "fun main() {}",
            "project/data/config.json" to "{}"
        )
        val result = ZipExtractor.extract(bytes)
        assertEquals(3, result.fileCount)
        assertEquals(3, result.topExtensions.values.sum())
        assertTrue(result.topExtensions.containsKey(".md"))
        assertTrue(result.topExtensions.containsKey(".kt"))
        assertTrue(result.topExtensions.containsKey(".json"))
    }

    @Test
    fun skipsNoiseEntries() {
        val bytes = zip(
            "__MACOSX/junk" to "noise",
            "project/node_modules/lib.js" to "skip me",
            "project/.git/config" to "skip me too",
            "project/keep.txt" to "keep"
        )
        val result = ZipExtractor.extract(bytes)
        assertEquals(1, result.fileCount)
        assertTrue(result.extractedText.contains("keep"))
    }

    @Test
    fun extractedTextContainsFileTreeAndContents() {
        val bytes = zip("project/README.md" to "# Documentation for the test")
        val result = ZipExtractor.extract(bytes)
        assertTrue(result.extractedText.contains("project/"))
        assertTrue(result.extractedText.contains("# Documentation for the test"))
    }

    @Test
    fun truncatesLongTextFiles() {
        val long = "a".repeat(20_000)
        val bytes = zip("project/big.md" to long)
        val result = ZipExtractor.extract(bytes)
        // Per-file text extraction is capped at 8000 chars plus a marker.
        assertTrue(result.extractedText.contains("[متوقف شد]"))
    }

    @Test
    fun formatsByteSizes() {
        assertEquals("512 B", ZipExtractor.formatBytes(512))
        assertEquals("1.0 KB", ZipExtractor.formatBytes(1024))
        assertEquals("1.5 MB", ZipExtractor.formatBytes((1.5 * 1024 * 1024).toLong()))
    }
}
