package io.domil.store.factory.addTaskFeature.model

data class UserTask(
    val startHour: Int = 0,
    val endHour: Int = 0,
    val startMinute: Int = 0,
    val endMinute: Int = 0,
    val product: Product = Product(),
    val task: String = "",
    val size: String = "",
    val number: Int = 0,
)
