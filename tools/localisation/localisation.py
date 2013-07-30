#!/usr/bin/python
# -*- coding: utf-8 -*-

import argparse
import os
import csv
import re

KEY_COLUMN = 0
STRING_COLUMN = 1

def parseArguments():
	parser = argparse.ArgumentParser()
	parser.add_argument('-d', '--dir', help='directory to scan', required=True)
	parser.add_argument('-l', '--lang', help='language file to produce', required=True)
	parser.add_argument('-f', '--file', help='save language strings to file', required=True)
	parser.add_argument('-p', '--patterns', help='file containing the patterns to match localisable strings', required=True)
	return parser.parse_args()

def collectStrings(dir, patterns):
	print '* Looking for strings in %s' % dir
	
	strings = {}
	
	for (dirPath, dirNames, fileNames) in os.walk(dir):
		for file in fileNames:
			if file.endswith('.java') or file.endswith('.xml'):
				print '    * Processing %s' % file
				textFile = open(os.path.join(dirPath, file))
				lines = textFile.readlines()
				
				for line in lines:
					for pattern in patterns:
						match = pattern.search(line)
					
						if match != None:
							key = match.group('key');
						
							if key != None:
								print '        * Found key %s' % key
								strings[key] = key
								break
				
	return strings

def parseLocalisationFile(file):
	print '* Parsing localisation file %s' % file
	
	strings = {}
	
	if os.path.exists(file):
		print '* Localisation file found'
		
		csvFile = open(file, 'rb')
		csvReader = csv.reader(csvFile)
		
		skip = True
		
		for row in csvReader:
			if skip:
				skip = False
				continue
			
			strings[row[KEY_COLUMN]] = row[STRING_COLUMN]
			
		csvFile.close()
	else:
		print '* Localisation file not found, creating'
		
	return strings
	
def updateLocalisationFile(fileName, codeStrings, localisedStrings):
	print '* Updating localised strings'

	for key in localisedStrings.keys():
		if key not in codeStrings:
			print '    * Deleting key %s' % key
			del localisedStrings[key]
	
	for key, value in codeStrings.iteritems():
		if key not in localisedStrings:
			print '    * Adding new key %s' % key
			localisedStrings[key] = value
		
	csvFile = open(fileName, 'wb')
	csvWriter = csv.writer(csvFile)
	
	csvWriter.writerow(['Key', 'Value', 'Context'])
	
	sortedStrings = localisedStrings.keys()
	sortedStrings.sort()
	
	for key in sortedStrings:
		csvWriter.writerow([key, localisedStrings[key], ''])
	
	csvFile.close()

def getPatterns(fileName):
	print '* Processing patterns file %s' % fileName
	
	patterns = []
	patternsFile = open(fileName, 'r')
	lines = patternsFile.read().splitlines()
	
	for line in lines:
		if len(line) > 0:
			print '    * Found pattern %s' % line
			patterns.append(re.compile(line))
		
	return patterns
	
def main():
	print '\nLOCALISATION TOOL'
	print '=================\n'
	
	args = parseArguments()
	patterns = getPatterns(args.patterns)
	codeStrings = collectStrings(args.dir, patterns)
	localisedStrings = parseLocalisationFile(args.file)
	updateLocalisationFile(args.file, codeStrings, localisedStrings)
	
if __name__ == "__main__":
	main()