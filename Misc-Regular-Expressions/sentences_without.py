import re

with open ("normal.txt", "r") as myfile:
	data = myfile.read()
	del_titles = re.sub(r"<TITLE>[a-zA-Z0-9]*<\/TITLE>", "", data)
	sentences = re.split(r"\. ", del_titles)
	print len(sentences)



