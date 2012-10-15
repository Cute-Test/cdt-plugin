#!/bin/bash 
java -jar /home/egraf/eclipse3.7SDK/plugins/org.eclipse.equinox.launcher_*.jar \
 -application org.eclipse.equinox.p2.publisher.UpdateSitePublisher \
 -metadataRepository file://home/egraf/cute/cute_update_site/cute/repository \
 -artifactRepository file://home/egraf/cute/cute_update_site/cute/repository \
 -source //home/egraf/cute/cute_update_site/cute \
 -configs gtk.linux.x86 \
 -compress  \
 -publishArtifacts