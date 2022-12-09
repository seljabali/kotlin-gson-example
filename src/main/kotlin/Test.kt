import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.FileReader
import java.io.FileWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val REVIEWS_FILENAME = "src/main/resources/reviews.json"
const val EMPLOYEES_FILENAME = "src/main/resources/employees.json"

fun main() {
    val gson = GsonBuilder().setPrettyPrinting().create()

    val employees = getEmployees(gson)
    val employeeReviewsMap = HashMap<String, SimpleEmployee>()
    for (employee in employees) {
        if (employee.ID.isBlank()) continue
        if (employee.EmployeeCode.isBlank()) continue
        if (employee.FirstName.isBlank()) continue
        if (employee.LastName.isBlank()) continue
        employeeReviewsMap[employee.EmployeeCode] = SimpleEmployee(
            qrCode = employee.EmployeeCode,
            name = employee.FirstName + " " + employee.LastName,
            phone = employee.PhoneNumber,
            email = employee.Email,
            reviews = emptyArray()
        )
    }

    val reviews = getReviews(gson)
    for (review in reviews) {
        if (review.ID.isBlank()) continue
        if (review.EmployeeCode.isBlank()) continue
        if (review.TransactionId.isBlank()) continue
        if (!employeeReviewsMap.containsKey(review.EmployeeCode)) continue
        val newReview = SimpleReview(
            transactionId = review.TransactionId,
            hashtags = review.HashTags ?: emptyArray(),
            rating = review.Rating,
            feedback = review.Feedback,
            date = LocalDateTime.ofInstant(Instant.ofEpochSecond(review.CreatedAt), ZoneId.of("UTC")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        val employee: SimpleEmployee = employeeReviewsMap[review.EmployeeCode]!!.apply {
            this.reviews = (this.reviews + newReview).sortedByDescending { it.date }.toTypedArray()
            val avgRating = this.reviews.sumOf { it.rating } / this.reviews.size.toDouble()
            val bd = BigDecimal(avgRating).setScale(2, RoundingMode.HALF_UP)
            this.averageRating = bd.toDouble()
        }
        employeeReviewsMap[employee.qrCode] = employee
    }
    val populatedReviews = employeeReviewsMap.values
        .filter { it.reviews.isNotEmpty() }
        .sortedByDescending { it.averageRating }
    println("Employee Count: " + populatedReviews.size + " of " + employees.size)
    println("Review Count: " + reviews.size)
    println("Employees with Reviews: " + populatedReviews.size)
    FileWriter("output.json").apply {
        write(gson.toJson(populatedReviews))
        close()
    }
}

fun getEmployees(gson: Gson): List<Employee> {
    val employeeListTypeToken = object : TypeToken<List<Employee>>() {}.type
    val reader = JsonReader(FileReader(EMPLOYEES_FILENAME))
    return gson.fromJson(reader, employeeListTypeToken)
}

fun getReviews(gson: Gson): List<Review> {
    val reviewListTypeToken = object : TypeToken<List<Review>>() {}.type
    val reader = JsonReader(FileReader(REVIEWS_FILENAME))
    return gson.fromJson(reader, reviewListTypeToken)
}

data class SimpleEmployee(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val qrCode: String = "",
    var averageRating: Double = 0.0,
    var reviews:  Array<SimpleReview> = emptyArray()
)

data class SimpleReview(
    val date: String,
    val rating: Int,
    val feedback: String,
    val hashtags: Array<String> = emptyArray(),
    val transactionId: String = ""
)