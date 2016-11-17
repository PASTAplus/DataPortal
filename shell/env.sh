# This is the shell environment for the 'pasta'. It is sourced from ~/.bashrc 
# in the pasta account.
# 
# The script sets a limited number of environment variables and aliases
# for managing, navigating, and supporting PASTA.
#

# System-wide variables
export APPDIR=$HOME/local

# LTER Git repositories and key project directories, PASTA (Turing)
export GIT=$HOME/git
export DATAPORTAL=$GIT/DataPortal

# Tomcat and Jetty variables and aliases
export TOMCAT=/var/lib/tomcat8
export WEBAPPS=$TOMCAT/webapps
alias tomcat_ps='ps auwwx | grep catalina.startup.Bootstrap' # show Tomcat processes

# New aliases for Tomcat 8 on EDI Data Portal servers
alias tomcat_start="sudo /usr/sbin/service tomcat8 start"
alias tomcat_stop="sudo /usr/sbin/service tomcat8 stop"
