package com.example.user.rssreader

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

@ObsoleteCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val netDispatcher = newSingleThreadContext(name = "ServiceCall")
    private val factory = DocumentBuilderFactory.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* 1. Synchronous function wrapped in an asynchronous caller.

        Pro: very explicit. Con: quite verbose.

        GlobalScope.launch(netDispatcher) {
            loadNews()
        } */

        /* 2. An asynchronous function with a predefined dispatcher.

        Pro: less verbose. Con: not as flexible, the caller can't decide where should it run (pro if that's needed).

        asyncLoadNews() */

        /* 3. An asynchronous function with a flexible dispatcher.

        Pro: less verbose and more flexible. Con: not as explicit, as option 2, depends on naming convention.*/

        asyncLoadNews(netDispatcher)

        // SUMMARY: Try to be flexible, explicit, safe and consistent.
    }

    // 1. Synchronous function wrapped in an asynchronous caller.

    private fun loadNews() {
        val headlines = fetchRssHeadlines()
        val newsCount = findViewById<TextView>(R.id.newsCount)
        GlobalScope.launch(Dispatchers.Main) {
            newsCount.text = "Found ${headlines.size} News"
        }
    }


    // 2. An asynchronous function with a predefined dispatcher.

    private fun asyncLoadNews() =
        GlobalScope.launch(netDispatcher) {
            val headlines = fetchRssHeadlines()
            val newsCount = findViewById<TextView>(R.id.newsCount)
            GlobalScope.launch(Dispatchers.Main) {
                newsCount.text = "Found ${headlines.size} News"
            }
        }

    // 3. An asynchronous function with a flexible dispatcher.

    private fun asyncLoadNews(dispatcher: CoroutineDispatcher = netDispatcher) =
        GlobalScope.launch(dispatcher) {
            val headlines = fetchRssHeadlines()
            val newsCount = findViewById<TextView>(R.id.newsCount)
            GlobalScope.launch(Dispatchers.Main) {
                newsCount.text = "Found ${headlines.size} News"
            }
        }

    private fun fetchRssHeadlines(): List<String> {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse("https://www.npr.org/rss/rss.php?id=1001")
        val news = xml.getElementsByTagName("channel").item(0)

        return (0 until news.childNodes.length)
            .asSequence()
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map { it as Element }
            .filter { "item" == it.tagName }
            .map {
                it.getElementsByTagName("title").item(0).textContent
            }
            .toList()
    }
}
