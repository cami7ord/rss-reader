# rss-reader
Example of Kotlin's Coroutines in Android App.

Chapter 2: Try to be flexible, explicit, safe and consistent.

###
Waiting

`task.join()` : Handle the exception without having to propagate it.
`task.await()` : The exception will be propagated.

Chapter 3: 

| Fire & forget task  | Expect result |
| ------------- | ------------- |
| launch()  | async()  |
| Job()  | CompletableDeferred()  |

Deferred is intended to be monitored.


## Job lifecycle
![Image Job lifecycle](https://user-images.githubusercontent.com/1473318/61947547-bc972f00-afa5-11e9-912d-09a84782f335.png?v=4&s=200)

