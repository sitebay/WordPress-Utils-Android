#!/usr/bin/env python

import logging, sys
import xml.etree.ElementTree as ET
from xml.dom import minidom
import json

xml_indent = '    '
logging.basicConfig(stream=sys.stderr, level=logging.DEBUG)
start_section_template = 'Autogenerated:%s'
end_section_template = '/Autogenerated:%s'

class CustomTreeBuilder(ET.TreeBuilder):
    def comment(self, text):
        self.start(ET.Comment, {})
        self.data(text)
        self.end(ET.Comment)

def xml_parse(xml_file):
    root = None
    ns_map = {} # prefix -> ns_uri
    xml_parser = ET.XMLParser(target=CustomTreeBuilder())
    logging.debug('Parsing %s' % xml_file)
    for event, element in ET.iterparse(xml_file, ['start-ns', 'start', 'end'], parser=xml_parser):
        if event == 'start-ns':
            ns_map[element[0]] = element[1]
        elif event == 'start':
            if root is None:
                root = element
    for prefix, uri in ns_map.items():
        ET.register_namespace(prefix, uri)
    return ET.ElementTree(root)

def format_element(element):
    if element.tag is ET.Comment:
        return '<!--%s-->'
    return '<%(tag)s name="%(name)s">%(text)s</%(tag)s>' % {
        'tag': element.tag,
        'name': element.attrib['name'],
        'text': element.text,
    }

def create_section(tree_root, section_name):
    insertion_point_index = len(tree_root)
    previous_element = list(tree_root)[insertion_point_index-1]
    previous_element.tail = ('\n'+xml_indent) * 3
    start_section_comment = ET.Comment(start_section_template % section_name)
    start_section_comment.tail = '\n'+xml_indent
    tree_root.insert(insertion_point_index, start_section_comment)
    end_section_comment = ET.Comment(end_section_template % section_name)
    end_section_comment.tail = '\n'
    insertion_point_index = insertion_point_index + 1
    tree_root.insert(insertion_point_index, end_section_comment)
    return insertion_point_index

def add_section(tree_root, insertion_point_index, section_name, new_elements):
    if insertion_point_index is None:
        insertion_point_index = create_section(tree_root, section_name)
    # remove strings already present in main xml
    string_names = [element.attrib['name'] for element in tree_root if 'name' in element.attrib]
    string_names_dict = dict.fromkeys(string_names, 1)
    # insert all elements which name attribute is not already present in tree
    logging.debug('Filling section %s' % section_name)
    for new_element in new_elements:
        if 'name' in new_element.attrib and new_element.attrib['name'] in string_names_dict:
            logging.debug('Skipping %s, name already exists in main xml' % format_element( new_element ) )
            continue
        tree_root.insert(insertion_point_index, new_element)
        insertion_point_index = insertion_point_index + 1
    # indent closing tag
    list(tree_root)[insertion_point_index-1].tail = '\n'+xml_indent

def find_and_empty_section(tree_root, section_name):
    removing = False
    start_index = None
    for index, resource_element in enumerate(list(tree_root)):
        if resource_element.tag is ET.Comment and resource_element.text == start_section_template % section_name:
            removing = True
            start_index = index
            logging.debug('Section %s found, emptying...' % section_name)
            continue
        if resource_element.tag is ET.Comment and resource_element.text == end_section_template % section_name:
            logging.debug('Done emptying.')
            return start_index + 1
        if removing:
            tree_root.remove(resource_element)
    logging.debug('Adding a new section %s' % section_name)
    return start_index

def merge_strings(main_xml, extra_sections):
    xml_output_tree = xml_parse(main_xml)
    main_root = xml_output_tree.getroot()
    for extra_section in extra_sections:
        section_tree = xml_parse(extra_section['file'])
        section_name = extra_section['name']
        new_elements = list(section_tree.getroot())
        insertion_point_index = find_and_empty_section(main_root, section_name)
        add_section(main_root, insertion_point_index, section_name, new_elements)
    # make sure xml file ends with a newline
    main_root.tail = '\n'
    xml_string = ('<?xml version="1.0" encoding="UTF-8"?>' + '\n' + 
        ET.tostring(main_root, encoding='utf8').split('\n',1)[1])
    with open(main_xml, 'wb') as xml_file:
        xml_file.write(xml_string)

def main():
    merge_strings(
        './WordPress/src/main/res/values/strings.xml',
        [
            { 'name': 'Gutenberg Native', 'file': './libs/gutenberg-mobile/bundle/android/strings.xml' },
        ]
    )

if __name__ == "__main__":
    main()