ackage com.example.shtrih2

import android.app.Application

class App : Application() {
    val database = HashMap<String, String>()
    val history = ArrayList<HistoryItem>()
}
