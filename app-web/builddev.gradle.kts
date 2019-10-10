/**
 * Task to go through all javascript files generated in DCE
 * And order them to load them based on dependencies between them and update the index_template.html
 */
val createIndexHtmlTask = task("_createIndexHtml") {
    group = "build"
    doLast {
        val kotlinDceOutputDir = file("${project.buildDir}/kotlin-js-min/main")
        val indexHtmlTemplate = file("index_template.html")

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

        var text = indexHtmlTemplate.readText()
        text = text.replace("<!--%SCRIPTS%-->",
            depsOrdered.joinToString("\n") {
                val n = it.relativeTo(project.projectDir).toString().replace("\\", "/")
                "<script src=\"$n\"></script>"
            }
        )
        file("index.html").apply {
            delete()
            writeText(text)
        }
    }
}

createIndexHtmlTask.dependsOn("runDceKotlin")