import xml.etree.ElementTree as et
import os
import csv

#file_path = '../app/src/main/res/values-kn/strings_cuvette.xml'
#file_path = '../app/src/main/res/values-te/strings_cuvette.xml'
file_path = '../app/src/main/res/values/strings_cuvette.xml'
#file_path = '../app/src/main/res/values/strings.xml'

tree = et.parse(file_path)

nodes = tree.findall("string")
with open('strings.csv', 'w', newline='', encoding='utf-8') as ff:
    cols = ['name']
    writer = csv.writer(ff)
    for node in nodes:
        values = [ node.attrib[kk] for kk in cols]
        values.append(node.text.replace("\\'","\'"))
        writer.writerow(values)
