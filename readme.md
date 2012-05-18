Dynamic Web Project for the MAGE
=======
# Merlin Java Servlet for Tomcat

## How it works

The servlets list for specific URL patterns and either run optMAGE or Javamage

This directory may be packaged as WAR file and places in the `/webapps` folder of a Tomcat sever to run

Ensure that before packaging the WAR folder. The config file in WEB-INF 
is correctly configured.

Additionally, ensure that `maxPostSize="20000000"` or 20 megabytes to ensure
that the genome can be transfered.

> optMAGE Scripts developed by Harris Wang at Harvard Wyss Institute

`CIDAR LAB 2012 - Samir Ahmed`



