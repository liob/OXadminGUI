Peter’s OX Server Admin GUI
===========================

This is a fork of [Peter’s OX Server Admin GUI](OXadminGUI) version [0.1.17] to fix an issue when utilized with grizzly [1].

The Netbeans project files are not included since I did not get it to compile. Instead I included a new build.xml.


Install
-------

Download the latest [release] or compile from source.

    tar -Pxzf psoxgui-{version_number}.tgz
    chown -R open-xchange:open-xchange /opt/open-xchange/etc/psoxgui

You need to increase the com.openexchange.connector.maxRequestParameters value to at least 300 in /opt/open-xchange/etc/server.properties.

    /etc/init.d/open-xchange restart

Login URL: https://FQDN/servlet/webserver/index.html


Deinstall
---------

    rm /opt/open-xchange/bundles/at.sciencesoft.oxadmingui.jar
    rm /opt/open-xchange/lib/freemarker.jar
    rm -r /opt/open-xchange/etc/psoxgui/
    rm /opt/open-xchange/osgi/bundle.d/at.sciencesoft.oxadmingui.ini


Build
-----

    ant dist



[OXadminGUI]: http://oxgui.wordpress.com/
[release]: https://github.com/liob/OXadminGUI/releases
[1]: https://forum.open-xchange.com/showthread.php?3750-Peter-s-Open-Xchange-Server-Admin-GUI&p=27021#post27021
[0.1.17]: http://oxgui.sciencesoft.at/psoxgui.0.1.17.src.zip
