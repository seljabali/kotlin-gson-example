data class Review(
    val HashTags: Array<String>? = emptyArray(),
    val TransactionId: String = "",
    val ShopCode: String = "",
    val ID: String = "",
    val Rating: Int,
    val ModifiedAt: Int,
    val CreatedAt: Long,
    val EmployeeCode: String = "",
    val RoomId: String = "",
    val Feedback: String = ""
)
