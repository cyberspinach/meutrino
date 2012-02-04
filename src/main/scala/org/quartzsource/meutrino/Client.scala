/**
 * Copyright (c) 2012, www.quartzsource.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quartzsource.meutrino

import java.io.File
import java.io.InputStream
import java.util.Date

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.seqAsJavaListConverter

trait QFactory {

  def create(path: File): QRepository
  def open(path: File): QRepository
  def clone(source: String, path: File, noupdate: Boolean = false,
    uncompressed: Boolean = false): QRepository

}

trait QRepository {
  /**
   * Add the specified files on the next commit.
   * If no files are given, add all files to the repository.
   *
   * Return whether all given files were added.
   */
  def add(files: List[QPath]): Boolean

  /**
   * Add all new files and remove all missing files from the repository.
   *
   * New files are ignored if they match any of the patterns in ".hgignore". As
   * with add, these changes take effect at the next commit.
   *
   * similarity - used to detect renamed files. With a parameter
   * greater than 0, this compares every removed file with every added file and
   * records those similar enough as renames. This option takes a percentage
   * between 0 (disabled) and 100 (files must be identical) as its parameter.
   * Detecting renamed files this way can be expensive. After using this
   * option, "hg status -C" can be used to check which files were identified as
   * moved or renamed.
   */
  def addRemove(files: List[QPath], similarity: Int = 100): Unit

  /**
   * Show changeset information by line for each file in files.
   *
   * rev - annotate the specified revision
   *
   * Yields a (revision, contents) tuple for each line in a file.
   */
  def annotate(file: QPath, rev: Option[Int] = None): List[(Int, String)]

  /**
   * Set a bookmark on the working directory's parent revision or rev,
   * with the given name.
   *
   * name - bookmark name
   * rev - revision to bookmark
   * force - bookmark even if another bookmark with the same name exists
   * delete - delete the given bookmark
   * inactive - do not mark the new bookmark active
   * rename - rename the bookmark given by rename to name
   */
  def bookmark(name: String, rev: Option[Int] = None, force: Boolean = false, delete: Boolean = false,
    inactive: Boolean = false, rename: Option[String] = None): Unit

  /**
   * Return the bookmarks as a list of (bookmark, rev, node).
   */
  def bookmarks(): List[(QBookmark, Int, QNodeId)]

  /**
   * When name isn't given, return the current branch name. Otherwise set the
   * working directory branch name (the branch will not exist in the repository
   * until the next commit). Standard practice recommends that primary
   * development take place on the 'default' branch.
   *
   * When clean is True, reset and return the working directory branch to that
   * of the parent of the working directory, negating a previous branch change.
   *
   * name - new branch name
   * clean - reset branch name to parent branch name
   * force - set branch name even if it shadows an existing branch
   */
  def branch(name: Option[String] = None, force: Boolean = false, clean: Boolean = false): QBranch

  /**
   * Returns the repository's named branches as a list of (branch, rev, node).
   *
   * active - show only branches that have unmerged heads
   * closed - show normal and closed branches
   */
  def branches(active: Boolean = false, closed: Boolean = false): List[(QBranch, Int, QNodeId)]

  /**
   * Generate a compressed changegroup file collecting changesets not known to
   * be in another repository.
   *
   * If destrepo isn't given, then hg assumes the destination will have all
   * the nodes you specify with base. To create a bundle containing all
   * changesets, use all (or set base to 'null').
   *
   * file - destination file name
   * destrepo - repository to look for changes
   */
  def bundle(file: String, destRepo: Option[String]): Boolean

  /**
   * Return a string containing the specified file as it was at the
   * given revision. If no revision is given, the parent of the working
   * directory is used, or tip if no revision is checked out.
   */
  def cat(file: QPath, revision: Option[QNodeId] = None): String

  /**
   * Create a copy of an existing repository specified by source in a new
   * directory dest.
   *
   * If dest isn't specified, it defaults to the basename of source.
   *
   * branch - clone only the specified branch
   * updaterev - revision, tag or branch to check out
   */
  def clone(source: String = ".", dest: Option[String] = None, branch: Option[String] = None,
    updateRev: Option[String] = None): Unit

  /**
   * Commit changes reported by status into the repository.
   *
   * message - the commit message
   * closebranch - mark a branch as closed, hiding it from the branch list
   * date - record the specified date as commit date
   * user - record the specified user as committer
   */
  def commit(message: String, user: Option[String] = None, logfile: Option[String] = None, addRemove: Boolean = false,
    closeBranch: Boolean = false, date: Option[Date] = None): (Int, QNodeId)

  /**
   * Return List(config files), List(source, section, key, value) where
   * source is of the form filename:[line]
   */
  def config(): (List[String], List[(Option[String], String, String, String)])

  /**
   * Mark dest as having copy of source file. If dest is a directory, copy
   * is put in that directory.
   *
   * Returns True on success, False if errors are encountered.
   *
   * source - a file
   * dest - a destination file or directory
   * after - record a copy that has already occurred
   * force - forcibly copy over an existing managed file
   */
  def copy(source: QPath, dest: QPath, after: Boolean = true, force: Boolean = false): Boolean

  /**
   * Return differences between revisions for the specified files.
   *
   * revs - a revision or a list of two revisions to diff
   * change - change made by revision
   * text - treat all files as text
   * git - use git extended diff format
   * nodates - omit dates from diff headers
   * showfunction - show which function each change is in
   * reverse - produce a diff that undoes the changes
   * ignoreallspace - ignore white space when comparing lines
   * ignorespacechange - ignore changes in the amount of white space
   * ignoreblanklines - ignore changes whose lines are all blank
   * unified - number of lines of context to show
   * stat - output diffstat-style summary of changes
   * subrepos - recurse into subrepositories
   */
  def diff(files: List[QPath] = Nil, revs: List[QNodeId] = Nil, change: Option[QNodeId] = None,
    text: Boolean = false, git: Boolean = false, noDates: Boolean = false, showFunction: Boolean = false,
    reverse: Boolean = false, ignoreAllSpace: Boolean = false, ignoreSpaceChange: Boolean = false,
    ignoreBlankLines: Boolean = false, unified: Option[Int] = None, stat: Boolean = false,
    subrepos: Boolean = false): String

  /**
   * Mark the specified files so they will no longer be tracked after the next
   * commit.
   *
   * This only removes files from the current branch, not from the entire
   * project history, and it does not delete them from the working directory.
   *
   * Returns True on success.
   */
  def forget(files: List[QPath]): Boolean

  //TODO grep command is not yet implemented.
  def grep(pattern: String): List[List[String]]

  /**
   * Return a list of current repository heads or branch heads.
   *
   * revs - return only branch heads on the branches associated with the specified
   * changesets.
   *
   * startRev - return only heads which are descendants of the given rev.
   * topological - named branch mechanics will be ignored and only changesets
   * without children will be shown.
   *
   * closed - normal and closed branch heads.
   */
  def heads(revs: List[QNodeId] = Nil, startRev: Option[QNodeId] = None,
    topological: Boolean = false, closed: Boolean = false): List[QRevision]

  /**
   * Import the specified patch and commit it  (unless nocommit is
   * specified).
   *
   * strip - directory strip option for patch. This has the same meaning as the
   * corresponding patch option (default: 1)
   *
   * force - skip check for outstanding uncommitted changes
   * nocommit - don't commit, just update the working directory
   * bypass - apply patch without touching the working directory
   * exact - apply patch to the nodes from which it was generated
   * importbranch - use any branch information in patch (implied by exact)
   * message - the commit message
   * date - record the specified date as commit date
   * user - record the specified user as committer
   * similarity - guess renamed files by similarity (0<=s<=100)
   */
  def import_(patch: InputStream, strip: Option[Int] = None, force: Boolean = false,
    noCommit: Boolean = false, byPass: Boolean = false, exact: Boolean = false,
    importBranch: Boolean = false, message: Option[String] = None, date: Option[Date] = None,
    user: Option[String] = None, similarity: Option[Int] = None): String

  /**
   * Return new changesets found in the specified path or the default pull
   * location.
   *
   * When bookmarks=True, return a list of (name, node) of incoming bookmarks.
   *
   * revrange - a remote changeset or list of changesets intended to be added
   * force - run even if remote repository is unrelated
   * newest - show newest record first
   * bundle - avoid downloading the changesets twice and store the bundles into
   * the specified file.
   *
   * bookmarks - compare bookmarks (this changes the return value)
   * branch - a specific branch you would like to pull
   * limit - limit number of changes returned
   * nomerges - do not show merges
   */
  def incoming(revRange: Option[String] = None, path: Option[String] = None,
    force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[QRevision]

  def incomingBookmarks(revRange: Option[String] = None, path: Option[String] = None,
    force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[(String, String)]

  /**
   * Return the revision history of the specified files or the entire project.
   *
   * File history is shown without following rename or copy history of files.
   * Use follow with a filename to follow history across renames and copies.
   * follow without a filename will only show ancestors or descendants of the
   * starting revision. followfirst only follows the first parent of merge
   * revisions.
   *
   * If revrange isn't specified, the default is "tip:0" unless follow is set,
   * in which case the working directory parent is used as the starting
   * revision.
   *
   * follow - follow changeset history, or file history across copies and renames
   * date - show revisions matching date spec
   * copies - show copied files
   * keyword - do case-insensitive search for a given text
   * removed - include revisions where files were removed
   * user - revisions committed by user
   * branch - show changesets within the given named branch
   * prune - do not display revision or any of its ancestors
   * limit - limit number of changes displayed
   * nomerges - do not show merges
   */
  def log(files: List[QPath] = Nil, revRange: List[String] = Nil, follow: Boolean = false,
    date: Option[Date] = None, copies: Boolean = false,
    keyword: List[String] = Nil,
    removed: Boolean = false, user: List[String] = Nil,
    branch: List[String] = Nil, prune: List[QNodeId] = Nil,
    limit: Option[Int] = None, noMerges: Boolean = false): List[QRevision]

  /**
   * Yields (nodeid, permission, executable, symlink, file path) tuples for
   * version controlled files for the given revision. If no revision is given,
   * the first parent of the working directory is used, or the null revision if
   * no revision is checked out.
   */
  def manifest(rev: Option[QNodeId] = None): List[(QNodeId, String, Boolean, Boolean, QPath)]
  /**
   * all files from all revisions are yielded (just the name).
   * This includes deleted and renamed files.
   */
  def manifestAll(): List[QPath]

  /**
   * Merge working directory with rev. If no revision is specified, the working
   * directory's parent is a head revision, and the current branch contains
   * exactly one other head, the other head is merged with by default.
   *
   * The current working directory is updated with all changes made in the
   * requested revision since the last common predecessor revision.
   *
   * Files that changed between either parent are marked as changed for the
   * next commit and a commit must be performed before any further updates to
   * the repository are allowed. The next commit will have two parents.
   *
   * force - force a merge with outstanding changes
   *
   * cb - controls the behaviour when Mercurial prompts what to do with regard
   * to a specific file, e.g. when one parent modified a file and the other
   * removed it. It can be a function that gets a
   * single argument which is the contents of stdout. It should return one
   * of the expected choices (a single character).
   */
  def merge(rev: Option[QNodeId] = None, force: Boolean = false,
    interaction: Option[QInteractiveMerge] = None, nonInteractive: Boolean = false): Boolean

  def mergePreview(mergeRevision: QNodeId): List[QRevision]

  /**
   * Mark dest as a copy of source; mark source for deletion.
   *
   * Returns True on success, False if errors are encountered.
   *
   * source - a file
   * dest - a destination file
   * after - record a rename that has already occurred
   * force - forcibly copy over an existing managed file
   * dryrun - do not perform actions, just print output
   */
  def move(source: QPath, dest: QPath, after: Boolean = false,
    force: Boolean = false, dryrun: Boolean = false): Boolean

  def outgoing(revRange: Option[String] = None, path: Option[String] = None,
    force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[QRevision]

  def outgoingBookmarks(revRange: Option[String] = None, path: Option[String] = None,
    force: Boolean = false, newest: Boolean = false,
    branch: Option[String] = None, limit: Option[Int] = None,
    noMerges: Boolean = false, subrepos: Boolean = false): List[(String, String)]

  /**
   * Return the working directory's parent revisions. If rev is given, the
   * parent of that revision will be printed. If file is given, the revision
   * in which the file was last changed (before the working directory revision
   * or the revision specified by rev) is returned.
   */
  def parents(rev: Option[QNodeId] = None, file: Option[QPath] = None): List[QRevision]

  /**
   * return a dictionary of pathname : url of all available names.
   *
   * Path names are defined in the [paths] section of your configuration file
   * and in "/etc/mercurial/hgrc". If run inside a repository, ".hg/hgrc" is
   * used, too.
   */
  def paths(): Map[String, String]

  /**
   * Pull changes from a remote repository.
   *
   * This finds all changes from the repository specified by source and adds
   * them to this repository. If source is omitted, the 'default' path will be
   * used. By default, this does not update the copy of the project in the
   * working directory.
   *
   * Returns True on success, False if update was given and there were
   * unresolved files.
   *
   * update - update to new branch head if changesets were pulled
   * force - run even when remote repository is unrelated
   * rev - a (list of) remote changeset intended to be added
   * bookmark - (list of) bookmark to pull
   * branch - a (list of) specific branch you would like to pull
   */
  def pull(source: Option[String] = None, rev: List[QNodeId] = Nil, update: Boolean = false,
    force: Boolean = false, bookmark: List[String] = Nil, branch: List[String] = Nil): Boolean

  /**
   * Push changesets from this repository to the specified destination.
   *
   * This operation is symmetrical to pull: it is identical to a pull in the
   * destination repository from the current one.
   *
   * Returns True if push was successful, False if nothing to push.
   *
   * rev - the (list of) specified revision and all its ancestors will be pushed
   * to the remote repository.
   *
   * force - override the default behavior and push all changesets on all
   * branches.
   *
   * bookmark - (list of) bookmark to push
   * branch - a (list of) specific branch you would like to push
   * newbranch - allows push to create a new named branch that is not present at
   * the destination. This allows you to only create a new branch without
   * forcing other changes.
   */
  def push(dest: Option[String] = None, rev: List[QNodeId] = Nil, force: Boolean = false,
    bookmark: List[String] = Nil, branch: List[String] = Nil,
    newBranch: Boolean = false): Boolean

  /**
   * Schedule the indicated files for removal from the repository. This only
   * removes files from the current branch, not from the entire project history.
   * Remove (and delete) file even if added or modified (with --force option)
   *
   * Returns True on success, False if any warnings encountered.
   */
  def remove(files: List[QPath]): Boolean

  /**
   * Redo merges or set/view the merge status of given files.
   *
   * Returns True on success, False if any files fail a resolve attempt.
   *
   * all - select all unresolved files
   * mark - mark files as resolved
   * unmark - mark files as unresolved
   */
  def resolve(file: List[QPath] = Nil, all: Boolean = false,
    mark: Boolean = false, unmark: Boolean = false): Boolean

  /**
   * Redo merges or set/view the merge status of given files.
   *
   * returns a list of (code, file path) of resolved
   * and unresolved files. Code will be 'R' or 'U' accordingly.
   *
   * all - select all unresolved files
   * mark - mark files as resolved
   * unmark - mark files as unresolved
   */
  def resolveListFiles(file: List[QPath] = Nil, all: Boolean = false,
    mark: Boolean = false, unmark: Boolean = false): List[(QStatus, QPath)]

  /**
   * Return the root directory of the current repository.
   */
  def root(): File
  /**
   * Return status of files in the repository as a list of (code, file path)
   * where code can be:
   *
   * M = modified
   * A = added
   * R = removed
   * C = clean
   * ! = missing (deleted by non-hg command, but still tracked)
   * ? = untracked
   * I = ignored
   * ' '= origin of the previous file listed as A (added)
   *
   */
  def status(change: Option[QNodeId] = None, ignored: Boolean = false,
    clean: Boolean = false, all: Boolean = false): List[(QStatus, QPath)]

  def summary() = throw new UnsupportedOperationException("It is unclear what should be the result for summary.")

  /**
   * Add one tag specified by names for the current or given revision.
   *
   * Changing an existing tag is normally disallowed; use force to override.
   *
   * Tag commits are usually made at the head of a branch. If the parent of the
   * working directory is not a branch head, a CommandError will be raised.
   * force can be specified to force the tag commit to be based on a non-head
   * changeset.
   *
   * local - make the tag local
   * rev - revision to tag
   * remove - remove a tag
   * message - set commit message
   * date - record the specified date as commit date
   * user - record the specified user as committer
   */
  def tag(name: String, rev: Option[QNodeId] = None, message: Option[String] = None,
    force: Boolean = false, local: Boolean = false, remove: Boolean = false,
    date: Option[Date] = None, user: Option[String] = None): Unit

  /**
   * Return a list of repository tags as: (name, rev, node, islocal)
   */
  def tags(): List[(String, Int, QNodeId, Boolean)]

  /**
   * Return the tip revision (usually just called the tip) which is the
   * changeset most recently added to the repository (and therefore the most
   * recently changed head).
   */
  def tip(): QRevision

  /**
   * Update the repository's working directory to changeset specified by rev.
   * If rev isn't specified, update to the tip of the current named branch.
   *
   * Return the number of files (updated, merged, removed, unresolved)
   *
   * clean - discard uncommitted changes (no backup)
   * check - update across branches if no uncommitted changes
   * date - tipmost revision matching date
   */
  def update(rev: Option[QNodeId] = None, clean: Boolean = false,
    check: Boolean = false, date: Option[Date] = None): (Int, Int, Int, Int)

  /**
   * Return hg version that runs the command server as a 3 fielded tuple: major,
   * minor, micro
   */
  def version(): QVersion

  def apply(node: QNodeId): QChangeContext
  def apply(rev: Int): QChangeContext

  def close()
}

trait JavaQRepository extends QRepository {

  def add(files: java.util.List[QPath]): Boolean = add(files.asScala.toList)
  def addRemove(files: java.util.List[QPath], similarity: Int): Unit = addRemove(files.asScala.toList, similarity)
  def remove(files: java.util.List[QPath]): Boolean = remove(files.asScala.toList)
  def getStatus: java.util.List[(QStatus, QPath)] = status(None).asJava
}

