package com.salizzai.app.util

import com.salizzai.app.data.model.FileTreeNode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * ZIP file extractor for Android — uses built-in java.util.zip.
 * No external libraries needed.
 */
object ZipExtractor {

    // Extensions considered "text" for content extraction
    private val TEXT_EXTENSIONS = setOf(
        "txt", "md", "markdown", "rst", "json", "yaml", "yml", "toml", "ini",
        "js", "jsx", "ts", "tsx", "mjs", "cjs",
        "py", "rb", "php", "go", "rs", "c", "h", "cpp", "cc", "hpp",
        "java", "kt", "swift", "cs", "vb",
        "html", "css", "scss", "less", "sass",
        "xml", "svg",
        "sh", "bash", "zsh", "fish",
        "sql", "graphql", "gql",
        "vue", "svelte", "astro",
        "env", "gitignore", "dockerignore", "editorconfig",
        "lock", "conf", "cfg",
        "csv", "tsv",
        "lua", "r", "dart", "scala", "clj", "el", "ex", "exs",
        "makefile", "mk"
    )

    private val SKIP_DIRS = setOf(
        "node_modules", ".git", ".next", ".nuxt", "dist", "build", "out",
        ".cache", "__pycache__", ".pytest_cache", ".venv", "venv", "env",
        ".idea", ".vscode", "coverage", ".turbo", ".svelte-kit"
    )

    private const val MAX_FILE_SIZE = 1024 * 1024 // 1MB per file
    private const val MAX_EXTRACTED_TEXT = 200 * 1024 // 200KB total

    data class ExtractionResult(
        val fileTree: List<FileTreeNode>,
        val extractedText: String,
        val fileCount: Int,
        val totalSize: Long,
        val topExtensions: Map<String, Int>,
        val summary: String
    )

    fun extract(zipBytes: ByteArray): ExtractionResult {
        val files = mutableListOf<FileInfo>()
        val textContents = mutableListOf<TextFile>()
        var totalSize = 0L
        var extractedTextSize = 0

        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    val path = entry.name

                    // Skip __MACOSX and noise
                    if (path.startsWith("__MACOSX/")) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }
                    // Skip files in unwanted directories
                    val parts = path.split("/")
                    if (parts.any { it in SKIP_DIRS }) {
                        zis.closeEntry()
                        entry = zis.nextEntry
                        continue
                    }

                    val size = entry.size.takeIf { it > 0 } ?: 0
                    files.add(FileInfo(path, size))
                    totalSize += size

                    // Extract text content for text files
                    val ext = getExtension(path)
                    if (isTextFile(path, ext) && size in 1..MAX_FILE_SIZE.toLong() && extractedTextSize < MAX_EXTRACTED_TEXT) {
                        try {
                            val content = zis.readBytes().toString(Charsets.UTF_8)
                            val truncated = if (content.length > 8000) {
                                content.substring(0, 8000) + "\n... [متوقف شد]"
                            } else content
                            textContents.add(TextFile(path, truncated, content.length.toLong()))
                            extractedTextSize += truncated.length
                        } catch (e: Exception) {
                            // skip unreadable
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        // Build file tree
        val tree = buildTree(files)

        // Top-level entries
        val topLevel = files.map { it.path.split("/").first() }.toSet()

        // Extensions stats
        val byExt = mutableMapOf<String, Int>()
        files.forEach { f ->
            val ext = getExtension(f.path)
            byExt[ext] = (byExt[ext] ?: 0) + 1
        }

        // Build extracted text with tree + file contents
        val sb = StringBuilder()
        sb.append("**خلاصه پروژه:**\n")
        sb.append("- تعداد فایل‌ها: ${files.size}\n")
        sb.append("- حجم کل: ${formatBytes(totalSize)}\n")
        sb.append("- پوشه‌های اصلی: ${topLevel.joinToString(", ")}\n")
        val topExts = byExt.entries.sortedByDescending { it.value }.take(10)
        if (topExts.isNotEmpty()) {
            sb.append("- پرتکرارترین پسوندها: ${topExts.joinToString(", ") { "${it.key}(${it.value})" }}\n")
        }
        sb.append("\n**ساختار فایل‌ها:**\n```\n")
        sb.append(renderTree(tree))
        sb.append("```\n\n")

        if (textContents.isNotEmpty()) {
            sb.append("**محتوای فایل‌های متنی (${textContents.size} فایل):**\n\n")
            textContents.forEach { tc ->
                sb.append("### `${tc.path}`\n\n```\n${tc.content}\n```\n\n")
            }
        }

        ExtractionResult(
            fileTree = tree,
            extractedText = sb.toString(),
            fileCount = files.size,
            totalSize = totalSize,
            topExtensions = byExt,
            summary = "خلاصه: ${files.size} فایل، ${formatBytes(totalSize)}"
        )
    }

    private data class FileInfo(val path: String, val size: Long)
    private data class TextFile(val path: String, val content: String, val size: Long)

    private fun getExtension(path: String): String {
        val name = path.substringAfterLast("/")
        val dotIdx = name.lastIndexOf('.')
        return if (dotIdx >= 0) name.substring(dotIdx).lowercase() else ""
    }

    private fun isTextFile(path: String, ext: String): Boolean {
        val cleanExt = ext.removePrefix(".").lowercase()
        if (cleanExt in TEXT_EXTENSIONS) return true
        val lowerName = path.substringAfterLast("/").lowercase()
        return setOf("readme", "license", "changelog", "contributing", "authors",
            "makefile", "dockerfile", "rakefile", "gemfile").any {
            lowerName == it || lowerName.startsWith("$it.")
        }
    }

    private fun buildTree(files: List<FileInfo>): List<FileTreeNode> {
        val root = mutableListOf<FileTreeNode>()
        val dirMap = mutableMapOf<String, MutableList<FileTreeNode>>()

        files.sortedBy { it.path }.forEach { file ->
            val parts = file.path.split("/")
            val path = StringBuilder()
            var currentLevel: MutableList<FileTreeNode>? = null
            var currentDirKey = ""

            for (i in parts.indices) {
                val part = parts[i]
                val isFile = i == parts.lastIndex
                if (path.isNotEmpty()) path.append("/")
                path.append(part)

                if (isFile) {
                    val ext = getExtension(file.path)
                    val node = FileTreeNode(
                        name = part,
                        path = path.toString(),
                        isDirectory = false,
                        size = file.size,
                        ext = ext
                    )
                    (currentLevel ?: root).add(node)
                } else {
                    currentDirKey = path.toString()
                    val existing = dirMap[currentDirKey]
                    if (existing == null) {
                        val newDir = FileTreeNode(
                            name = part,
                            path = path.toString(),
                            isDirectory = true,
                            size = 0,
                            children = mutableListOf()
                        )
                        (currentLevel ?: root).add(newDir)
                        val newChildren = newDir.children as MutableList
                        dirMap[currentDirKey] = newChildren
                        currentLevel = newChildren
                    } else {
                        currentLevel = existing
                    }
                }
            }
        }
        return root
    }

    private fun renderTree(nodes: List<FileTreeNode>, prefix: String = "", depth: Int = 0): String {
        if (depth > 5) return ""
        val sb = StringBuilder()
        nodes.forEachIndexed { idx, node ->
            val isLast = idx == nodes.lastIndex
            sb.append(prefix)
            sb.append(if (isLast) "└── " else "├── ")
            sb.append(node.name)
            if (node.isDirectory) sb.append("/")
            if (node.size > 0) sb.append(" (${formatBytes(node.size)})")
            sb.append("\n")
            if (node.isDirectory && node.children.isNotEmpty()) {
                sb.append(renderTree(node.children,
                    prefix + if (isLast) "    " else "│   ", depth + 1))
            }
        }
        return sb.toString()
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        if (bytes < 1024 * 1024) return "%.1f KB".format(bytes / 1024.0)
        return "%.1f MB".format(bytes / (1024.0 * 1024))
    }
}
