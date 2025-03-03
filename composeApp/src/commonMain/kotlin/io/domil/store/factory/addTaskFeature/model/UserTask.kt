package io.domil.store.factory.addTaskFeature.model

data class UserTask(
    var startHour: Int = 0,
    var endHour: Int = 0,
    var startMinute: Int = 0,
    var endMinute: Int = 0,
    var product: Product = Product(),
    var task: String = "",
    var size: String = "",
    var number: Int = 0,
)
