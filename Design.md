#design goals

# Design Goals for the project #

  * stay close to python-hglib where it is possible
  * pure Scala implementation with Java examples in [a separate project](http://code.google.com/p/meutrino/source/checkout?repo=javaexample)
  * immutable data structures.. There are 2 exceptions: Java API requires Java mutable collections. There is one IO method with mutable state.
  * Scala version: 2.9.0+. The version 2.8 is not supported because of the the missing `JavaConverters`. Since nothing else is used from 2.9, the former 2.8 version may be applied with minimum changes in the source code.
  * no external dependencies except for the Scala library


### Immutability ###

Consequences:
  * no vars
  * no cycles
  * no mutable lists or maps
  * no co-, contra- variance. Everything is naturally covariant.