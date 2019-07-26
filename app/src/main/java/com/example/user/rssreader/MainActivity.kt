package com.example.user.rssreader

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.user.rssreader.adapter.ArticleAdapter
import com.example.user.rssreader.model.Article
import com.example.user.rssreader.model.Feed
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var articles: RecyclerView
    private lateinit var viewAdapter: ArticleAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val netDispatcher = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        Feed("cnn", "http://rss.cnn.com/rss/cnn_topstories.rss"),
        Feed("fox", "http://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv", "htt:myNewsFeed")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ArticleAdapter()
        articles = findViewById<RecyclerView>(R.id.articlesList)
            .apply {
                layoutManager = viewManager
                adapter = viewAdapter
            }

        asyncLoadNews()

    }

    private fun asyncLoadNews(dispatcher: CoroutineDispatcher = netDispatcher) =
        GlobalScope.launch(dispatcher) {

            val requests = mutableListOf<Deferred<List<Article>>>()
            feeds.mapTo(requests) {
                fetchArticlesAsync(it, dispatcher)
            }

            requests.forEach {
                it.join()
            }

            val articles = requests
                .filter { !it.isCancelled }
                .flatMap { it.getCompleted() }

            val failed = requests
                .filter { it.isCancelled }
                .size

            val obtained = requests.size - failed

            GlobalScope.launch(Dispatchers.Main) {
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                viewAdapter.add(articles)
            }
        }

    private fun fetchArticlesAsync(feed: Feed, dispatcher: CoroutineDispatcher) =
        GlobalScope.async(dispatcher) {

            delay(1000)

            val builder = factory.newDocumentBuilder()
            val xml = builder.parse(feed.url)
            val news = xml.getElementsByTagName("channel").item(0)

            (0 until news.childNodes.length)
                .asSequence()
                .map { news.childNodes.item(it) }
                .filter { Node.ELEMENT_NODE == it.nodeType }
                .map { it as Element }
                .filter { "item" == it.tagName }
                .map {
                    val title = it.getElementsByTagName("title").item(0).textContent
                    var summary = it.getElementsByTagName("description").item(0).textContent

                    if (!summary.startsWith("<div") && summary.contains("<div")) {
                        summary = summary.substring(0, summary.indexOf("<div"))
                    }

                    Article(feed.name, title, summary)
                }
                .toList()
        }
}
