#!/bin/sh
#
# This script uses httrack to download an offline copy of a redmine project
# that can then be used as Eclipse help pages.
#
# To include a page in the help, you need to reference it from the "toc.xml"
# or similar, we do not (yet) generate this automatically. The output is written
# to a "web" directory.
#
# After downloading the page, we need to change its style so that all the
# redmine header and sidebar are hidden. We do this by appending some css
# rules to a stylesheet (e.g. eclipsehelp.css). Depending on your redmine
# theme, you might need to adapt these rules. Also make sure that they come
# last and are not overridden by the theme again. For our *ators, we included
# an empty dummy css file (eclipsehelp) at the correct position (see the end
# of this script).
#
# You can either generate the documentation and commit it to the repository, or
# even better call this script as part of your build. With maven, you can use the
# "Exec Maven Plugin":
#
#   <build>
#            <plugins>
#                    <plugin>
#                            <groupId>org.codehaus.mojo</groupId>
#                            <artifactId>exec-maven-plugin</artifactId>
#                            <version>1.1</version>
#                            <executions>
#                                    <execution>
#                                    <phase>generate-resources</phase>
#                                            <goals>
#                                                    <goal>exec</goal>
#                                            </goals>
#                                    </execution>
#                            </executions>
#                            <configuration>
#                                    <executable>sh</executable>
#                                    <workingDirectory>docs</workingDirectory>
#                                    <commandlineArgs>update_doc_from_wiki.sh</commandlineArgs>
#                            </configuration>
#                    </plugin>
#            </plugins>
#    </build>
#

# The name of the project (domain and redmine project)
DOMAIN="cute-test"
PROJECT="cute"

# This page will be used as the entry point for httrack:
START_PAGE="User_Guide"

echo "Generating offline help from ${PROJECT} wiki."

hash httrack 2>&- || { echo >&2 "Required program 'httrack' is not installed.  Aborting."; exit 1; }

# We have to exclude some URLs that aren't used anyway, and we need to add all
# the directories that we want to make available offline. If you update your
# documentation regularly, you might want to enable the cache (remove --cache
# option).
httrack "http://${DOMAIN}.com/projects/${PROJECT}/wiki/${START_PAGE}" "http://${DOMAIN}.com/stylesheets/application.css" -n --cache=0 -O "web" "+*/attachments/*" "+*/themes/*" "+*/stylesheets/*" "-*/javascripts/*" "-*/activity*" "+*/images*"  "-*files?sort*"

# HTTrack adds an invalid tag to the output that trips Eclipse's parser:
echo "Sanitizing HTML..."
find web -name "*.html" -type f -exec sed -i -e "/^.*Added by HTTrack.*$/d" {} \;
find web -name "*.html" -type f -exec sed -i -e "/^.*<!-- Mirrored from.*$/d" {} \;

# You might need to adapt these rules or the file they are appened to:
echo '
.help_breadcrumbs {display: none;}
#top-menu, #header, #footer, #sidebar {display: none;}
body, #wrapper {background-color: #fff;}
#main, #header, #top-menu, #content {border: 0;}
#content {width:98%!important; border-right: none!important;}
' >> web/${DOMAIN}.com/stylesheets/jquery/jquery-ui-1.9.2a99a.css
