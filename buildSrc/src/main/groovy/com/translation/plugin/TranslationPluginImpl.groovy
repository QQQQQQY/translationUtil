package com.translation.plugin


import org.gradle.api.Plugin
import org.gradle.api.Project

class TranslationPluginImpl implements Plugin<Project> {

    def defaultTargetPath = "./translationTarget/" // 默认翻译文件输出目录

    def sourcePath
    def targetPath
    def androidKeyColumn = 2
// def iosKey = 2

    def columnModelList

    @Override
    void apply(Project project) {

        project.task('exeTranslation') {
            group "plugin"
            description '将 md 翻译文件 转换为 .xml 文件.'

            doLast {
                startTranslate()
            }
        }

        project.getExtensions().create('translation', TranslationModel.class, project)
        project.afterEvaluate {
            TranslationModel translationModel = project.getExtensions().getByType(TranslationModel.class)
            sourcePath = translationModel.sourcePath
            targetPath = translationModel.targetPath
            androidKeyColumn = translationModel.androidKeyColumn
            println("sourcePath -----> $sourcePath \n targetPath -----> $targetPath \n androidKeyColumn -----> $androidKeyColumn")
            if (null == sourcePath || sourcePath.isEmpty()) {
                throw GradleException("请先配置翻译文件的源路径 sourcePath !")
            }
            if (null == targetPath || targetPath.isEmpty()) {
                targetPath = defaultTargetPath
            }
            if (null == androidKeyColumn || androidKeyColumn < 0) {
                throw GradleException("请配置正确的 androidKeyColumn !")
            }
            columnModelList = translationModel.language
            println("language -----> $translationModel.language")
            if (null == columnModelList || columnModelList.isEmpty()) {
                throw GradleException("请先在 language{} 配置需要翻译语言的列号!")
            }
        }
    }


    def startTranslate() {
        def sourceFile = new File(sourcePath)
        if (!sourceFile.exists()) {
            throw GradleException("找不到源文件!请检查 sourcePath 配置!")
        }

        List<File> fileList = new ArrayList<>()
        List<String> languageColumnList = new ArrayList<>()

        columnModelList.each { columnModel ->
            def createTargetPath = targetPath + columnModel.name
            new File(createTargetPath).mkdirs() // 创建生成文件目录.
            def file = new File(createTargetPath + "\\\\strings.xml")
            println("创建 ${file.getPath()} 文件")
            file.withPrintWriter { it -> it.append("<resources>\n") }
            fileList.add(file)
            def columnNum = columnModel.columnNum
            if (columnNum < 0) {
                throw GradleException("columnNum 不能 < 0 ,请在 language{} 配置正确的列号!")
            }
            languageColumnList.add(columnNum)
        }

        def curLine = 1
        sourceFile.splitEachLine("\\|", "UTF-8") { lineContent ->
            if (curLine == 1) {
                println "第一行内容 -----> $lineContent"
            } else if (curLine == 2) {
                println "正在写入..."
            } else {  // 从第三行还是才是 翻译.前两行是 翻译标题 和 分割线.
                def key = lineContent[androidKeyColumn]
                fileList.eachWithIndex { File file, int index ->
                    def valueColumn = languageColumnList[index]
                    writeKeyValue2Xmls(file, key, lineContent[valueColumn])
                }
            }
            curLine++
        }
        fileList.each {
            writeEndTag2Xmls(it)
        }
        println("翻译文件转换完成,请到 $targetPath 中查看")
    }

    static def writeEndTag2Xmls(File file) {
        def resources = "</resources>\n"
        file.append(resources)
    }

    def writeKeyValue2Xmls(File file, String key, String value) {
        if (key != null) {
            key = key.trim().replaceAll("\\\\", "")
            if (key != "-"
                    && key != "") {
                value = value.trim()
                        .replaceAll("%d", "%s") // ios 占位符替换.
                        .replaceAll("%@", "%s")
                        .replaceAll("%ld", "%s")
                        .replaceAll("\\\\!", "!")
                        .replaceAll("\\\\'", "'") // 单引号转义
                        .replaceAll("'", "\\\\'") // 单引号转义
                        .replaceAll("\\\\\\\\n", "\\\\n") // 换行转义
                        .replaceAll("\\\\\\\\", "") // 去掉双反斜杠
                        .replaceAll("\\\$", "") // 去掉 $ 符号
                        .replaceAll(" ", "") // 去掉 \t 符号

                file.append("    <string name=\"" + key + "\">" + value + "</string>\n", "UTF-8")
            }
        } else {
            println "写入 key-value 出现错误 : $curLine 行"
        }
    }
}
