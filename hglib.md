#relation to python-hglib library

# python-hglib #

Meutrino is inspired by [python-hglib](http://mercurial.selenic.com/wiki/PythonHglib). It tries to stay close to its API to simplify maitenance.

Some things are naturally different

  * python-hglib is written in Python, `Meutrino` is written in `Scala`
  * python-hglib is written in dynamically typed language, Meutrino is written in statically typed language
  * statically typed language dictates the same return type for methods and functions (`Meutrino` has a few methods with similar names when different return type is expected)

## Deviations ##

  * branch command always returns QBranch
  * branch command supports spaces in the name
  * repository instance (QRepository) cannot be re-opened, but there is no need to call open()
  * log command swaps 'files' and 'revRange' arguments because it is closer to how it is used in Mercurial
  * merge preview implemented [via revsets](http://stackoverflow.com/questions/7235315/is-there-a-way-to-get-the-equivalent-of-hg-merge-preview-with-hg-log-and-revse)
  * merge and resolve return boolean value (hg-lib in the docs says that it returns boolean but in fact they fail with an exception when merge does not succeed)
  * 'move' command copies only one file
  * python-hglib is not thread safe - if the same repository is called from different threads the communication is broken. `Meutrino` has optional basic synchronization. More concurrency will be introduced later.