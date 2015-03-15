[CommandServer](http://mercurial.selenic.com/wiki/CommandServer) implementation for [Mercurial](http://mercurial.selenic.com/). Requires Mercurial 2.2 to run

Meutrino is inspired by [python-hglib](http://mercurial.selenic.com/wiki/PythonHglib). At the moment it has all the features of hglib version 1.2

### Design goals ###
  * support [Scala](http://www.scala-lang.org/) developers
  * maintain in parallel a pure Java API with examples
  * no external dependencies except for Scala library
  * maintain high test coverage (at the moment all the tests for python-hglib pass)
  * support IDE developers if they decide to use Meutrino
  * contribute to python-hglib and Mercurial

### Dependencies ###
  * Mercurial 2.2
  * Java 6 or 7

### Run tests ###
```
hg clone https://code.google.com/p/meutrino/
cd meutrino
./sbt clean test
```