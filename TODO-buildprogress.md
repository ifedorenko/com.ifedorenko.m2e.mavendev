## IMMEDIATE

* only show inprogress projects. again, having 100x of waiting/completed projects pollutes the view without adding much/any useful information

## FUTURE

* project/execution times
* deal with multiple cuncurrent launches
  * launch can be pinned explicitly
  * eclipse can be configured to keep old launches
  * can actually run multiple builds concurrently
* exception handling (printStackTrace isn't it)
* consider moving model to eventspy bundle, such that it can be serialized/deserialized with gson
  * alternatively, introduce message attribute name constants to share between UI and EventSpy
* expand in-progress project subtrees, collapse after project build completion
