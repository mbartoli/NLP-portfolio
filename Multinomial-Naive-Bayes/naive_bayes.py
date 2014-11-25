# Michael Bartoli
# cs159 hw7
# Multinomial Naive Bayes Classifier

import sys
from random import randint
from collections import Counter
import math

class MultinomialNaiveBayesClassifier:
    
    def __init__(self):
        """ intializes an object for the class MultinomialNaiveBayesClassifier
    
        Args:
            self: object representing the class MultinomialNaiveBayesClassifier   
        """
        self.train_pos, self.train_neg = Counter(), Counter()
        self.big_bag = Counter()
        self.dev, self.test = {}, {}
        self.pos_count, self.neg_count = 0, 0
        self.lambda_val = 0
        self.prob_pos, self.prob_neg = 0, 0
        self.sumneg, self.sumpos = 0, 0
        #self.listP, self.listN = Counter(), Counter()
        self.temp1,self.temp2,self.temp3,self.temp4 = 0,0,0,0

    
    def countProb(self, training_file):
        """ counts word occurences
    
        Args:
            training_file: the name of the file containing training sentences   
        """
        training_raw = open(training_file, 'r')
        for line in training_raw:
            intra_line = line.split("\t")
            sentiment = intra_line[0]
            review = intra_line[1]
            review_bag = review[:-1].split(" ")
            randInt = randint(0,9)
            if randInt<7:
                if sentiment == "positive":
                    for word in review_bag: 
                        self.train_pos[word] += 1
                        self.big_bag[word] += 1
                    self.pos_count += 1
                else:
                    for word in review_bag: 
                        self.train_neg[word] += 1
                        self.big_bag[word] += 1
                    self.neg_count += 1
            elif randInt==7:
                self.dev[review] = sentiment
            else:
                self.test[review] = sentiment
    
    def classify(self, bag):
        """ classifies a movie review based on raw probabilities
    
        Args:
            bag: bag-of-words representation of a given review  
        
        Returns:
            The sentiment of the given review           
        """
        prob_pos = self.prob_pos
        prob_neg = self.prob_neg
        sumpos = self.sumpos
        sumneg = self.sumneg
        prob_x_neg, prob_x_pos = 1, 1
        for word in bag:
            prob_cur_pos = 0.0
            prob_cur_neg = 0.0
            if word in self.train_pos:
                prob_cur_pos = math.log10(float(self.train_pos[word]+self.lambda_val) / (sumpos + len(self.big_bag)))
            elif word in self.big_bag:
                prob_cur_pos = math.log10(float(self.lambda_val) / (sumpos + len(self.big_bag)))
            if word in self.train_neg:
                prob_cur_neg = math.log10(float(self.train_neg[word]+self.lambda_val) / (sumneg + len(self.big_bag)))
            elif word in self.big_bag:
                prob_cur_neg = math.log10(float(self.lambda_val) / (sumneg + len(self.big_bag)))
            #if math.fabs(self.listP[word]) < math.fabs(prob_cur_pos): self.listP[word] = prob_cur_pos
            #if math.fabs(self.listN[word]) < math.fabs(prob_cur_neg): self.listN[word] = prob_cur_neg
            prob_x_pos += prob_cur_pos
            prob_x_neg += prob_cur_neg
        pos = math.log10(prob_pos) + prob_x_pos
        neg = math.log10(prob_neg) + prob_x_neg
        self.temp1 = prob_pos
        self.temp2 = prob_neg
        self.temp3 += pos
        self.temp4 += neg
        if pos > neg:
            #print "positive\t"+str(pos)
            return "positive"
        else: 
            #print "negative\t"+str(neg)
            return "negative"
    
    def newClassify(self, testing_file):
        """ classifies new reviews that don't have a corresponding sentiment
    
        Args:
            testing_file: the name of the file containing the test sentences       
        """
        testing_raw = open(testing_file, 'r')
        for line in testing_raw:
            print "--------------------------------"
            review_bag = line[:-1].split(" ")
            sentiment = self.classify(review_bag)
            print sentiment
            #print "p(positive): "+str(self.temp1)
            #print "p(negative): "+str(self.temp2)
            #print "p(*|positive): "+str(self.temp3)
            #print "p(*|negative): "+str(self.temp4)
    
    def devsetClassify(self):
        """ classifies reviews in our development set      
        """
        good, bad = 0, 0
        for line in self.dev:
            correct = self.dev[line]
            review_bag = line[:-1].split(" ")
            sentiment = self.classify(review_bag) 
            if sentiment == correct:
                good += 1
            else:
                bad += 1
        #print "good: "+str(good)+"    bad: "+str(bad)
        return float(good/float(good+bad))

        
    def testsetClassify(self):
        """ classifies reviews in our test set      
        """
        good, bad = 0, 0
        for line in self.test:
            correct = self.test[line]
            review_bag = line[:-1].split(" ")
            sentiment = self.classify(review_bag) 
            if sentiment == correct:
                good += 1
            else:
                bad += 1
        #print "good: "+str(good)+"    bad: "+str(bad)
        return float(good/float(good+bad))

    def run(self, training_file, testing_file, lambdav):
        """ wrapper for the the implementation of naive bayes 
    
        Args:
            training_file: the name of the file containing the training data 
            testing_file: the name of the file containing the testing data  
            lambdav: the smoothing constant 
        """
        self.lambda_val = float(lambdav)
        self.countProb(training_file)
        
        self.prob_pos = float(self.pos_count) / (self.pos_count + self.neg_count)
        self.prob_neg = float(self.neg_count) / (self.pos_count + self.neg_count)
        self.sumneg = sum(self.train_neg.values())
        self.sumpos = sum(self.train_pos.values())
        self.newClassify(testing_file)
        

        
        #return self.devsetClassify()
        #print list(reversed(self.listP.most_common()[-10:]))
        #print list(reversed(self.listN.most_common()[-10:]))
        #return self.devsetClassify()
            
           

            
            

# implement the class        
def main(argv):
    training_file, testing_file, lambda_val = argv[1], argv[2], argv[3]
    #classifier = MultinomialNaiveBayesClassifier()
    #classifier.run(training_file, testing_file, lambda_val) 
    """
    lambdas = Counter()
    possible = [lambda_val]#[1, 1.5, 1.75, 2, 2.25, 2.5, 2.75, 3, 3.25, 3.5, 3.75, 4, 5, 10]
    for i in possible:
        avg = 0.0
        iterations = range(1)
        for t in iterations: # average over 5 iterationstions
            classifier = MultinomialNaiveBayesClassifier()
            avg += classifier.run(training_file, testing_file, i) 
        avg = float(avg) / len(iterations)
        lambdas[i] = avg
    print lambdas.most_common(len(possible))
    """
    classifier = MultinomialNaiveBayesClassifier()
    print classifier.run(training_file, testing_file, lambda_val)

if __name__ == "__main__":
    main(sys.argv)