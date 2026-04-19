package com.igdtuw.studysync

data class Topic(
    var id: String = "",
    var name: String = "",
    var subject: String = "",
    var subjectId: String = "",
    var date: String = "",
    var status: String = "pending",   // pending / completed / missed
    var revise: Boolean = false,
    var time: String = ""
)
