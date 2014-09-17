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

# Runs the emfcompare-git.pgm application fowarding the arguments.
emfcompare-git-pgm (){
	#java -Dequinox.use.ds=true -jar $EMF_COMPARE_GIT_PGM_PATH/plugins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar -consoleLog -nosplash -application emf.compare.git.launcherApp "$@"
	$EMF_COMPARE_GIT_PGM_PATH/emfcompare-git-pgm -data @user.home/.emfcompare_git_pgm/workspace -consoleLog -nosplash --launcher.suppressErrors -application emf.compare.git.launcherApp "$@"
}
