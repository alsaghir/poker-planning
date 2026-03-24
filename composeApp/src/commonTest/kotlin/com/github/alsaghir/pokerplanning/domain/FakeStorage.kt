import com.github.alsaghir.pokerplanning.domain.Storage

class FakeStorage : Storage {
    private val data = mutableMapOf<String, String>()

    override fun getString(key: String): String? = data[key]

    override suspend fun putString(key: String, value: String) {
        data[key] = value
    }

    override fun remove(key: String) {
        data.remove(key)
    }

    fun clear() {
        data.clear()
    }

}