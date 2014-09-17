#!/bin/bash
# ====================================================================
# Copyright (c) 2014 Obeo
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    Obeo - initial API and implementation
# ====================================================================

# This script aims to test all 3 logical commands:
#		logicalmerge
#		logicaldiff
#		logicalmertool
# This script has to be updated regarding the developpment state.

set -o nounset 
set -o errexit
#set -o errtrace
#set -o functrace
#set -o xtrace

source Utils.sh

#Location of the current script
scriptLocation="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# If the test in run on a linux gtk x86_64
# TODO improve this
export EMF_COMPARE_GIT_PGM_PATH="$scriptLocation/../../org.eclipse.emf.compare.git.pgm.product/target/products/org.eclipse.emf.compare.git.pgm.product/linux/gtk/x86_64/emfcompare-git-pgm"

#Unique Id used to build a workspace
UUID=$(cat /proc/sys/kernel/random/uuid)
#Creates workspace
currentWorkspace="/tmp/workspace$UUID"
mkdir $currentWorkspace

echo "***************************************************************"
echo "Starting integration test"

# Adds the location of the folder holding git scripts to the path. This way the command "git SCRIPT_NAME ARGS" will work.
PATH=$scriptLocation/..:$PATH

# Configures the workspace and the repository location
config $currentWorkspace 'logicalMergeRepo'


# Creates a fake Oomph configuration file
goToWorkspace
echo 'Some configuration...' > setup.setup

# Creates a new test repo
createRepo

# Create a new file in the repo folder
createFileInRepo "newFile.txt" "some content"

# Adds all unstage files to the staged area
addAllToStageArea

# Commits
commitId=$(commitAndGetId 'First commit')

########################################## Starts testing the logicalmerge command ##########################################################

# Tests the logicalmerge command without any argument
expectedOut="fatal: Argument \"<setup>\" is required in:

logicalmerge <setup> <commit> [--help (-h)] [-m message]

 <setup>     : Path to the setup file. The setup file is a Oomph model
 <commit>    : Commit ID or branch name to merge
 --help (-h) : Dispays help for this command
 -m message  :  Set the commit message to be used for the merge commit (in case
               one is created)."

assertCommandOutput "git logicalmerge" 128 "$expectedOut" "The the logical merge command without any argument" || true

assertCommandOutput "git logicalmerge ../incorectLocation.setup master" 128 "fatal: ../incorectLocation.setup setup file does not exist" "Tests the logicalmerge command with an incorrect path to the setup file"|| true

assertCommandOutput "git logicalmerge ../setup.setup fakeRef" 128 "fatal: fakeRef - not a valid git reference." "Tests the logicalmerge command with an incorrect commit id"|| true

assertCommandOutput "git logicalmerge ../setup.setup master" 0 "Already up to date" "Tests the logicalmerge command with correct reference"|| true

assertCommandOutput "git logicalmerge ../setup.setup $commitId" 0 "Already up to date" "Tests the logicalmerge command with correct commit id" || true

# Changes the current directory in order not to be in a git repository
goToWorkspace

assertCommandOutput "git logicalmerge setup.setup $commitId" 128 "fatal: Can't find git repository" "Tests the logicalmerge outside a git repository scope" || true

########################################## Starts testing the logicaldiff command ##########################################################

goToRepoRoot

expectedOut="fatal: Argument \"<setup>\" is required in:

logicaldiff <setup> <commit> [<compareWithCommit>] [-- <path...>] [--help (-h)]

 <setup>             : Path to the setup file. The setup file is a Oomph model
 <commit>            : Commit ID or branch name
 <compareWithCommit> : Commit ID or branch name. This is to view the changes
                       between <commit> and <compareWithCommit> or HEAD if not
                       specified.
 -- <path...>        : This is used to limit the diff to the named paths (you
                       can give directory names and get diff for all files
                       under them).
 --help (-h)         : Dispays help for this command"

assertCommandOutput "git logicaldiff" 128 "$expectedOut" "Tests the logicaldiff command without any argument"|| true

assertCommandOutput "git logicaldiff ../setup.setup fakeRef" 128 "fatal: fakeRef - not a valid git reference." "Tests the logicaldiff command with an incorrect commit id" || true

assertCommandOutput "git logicaldiff ../setup.setup master" 0 "" "Tests the logicaldiff command with the current branch"|| true

assertCommandOutput "git logicaldiff ../setup.setup $commitId" 0 "" "Tests the logicaldiff command with current commit id" || true

# Changes the current directory in order not to be in a git repository
goToWorkspace

assertCommandOutput "git logicaldiff setup.setup $commitId" 128 "fatal: Can't find git repository" "Tests the logicaldiff outside a git repository scope" || true

########################################## Starts testing the logicalmergetool command ##########################################

goToRepoRoot

expectedOut="fatal: Argument \"<setup>\" is required in:

logicalmergetool <setup> [--help (-h)]

 <setup>     : Path to the setup file. The setup file is a Oomph model
 --help (-h) : Dispays help for this command"

assertCommandOutput "git logicalmergetool" 128 "$expectedOut" "Tests the logicalmergetool command without any argument"|| true

assertCommandOutput "git logicalmergetool ./incorectLocation.setup" 128 "fatal: ./incorectLocation.setup setup file does not exist" "Tests the logicalmergetool command with an incorrect setup file" || true

assertCommandOutput "git logicalmergetool ../setup.setup" 128 "fatal: No conflict to merge" "Tests the logicalmergetool command with a correct setup file" || true

# Changes current directory in order not to be in a git repository
goToWorkspace

assertCommandOutput "git logicalmergetool setup.setup" 128 "fatal: Can't find git repository" "Tests the logicalmergetool command outside a git repository scope" || true

# Creates a conflict between master and conflictingBranch
goToRepoRoot
conflictingBranch="conflictingBranch"
git branch $conflictingBranch > /dev/null 2>&1 
createFileInRepo "ConflictingFile.txt" "Some content"
addAllToStageArea
commitAndGetId "Conflicting commit on master" > /dev/null 2>&1 

git checkout $conflictingBranch > /dev/null 2>&1 
createFileInRepo "ConflictingFile.txt" "Any content"
addAllToStageArea
commitAndGetId "Conflicting commit on $conflictingBranch" > /dev/null 2>&1 
git checkout "master" > /dev/null 2>&1 
# Sets the repository to "Conflicting state"
git merge $conflictingBranch > /dev/null 2>&1  || true # the merge fail but we want it to fail.

assertCommandOutput "git logicalmergetool ../setup.setup" 0 "Should display the differences" "Normal use case" || true

#clean up
rm -rf $currentWorkspace


echo 'End...'
echo '***************************************************************'