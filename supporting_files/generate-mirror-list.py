#!/usr/bin/python

import re
import sys
import urllib2

# mirrors = ("ftp.at.debian.org", "ftp.au.debian.org",
#            "ftp.ba.debian.org", "ftp.be.debian.org", "ftp.bg.debian.org",
#            "ftp.br.debian.org", "ftp.by.debian.org", "ftp.ca.debian.org",
#            "ftp.ch.debian.org", "ftp.cl.debian.org", "ftp.cn.debian.org",
#            "ftp.cz.debian.org", "ftp.de.debian.org", "ftp.dk.debian.org",
#            "ftp.ee.debian.org", "ftp.es.debian.org", "ftp.fi.debian.org",
#            "ftp.fr.debian.org", "ftp.gr.debian.org", "ftp.hk.debian.org",
#            "ftp.hr.debian.org", "ftp.hu.debian.org", "ftp.ie.debian.org",
#            "ftp.is.debian.org", "ftp.it.debian.org", "ftp.jp.debian.org",
#            "ftp.kr.debian.org", "ftp.lt.debian.org", "ftp.mx.debian.org",
#            "ftp.nc.debian.org", "ftp.nl.debian.org", "ftp.no.debian.org",
#            "ftp.nz.debian.org", "ftp.pl.debian.org", "ftp.pt.debian.org",
#            "ftp.ro.debian.org", "ftp.ru.debian.org", "ftp.se.debian.org",
#            "ftp.si.debian.org", "ftp.sk.debian.org", "ftp.th.debian.org",
#            "ftp.tr.debian.org", "ftp.tw.debian.org", "ftp.ua.debian.org",
#            "ftp.uk.debian.org", "ftp.us.debian.org", "mirror.cc.columbia.edu",
#            "ftp.gtlib.gatech.edu", "mirrors.ece.ubc.ca")


keep = []
mirrors = urllib2.urlopen('http://www.debian.org/mirror/list')
for line in mirrors.readlines():
    m = re.match('.*<td valign="top"><a rel="nofollow" href="http://([^/]*).*">.*', line)
    if m:
        mirror = m.group(1)
        debianurl = 'http://' + mirror + '/debian/dists/jessie'
        ubuntuurl = 'http://' + mirror + '/ubuntu/dists/raring'
        print 'trying: ',
        print mirror,
        print '...',
        sys.stdout.flush()
        try:
            response=urllib2.urlopen(debianurl, timeout=5)
            response=urllib2.urlopen(ubuntuurl, timeout=5)
            keep.append(mirror)
            print 'success!'
        except:
            print 'fail!'

print keep

i = 0
for m in sorted(keep):
    print '"' + m + '", ',
    i += 1
    if i % 3 == 0:
        print ""
