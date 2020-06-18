ToDo
====

* Allow for dependencies health
  * Database
  * External service
* Allow for capabilities health
  * Function of dependencies
* Allow for arbitrary extra data (either a map, or a function returning a map)
  to be included on the response  
* Make checks asynchronous
* Add factories for dependencies of different types.

Each health check should have a name identifying the dependency, and a check 
function. The check function should return a map including a `:healthy` flag.
Any other data returned in the map will be included in the health response.

`{:name :database, :check-fn (fn [dependencies] ...)}`