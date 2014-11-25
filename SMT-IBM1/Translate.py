# Michael Bartoli
# CS159 HW6


import sys
from collections import *
import math


def train(english_sentences, foreign_sentences, iterations):
    """Uses the EM algorithm to calculate IBM Model 1
    
    Args:
        english_sentences: the filename containing english sentences
        foreign_sentences: the filename containing foreign sentences
        iterations: the specified number of iterations for the EM algorithm
        probability_threshold: print every p(f|e) above this value
    """
    
    # open+read both files, glue them together sent-by-sent
    dictionary = {}
    ef, ff = open(english_sentences,"r+"), open(foreign_sentences,"r+")
    foreign_sentences, english_sentences = {}, {}
    ecount, fcount = 0, 0
    for eng_sentence in ef.read().split("\n"):
        eng_sentence = "NULL "+eng_sentence
        english_sentences[ecount] = eng_sentence
        ecount+=1
    ef.close()
    for f_sentence in ff.read().split("\n"):
        foreign_sentences[fcount] = f_sentence
        fcount+=1
    ff.close()
    sentences = {} # map english sentence -> foreign sentence
    for i in range(0, len(english_sentences.keys())):
        sentences[english_sentences[i]] = foreign_sentences[i]
    
    # initialize words uniformly
    f_words, e_words = [], []
    f_given_e = {}
    for e_sentence in sentences.keys():
   	for e_word in e_sentence.split(" "):
  		e_words.append(e_word)
   	for f_word in sentences[e_sentence].split(" "):
  		f_words.append(f_word)
    count_ef = Counter() # use collections so if we call something that doesn't exist we won't get null 
    uniq_foreign_words, uniq_english_words = list(set(f_words)), list(set(e_words))
    for f_word in uniq_foreign_words: # initialize each pair to 1 / num of uniq f words
   	for e_word in uniq_english_words:
  		f_given_e[(f_word, e_word)] = 1/float(len(uniq_english_words)) 
  		count_ef[(f_word, e_word)] = 0
    
    # E part of the EM algorithm
    for iteration in range(0, iterations): 
   	count_ef.clear()
   	count_e = {}
   	for word in uniq_english_words:
  		count_e[word] = 0   	
   	for sent in sentences.keys():
  		eng_words = sent.split(" ")
  		foreign_words = sentences[sent].split(" ")
  		the_sum = {} 
  		for i in range(0,len(foreign_words)):
 			foreign_word = foreign_words[i]
 			the_sum[foreign_word] = 0
 			for j in range(0,len(eng_words)):
    				eng_word = eng_words[j]
    				pair = (foreign_word, eng_word)
    				the_sum[foreign_word] += f_given_e[pair]
    
  		for i in range(0,len(foreign_words)):
 			foreign_word = foreign_words[i]
 			for j in range(0,len(eng_words)):
    				eng_word = eng_words[j]
    				pair = (foreign_word, eng_word)
    				count_ef[pair] += float(f_given_e[pair])/float(the_sum[foreign_word])
    				count_e[eng_word] += float(f_given_e[pair])/float(the_sum[foreign_word])
    
        # M part of the EM algorithm
   	for eng_word in uniq_english_words:
  		for for_word in uniq_foreign_words:
 			pair = (for_word, eng_word)
 			f_given_e[pair] = float(count_ef[pair])/float(count_e[eng_word])
 			
 	# print lexically sorted p(f|e) for all f,e on e given a probability threshold
        if (iteration == iterations-1):
            for f_word in uniq_foreign_words:
                possibleE = ""
                possibleEW = 0.0
                for e_word in uniq_english_words:
                    pair = (f_word, e_word)
                    if str(f_given_e[pair])!="0.0":
                        if possibleE is "":
                            possibleE = e_word
                            possibleEW = f_given_e[pair]
                        elif f_given_e[pair] > possibleEW:
                            possibleE = e_word
                            possibleEW = f_given_e[pair]
                dictionary[f_word] = possibleE
    return dictionary

def translate(tfilename, dictionary):
    """Uses the dictionary calcuated from train to translate sentences word-by-word
    
    Args:
        tfilename: the filename for the sentences you want to translate
        dictionary: the dictionary return from train
    """
    tt = open(tfilename,"r+")
    print dictionary
    for t_sentence in tt.read().split("\n"):
        translated = ""
        for t_word in t_sentence.split(" "):
            if t_word in dictionary:
                translated += dictionary[t_word]+" "
            else:
                translated += "UNKNOWN "
        print translated        
    tt.close()
                                           
def main(argv):
    # input parameters
    english_sentences = sys.argv[1]
    foreign_sentences = sys.argv[2]
    iterations = int(sys.argv[3])
    to_translate = sys.argv[4]
    dictionary = train(english_sentences, foreign_sentences, iterations)
    translate(to_translate, dictionary)

if __name__ == "__main__":
    main(sys.argv)