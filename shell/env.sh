# This is the shell environment for the 'pasta'. It is sourced from ~/.bashrc 
# in the pasta account.
# 
# The script sets a limited number of environment variables and aliases
# for managing, navigating, and supporting PASTA.
#

# System-wide variables
export APPDIR=$HOME/local

# Ant variables
export ANT_HOME=$APPDIR/apache-ant
export ANT_BINDIR=$ANT_HOME/bin

# LTER Git repositories and key project directories, PASTA (Turing)
export GIT=$HOME/git
export DATAPORTAL=$GIT/DataPortal

# Tomcat and Jetty variables and aliases
export TOMCAT=/var/lib/tomcat8
export WEBAPPS=$TOMCAT/webapps
alias tomcat_ps='ps auwwx | grep catalina.startup.Bootstrap' # show Tomcat processes

# PATH management
export PATH=$ANT_BINDIR
