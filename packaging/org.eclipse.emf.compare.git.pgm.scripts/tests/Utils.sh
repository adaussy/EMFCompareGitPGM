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

# Util class for testing logical command integration

#Configures the util class.
# $1 the path to the workspace
# $2 name of the repository
config(){
	echo "# Configuration:"
	workspace=$1
	echo "#	Current workspace" $workspace
	repositoryName=$2
	echo "#	Repository name" $repositoryName
	repoPath=$workspace/$repositoryName
	echo "#	Repository path" $repoPath
	echo ""
}

# Runs an command and assert the outputs
# $1 Command
# $2 Expected return code
# $3 Expected out
# $4 Test name
assertCommandOutput (){
	result=$($1)
	returnCode=$?

	#Checks return code
	if [ "$returnCode" -ne "$2" ]; then
		echo "*** ERROR Unexpected return code while running \"$1\" ****"
		echo "Test name: $4"
		echo "Expected  $2 but was $returnCode"
		echo ""
		#exit -1
	fi
	if [ "$result" != "$3" ]; then
		echo "*** ERROR Unexpected out while running \"$1\" ****"
		echo "Test name: $4"
		echo "Expected:"
		echo "$3"
		echo "Actual:"
		echo "$result"
		echo ""
		#exit -1
	fi
}

# Creates a new repo and sets the context for next command at the root of this repo.
createRepo(){
	cd $workspace
	git init $repositoryName
	cd $repositoryName
}
# Creates a new file
# $1 = File name
# $2 = File content
createFileInRepo(){
	echo $2 > $workspace/$repositoryName/$1 
}
# Sets the current context for the next command to the root of the git repository.
goToRepoRoot () {
	cd $repoPath
}
# Sets the current context for the next command to the root of the workspace.
goToWorkspace (){
	cd $workspace
}
# Adds all unstaged file to the stage area.
addAllToStageArea () {
	goToRepoRoot
	git add .
}

# Commits what is the staged area
# $1 Message
# echo the id of the new commit
commitAndGetId () {
	goToRepoRoot
	git commit -m "$1" > /dev/null 2>&1 
	local myResult="$(git log -n 1 2>&1 | sed 's/commit\s//' | head -c7)"
	echo $myResult
}


