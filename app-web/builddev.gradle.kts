val kotlinDceOutputDir = file("${project.buildDir}/kotlin-js-min/main")
val artifactOutputDir = file("${project.buildDir}/out")
val zipArtifactOutputDir = file("${project.buildDir}/artifact")
val resFolder = file("${project.projectDir}/src/main/resources")
val releaseFileName = "uitor.js"
val releaseMinFileName = "uitor.min.js"

/**
 * Generate html list of dependencies from res folder into html header.
 */
fun generateHtmlDeps() : String {
    return resFolder
        .listFiles()
        .filter { it.name.endsWith(".js") || it.name.endsWith(".css") }
        .map { it.name }
        .sortedBy { it.substringAfterLast(".") }
        .joinToString("\n\t") { fileName ->
            when {
                fileName.endsWith(".js") -> "<script src=\"$fileName\"></script>"
                fileName.endsWith(".css") -> "<link rel=\"stylesheet\" href=\"$fileName\">"
                else -> ""
            }
        }
}

fun generateBuildMeta() : String {
    val dateFormat = java.text.SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
    val items = mapOf(
        "Build" to dateFormat.format(java.util.Date()),
        "Version" to project.version
    )
    return items.entries.joinToString("\n    ") { (k, v) ->
        "<meta name=\"$k\" content=\"$v\" />"
    }
}
/**
 * Task to go through all javascript files generated in DCE
 * And order them to load them based on dependencies between them and update the index_template.html
 */
val createDevIndexHtmlTask = task("createIndexHtml") {
    group = "custom build"
    doLast {
        val indexHtmlTemplate = file("index_template.html")
        //looks like webTask { outputFileName = "" } works only for distro, but development
        //doesn't care about this value and it's still named as project-module
        val exclude = "app-web.js"
        var text = indexHtmlTemplate.readText()
        text = text.replace("<!--%HEADER_DEPS%-->", generateHtmlDeps())
        text = text.replace("<!--%BUILD%-->", generateBuildMeta())
        //not used anymore, some additional stuff to load
        text = text.replace("<!--%SCRIPTS%-->", "")
        text += """
            <script>
                window.onload = function() {
                    let script = document.createElement('script');
                    script.src = "$exclude";
                    document.head.appendChild(script)
                };
            </script>
        """.trimIndent()
        file(resFolder.resolve("index.html")).apply {
            delete()
            writeText(text)
        }
    }
}

val assembleReleaseZipArtifactTask = tasks.create<Zip>("assembleReleaseZipArtifact") {
    group = "custom build"
    archiveFileName.set("uitor_webapp.zip")
    destinationDirectory.set(zipArtifactOutputDir)
    from(resFolder.absolutePath)
    from(artifactOutputDir.absolutePath) {
        exclude(releaseFileName)
    }
    dependsOn("browserProductionWebpack")
}

fun String.osCommandLineArgs(): List<String> {
    var cmd = this
    if (System.getProperty("os.name").contains("windows", true)) {
        cmd = "cmd /c $cmd"
    }
    return cmd.split(" ")
}