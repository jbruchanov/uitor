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
val createDevIndexHtmlTask = task("createDevIndexHtml") {
    group = "custom build"
    doLast {
        val indexHtmlTemplate = file("index_template.html")

        val depsOrdered = getOrderedDeps()
        val exclude = "uitor-app-web.js"
        var text = indexHtmlTemplate.readText()
        text = text.replace("<!--%HEADER_DEPS%-->", generateHtmlDeps())
        text = text.replace("<!--%BUILD%-->", generateBuildMeta())
        text = text.replace("<!--%SCRIPTS%-->",
            depsOrdered
                .filter { it.name != exclude }
                .joinToString("\n") {
                    val n = it.relativeTo(project.projectDir).toString().replace("\\", "/")
                    "<script src=\"$n\"></script>"
                }
        )
        val app = depsOrdered.last().relativeTo(project.projectDir).toString().replace("\\", "/")
        text += """
            <script>
                window.onload = function() {
                    let script = document.createElement('script');
                    script.src = "$app";
                    document.head.appendChild(script)
                };
            </script>
        """.trimIndent()
        file("index.html").apply {
            delete()
            writeText(text)
        }
    }
    dependsOn("runDceKotlin")
}

val generateSingleArtifact = task("createSingleArtifact") {
    group = "custom build"
    doLast {
        artifactOutputDir.mkdirs()
        val outputFile = File(artifactOutputDir, releaseFileName)
        if (outputFile.exists()) {
            check(outputFile.delete()) { "Unable to delete release file:${outputFile}" }
        }

        getOrderedDeps().forEach { f ->
            println(f.absolutePath)
            outputFile.appendText(f.readText())
        }
    }
    dependsOn(createDevIndexHtmlTask)
}

fun getOrderedDeps(): List<File> {
    val depRegEx = "require\\('(\\S*)'\\)".toRegex()
    val deps = mutableMapOf<File, MutableSet<String>>()
    val baseDir = file(kotlinDceOutputDir)
    if (!baseDir.exists()) {
        throw StopExecutionException("$baseDir doesn't exist!")
    }

    fileTree(baseDir).toList()
        .filter { it.name.endsWith(".js") }
        .forEach { f ->
            val content = f.readText().substringBefore("}")
            val list = mutableSetOf<String>()
            deps[f] = list
            depRegEx
                .findAll(content)
                .toList()
                .takeIf { it.isNotEmpty() }
                ?.forEach {
                    list.add(it.groupValues[1])
                }
        }

    val depsOrdered = mutableListOf<File>()
    while (deps.isNotEmpty()) {
        val (file, fileDeps) = deps.entries.first {
            it.value.isEmpty()
        }

        val moduleName = file.nameWithoutExtension
        depsOrdered.add(file)
        deps.remove(file)
        deps.forEach { (_, d) -> d.remove(moduleName) }
    }
    return depsOrdered
}

val installNpmTask = tasks.register<Exec>("installNpm") {
    group = "custom build"
    workingDir = project.projectDir
    commandLine = "npm install".osCommandLineArgs()
}

//FIXME: uglifyjs needs to be installed global
val uglifyjsReleaseArtifactTask = tasks.create<Exec>("uglifyjsReleaseArtifact") {
    group = "custom build"
    workingDir = project.projectDir
    val outputFile = File(artifactOutputDir, releaseFileName)
    val outputMinFile = File(artifactOutputDir, releaseMinFileName)
    //having "$outputMinFile" (qouted) full path doesn't work with npx (on mac)
    //will have probably issues with spaces in file
    val cmd = "npx uglifyjs -m -c -o $outputMinFile $outputFile"
    commandLine = cmd.osCommandLineArgs()
    dependsOn(installNpmTask, generateSingleArtifact)
}

//release html
val createReleaseIndexHtmlTask = task("createReleaseIndexHtml") {
    group = "custom build"
    doLast {
        val indexHtmlTemplate = file("index_template.html")
        val outputMinFile = File(artifactOutputDir, releaseMinFileName)
        var text = indexHtmlTemplate.readText()
        text = text.replace("<!--%HEADER_DEPS%-->", generateHtmlDeps())
        text = text.replace("<!--%BUILD%-->", generateBuildMeta())
        text = text.replace("<!--%SCRIPTS%-->", "")
        text += """
            <script>
                window.onload = function() {
                    let script = document.createElement('script');
                    script.src = "$releaseMinFileName";
                    document.head.appendChild(script)
                };
            </script>
        """.trimIndent()
        File(artifactOutputDir, "index.html").apply {
            delete()
            writeText(text)
        }
    }
    dependsOn(uglifyjsReleaseArtifactTask)
}

val assembleReleaseZipArtifactTask = tasks.create<Zip>("assembleReleaseZipArtifact") {
    group = "custom build"
    archiveFileName.set("uitor_webapp.zip")
    destinationDirectory.set(zipArtifactOutputDir)
    from(resFolder.absolutePath)
    from(artifactOutputDir.absolutePath) {
        exclude(releaseFileName)
    }
    dependsOn(createReleaseIndexHtmlTask)
}

fun String.osCommandLineArgs(): List<String> {
    var cmd = this
    if (System.getProperty("os.name").contains("windows", true)) {
        cmd = "cmd /c $cmd"
    }
    return cmd.split(" ")
}