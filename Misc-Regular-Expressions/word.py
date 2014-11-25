import re

with open ("normal_lc.txt", "r") as myfile:
	data = myfile.read()
	del_titles = re.sub(r"<TITLE>", "", data)
	del_other_titles = re.sub(r"</TITLE>", "", del_titles)
	del_symbs = re.sub(r"[\"|;|:|,|(|)|\.|\?|!|\-|\_|&|\']","",del_other_titles)
	sentences = re.split(r"\s", del_symbs)
	print len(sentences)



