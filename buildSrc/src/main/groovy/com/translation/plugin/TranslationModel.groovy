package com.translation.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer

class TranslationModel {
    public def sourcePath
    public def targetPath
    public def androidKeyColumn

//    public def column
    public def language

    TranslationModel(project) {
//        column = project.getObjects().newInstance(ColumnModel.class)
        language = project.container(ColumnModel.class)
    }

    // 方法名和 DSL 中的使用的配置块名称一致。
//    def column(Action<ColumnModel> action) {
//        action.execute(column)
//    }

    // 方法名和 DSL 中的使用的配置块名称一致。
    def language(Action<NamedDomainObjectContainer<ColumnModel>> action) {
        action.execute(language)
    }

}