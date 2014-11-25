import sys
import re
import operator
import collections
# sys.stdout = open("wordcounter_stopwords.txt", "w")
with open ("normal_lc.txt", "r") as myfile:
	data = myfile.read()
	del_titles = re.sub(r"<TITLE>", "", data)
	del_other_titles = re.sub(r"</TITLE>", "", del_titles)
	del_symbs = re.sub(r"[\"|;|:|,|(|)|\.|\?|!]","",del_other_titles)
	del_stopwords = re.sub(r"\s+a\s+|\s+able\s+|\s+about\s+|\s+across\s+|\s+after\s+|\s+all\s+|\s+almost\s+|\s+also\s+|\s+am\s+|\s+among\s+|\s+an\s+|\s+and\s+|\s+any\s+|\s+are\s+|\s+as\s+|\s+at\s+|\s+be\s+|\s+because\s+|\s+been\s+|\s+but\s+|\s+by\s+|\s+can\s+|\s+cannot\s+|\s+could\s+|\s+dear\s+|\s+did\s+|\s+do\s+|\s+does\s+|\s+either\s+|\s+else\s+|\s+ever\s+|\s+every\s+|\s+for\s+|\s+from\s+|\s+get\s+|\s+got\s+|\s+had\s+|\s+has\s+|\s+have\s+|\s+he\s+|\s+her\s+|\s+hers\s+|\s+him\s+|\s+his\s+|\s+how\s+|\s+however\s+|\s+i\s+|\s+if\s+|\s+in\s+|\s+into\s+|\s+is\s+|\s+it\s+|\s+its\s+|\s+just\s+|\s+least\s+|\s+let\s+|\s+like\s+|\s+likely\s+|\s+may\s+|\s+me\s+|\s+might\s+|\s+most\s+|\s+must\s+|\s+my\s+|\s+neither\s+|\s+no\s+|\s+nor\s+|\s+not\s+|\s+of\s+|\s+off\s+|\s+often\s+|\s+on\s+|\s+only\s+|\s+or\s+|\s+other\s+|\s+our\s+|\s+own\s+|\s+rather\s+|\s+said\s+|\s+say\s+|\s+says\s+|\s+she\s+|\s+should\s+|\s+since\s+|\s+so\s+|\s+some\s+|\s+than\s+|\s+that\s+|\s+the\s+|\s+their\s+|\s+them\s+|\s+then\s+|\s+there\s+|\s+these\s+|\s+they\s+|\s+this\s+|\s+tis\s+|\s+to\s+|\s+too\s+|\s+twas\s+|\s+us\s+|\s+wants\s+|\s+was\s+|\s+we\s+|\s+were\s+|\s+what\s+|\s+when\s+|\s+where\s+|\s+which\s+|\s+while\s+|\s+who\s+|\s+whom\s+|\s+why\s+|\s+will\s+|\s+with\s+|\s+would\s+|\s+yet\s+|\s+you\s+|\s+your\s+","",del_symbs)
	words = re.split(r"\s+", del_stopwords)
	word_counter = collections.Counter(words)
	print len(word_counter)
	#for word, count in word_counter.most_common(10):
	#print word + " " + str(count)
	#print sorted(word_counter.iteritems(), key=operator.itemgetter(1))





