#!/usr/bin/env bash

#
# When building branches Travis does not check out the branch but a specific
# commit. This results in the working copy being in "detached HEAD" state.
# This breaks the logic for deriving version numbers from branch names in the
# build script.
# 
# This script checks if the current build corresponds to the tip of a branch.
# If it does then the branch is checked out.
#

function main {
    require_not_building_pull_request
    require_not_triggered_by_tag
    require_on_tip_of_branch
    checkout_branch
}

function require_not_building_pull_request {
    if [ -v TRAVIS_PULL_REQUEST -a "$TRAVIS_PULL_REQUEST" != "false" ]; then
        echo "Building pull request. Will not replace detached head with branch"
        exit 0
    fi
}

#
# In builds triggered by a tag Travis sets the variable TRAVIS_BRANCH to the
# tag name instead of the branch name. It is not possible (and not necessary)
# to check out the branch in this case.
#
function require_not_triggered_by_tag {
    if [ -v TRAVIS_TAG -a "$TRAVIS_TAG" != "" ] ; then
        echo "Build was triggered by a tag. Will not replace detached head with branch"
        exit 0
    fi
}

function require_on_tip_of_branch {
    if [ $TRAVIS_COMMIT != $( git rev-parse --verify $TRAVIS_BRANCH ) ] ; then 
        echo "Detached head does not match tip of current branch. Staying on detached head."
        exit 0
    fi
}

function checkout_branch {
    echo "Detached head matches tip of current branch. Replacing detached head with branch"
    git checkout $TRAVIS_BRANCH 
}

main

