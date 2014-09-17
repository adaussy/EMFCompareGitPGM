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


#Location of the current script
scriptLocation="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
echo "Exporting application varaible"
# If the test in run on a linux gtk x86_64
# TODO improve this
export EMF_COMPARE_GIT_PGM_PATH="$scriptLocation/../../org.eclipse.emf.compare.git.pgm.product/target/products/org.eclipse.emf.compare.git.pgm.product/linux/gtk/x86_64/emfcompare-git-pgm"

# Adds the location of the folder holding git scripts to the path. This way the command "git SCRIPT_NAME ARGS" will work.
PATH=$scriptLocation/..:$PATH
