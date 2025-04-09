package io.domil.store.factory.addTaskFeature.model

/**
 * Represents a user's task within a scheduling system.
 *
 * This data class encapsulates all the information related to a single task
 * assigned to a user, including its timing, associated product, task details,
 * and relevant codes.
 *
 * @property startHour The hour of the day (24-hour format) when the task is scheduled to begin. Defaults to 0.
 * @property endHour The hour of the day (24-hour format) when the task is scheduled to end. Defaults to 0.
 * @property startMinute The minute within the `startHour` when the task is scheduled to begin. Defaults to 0.
 * @property endMinute The minute within the `endHour` when the task is scheduled to end. Defaults to 0.
 * @property product The [Product] associated with this task.  Contains product-specific details. Defaults to an empty Product.
 * @property task A description of the task. Defaults to an empty string.
 * @property taskId A unique identifier for this task. Defaults to 0L.
 * @property size The size associated with the task or product (e.g., "small", "large"). Defaults to an empty string.
 * @property sizeCode A numerical code representing the size of the task or product.  Defaults to 0.
 * @property number A quantity or count associated with the task. Defaults to 0.
 * @property machineCode A numerical code identifying the machine used for the task. Defaults to 0.
 */
data class UserTask(
    val startHour: Int = 0,
    val endHour: Int = 0,
    val startMinute: Int = 0,
    val endMinute: Int = 0,
    val product: Product = Product(),
    val task: String = "",
    val taskId: Long = 0L,
    val size: String = "",
    val sizeCode : Int = 0,
    val number: Int = 0,
    val machineCode: Int = 0
)
