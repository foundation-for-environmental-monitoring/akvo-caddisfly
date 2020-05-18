import xml.etree.ElementTree as et
import os
import csv

tree = et.parse('../app/src/main/res/values/strings.xml')

nodes = tree.findall("string")
with open('strings.csv', 'w', newline='', encoding='utf-8') as ff:
    cols = ['name']
    writer = csv.writer(ff)
    for node in nodes:
        values = [ node.attrib[kk] for kk in cols]
        values.append(node.text.replace("\\'","\'"))
        writer.writerow(values)
