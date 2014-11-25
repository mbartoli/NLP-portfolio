# author: Mike Bartoli


import csv
import datetime

def csvToArray(fileName, tweetsColumnHeader = 'Tweet text'):
	"""
		Input: fileName, a path to a CSV file.

		Parses tweets from the given CSV training data into an array of strings,
		each a particular tweet. Hard-coded to assume structure as given by our data.
	"""
	with open(fileName, 'rU') as csvfile:
		# each row of trainingData a dictionary of column label: col value using first-row headers
		trainingData = csv.DictReader(csvfile)
		tweets = []

		for row in trainingData: # create array of tweets
			newRow = ""
			# replace short-links with "@URL"
			for word in row[tweetsColumnHeader].split():
				if "http" in word:
					newRow += "URL "
				else:
					newRow += word + " "

			tweets.append(unicode(newRow, errors='ignore')) 

		return tweets

def getWordsFromTweets(tweets):
	"""
		tweets: A list of strings of all the tweets.

		We get all the words from the tweets for the dictionary for our SVM features.
	"""
	allWords = set()
	for tweet in tweets:
		for word in tweet.split():
			if "http" in word:
				allWords.add("@URL")
			else:
				allWords.add(word)
	return allWords

def getFeatures(tweets, vocabularyWords):
	"""
		Gets the features (word count, represented as a sparse matrix), 
		where we can recover the particular feature labels.

		We then weight features via Tf-idf terms. (http://en.wikipedia.org/wiki/Tf%E2%80%93idf)

		See: http://scikit-learn.org/dev/modules/feature_extraction.html#text-feature-extraction
	"""
	from sklearn.feature_extraction.text import TfidfVectorizer

	vectorizer = TfidfVectorizer(vocabulary = vocabularyWords, ngram_range = (1, 3))
	features = vectorizer.fit_transform(tweets)

	# print "features are: "
	# print features.toarray()
	print "features length is: "
	print len(features.toarray()[0])

	# print "feature names are: "
	# print vectorizer.get_feature_names()
	print "feature name lengths are: "
	print len(vectorizer.get_feature_names())

	return (features.toarray(), vectorizer.get_feature_names())

def getTrainingLabels(fileName, possibleLabels):
	"""
		fileName: a path to a CSV file with our training data.
		possibleLabels: The possibleLabels (i.e. column headers corresponding
			to whatever we are currently training on.)

		NOTE: This has to be changed depending on the structure of the labels.
		It outputs a number corresponding to a particular label, and thus
		this depends on the possible labels. For now, it is 1 if an individual,
		0 otherwise.
	"""
	with open(fileName, 'rU') as csvfile:
		trainingData = csv.DictReader(csvfile)
		trainingLabels = []

		for row in trainingData:
			if row["IsIndividual"] == '0':
				trainingLabels.append(0)
			else:
				trainingLabels.append(1)

		# How it looks, for example, with the structure of the weather data.
		# I'm leaving this in in case we add more labels or other complexity
		# to the data we have.
		# for row in trainingData:
		# 	maxLabel = possibleLabels[0]
		# 	maxLabelValue = 0
		# 	for label in possibleLabels: # get the highest probability level
		# 		if row[label] > maxLabelValue:
		# 			maxLabelValue = row[label]
		# 			maxLabel = label

		return trainingLabels

def svmClassifier(tweetFeatures, tweetLabels):
	"""
		Learns using features and the correct label using an SVM.
	"""	
	# print "Tweet features:"
	# print tweetFeatures
	from sklearn import svm

	classifier = svm.SVC(kernel="linear")
	return classifier.fit(tweetFeatures, tweetLabels)

def decisionTreeClassifier(tweetFeatures, tweetLabels):
	"""
		Learns using features and the correct label via a decision tree.
	"""
	from sklearn import tree

	classifier = tree.DecisionTreeClassifier()
	return classifier.fit(tweetFeatures, tweetLabels)

def randomForestClassifier(tweetFeatures, tweetLabels):
	"""
		Learns using features and the correct label via a random forest.
	"""
	from sklearn import ensemble
	classifier = ensemble.RandomForestClassifier()
	return classifier.fit(tweetFeatures, tweetLabels)

def adaBoostClassifier(tweetFeatures, tweetLabels):
	"""
		Learns using features and the correct label via adaBoost.
	"""
	from sklearn import ensemble
	classifier = ensemble.AdaBoostClassifier()
	return classifier.fit(tweetFeatures, tweetLabels)

def kMeansClassifier(tweetFeatures, k):
	"""
		Clusters the tweets into k different clusters.
	"""
	from sklearn import cluster
	classifier = cluster.KMeans(n_clusters=k)
	return classifier.fit_predict(tweetFeatures)

def gaussianNBClassifier(tweetFeatures, tweetLabels):
	"""
		Learns using features and the correct label via a gaussian naive bayes classifier.
	"""
	from sklearn import naive_bayes
	classifier = naive_bayes.GaussianNB()
	return classifier.fit(tweetFeatures, tweetLabels)

def predictorAccuracy(predictor, testTweetFeatures, testTrainingLabels):
	"""
		Calculates the accuracy on the test set using a given predictor.
	"""
	total = 0
	totalCorrect = 0
	confidenceList = []
	for index in range(len(testTweetFeatures)):
		predictedLabel = predictor.predict(testTweetFeatures[index])
		# print "predictedLabel is: "
		# print predictedLabel
		## USES THE SAME CONFIDENCE SCORE FOR INDIVIDUAL STUFF
		confidence = predictor.decision_function(testTweetFeatures[index])
		confidenceList.append(confidence[0])
		# print "confidence is: "
		# print confidence
		if predictedLabel == testTrainingLabels[index]:
			totalCorrect += 1
		total += 1

	accuracy = float(totalCorrect)/float(total)
	return accuracy

def predictLabels(predictor, tweetFeatures, desiredNumTweets):
	"""
		Predicts labels for each tweet in the rest of the data, returns a list of predictions for 
		that particular label. 

		desiredNumTweets: Takes the number of tweets that you want predicted. It then gives the
		highest num confidence score tweets that prediction, and all others the other prediction.
		Added because otherwise the SVM predicts all tweets to be by organizations, as they
		heavily skew our data.
	"""
	listPredictions = []
	confidenceScores = []
	for index in range(len(tweetFeatures)):
		predictedLabel = predictor.predict(tweetFeatures[index])
		confidence = predictor.decision_function(tweetFeatures[index])
		confidenceScores.append(confidence[0])

	sortedScores = sorted(confidenceScores, reverse=True) # sort from biggest to smallest
	cutOffScore = sortedScores[desiredNumTweets]

	for score in confidenceScores:
		if score >= cutOffScore:
			listPredictions.append(1)
		else:
			listPredictions.append(0)

	return listPredictions

def k_main(diseaseType, k, columnHeader = 'Snippet'):
	"""
	diseaseType: "RA" or "ALZ"
	k: The number of clusters to use.
	columnHeader: defaults to 'Snippet' as per our data, but is the column header
		for the tweets themselves. 

	Takes the disease wanted, finds the unlabeled file according to our file specs
	(see below for format), and then outputs the k_means predictions for each
	in the proper directory.
	"""
	currentTime = datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") # current time for output file

	if diseaseType not in ("RA", "ALZ"):
		print "ERROR: You did not provide a valid diseaseType."
	if diseaseType == "RA":
		unlabeledFileName = "k_RA/RA_unlabeled_individuals.csv"
		outputFileName = "k_RA/RA_" + str(k) + "clusters_individuals_" + currentTime + ".csv"
	if diseaseType == "ALZ":
		unlabeledFileName = "k_ALZ/ALZ_unlabeled_individuals.csv"
		outputFileName = "k_ALZ/ALZ_" + str(k) + "clusters_individuals_" + currentTime + ".csv"

	tweets = csvToArray(unlabeledFileName, columnHeader)
	# print tweets
	wordsInTrainingData = getWordsFromTweets(tweets)
	# print wordsInTrainingData
	(tweetFeatures, featureNames) = getFeatures(tweets, wordsInTrainingData)
	predictions = kMeansClassifier(tweetFeatures, k)

	outputToFile(tweets, predictions, outputFileName, str(k) + "clusters")
	
	# for prediction in predictions: # outputs cluster labels for all tweets
	# 	print prediction 

def findHighestWeightFeatures(predictor, featureNames):
	"""
		Finds the features that are most indicative of an individual having written
		the tweet. Note that if you change reverse to False, then you can get
		those most indicative of an organization.

		Values are just printed out to the terminal.
	"""
	# predictor.coef_[0] has the stuff we want. find the top ten values in it, then
	# get their indices. match with features and output that.

	sortedCoefs = sorted(predictor.coef_[0], reverse=True)
	for i in range(0,100):
		indexOfFeatureName = predictor.coef_[0].tolist().index(sortedCoefs[i])
		print featureNames[indexOfFeatureName]

def svm_main(diseaseType, desiredNumTweets = 10000, columnHeader = 'Snippet'):
	"""
	diseaseType:	The string "RA" OR "ALZ" (CASE-SENSITIVE).
	columnHeader: 	The string in the first row of the csv file that acts
					as the column header for the tweets. Used to get those
					tweets from an arbitrary csv file.
	
	Output:			Writes and creates a new file of the form 
					[RA|ALZ]_svm_DATETIME.csv with the predictions from the machine learning. 
	"""
	import sys
	currentTime = datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") # current time for output file

	labels = ["IsIndividual", "NotIndividual"] # NOTE: Update if more added.
	if diseaseType not in ("RA", "ALZ"):
		print "ERROR: You did not provide a valid diseaseType."
		sys.exit()
	if diseaseType == "RA":
		# NOTE: We currently train on the ALZ data.
		trainFileName = "svm_ALZ/ALZ_training.csv"
		testFileName = "svm_ALZ/ALZ_test.csv"
		unlabeledFileName = "svm_RA/RA_unlabeled.csv"
		outputFileName = "svm_RA/RA_" + currentTime + ".csv"

	elif diseaseType == "ALZ":
		trainFileName = "svm_ALZ/ALZ_training.csv"
		testFileName = "svm_ALZ/ALZ_test.csv"
		unlabeledFileName = "svm_ALZ/ALZ_unlabeled.csv"
		outputFileName = "svm_ALZ/ALZ_" + currentTime + ".csv"

	tweets = csvToArray(trainFileName, columnHeader)
	wordsInTrainingData = getWordsFromTweets(tweets)
	(tweetFeatures, featureNames) = getFeatures(tweets, wordsInTrainingData)
	trainingLabels = getTrainingLabels(trainFileName, labels)

	predictor = svmClassifier(tweetFeatures, trainingLabels)

	# findHighestWeightFeatures(predictor, featureNames)

	####### ACCURACY VALIDATION ON TEST DATA
	# testTweets = csvToArray(testFileName, columnHeader)
	# testTweetFeatures = getFeatures(testTweets, wordsInTrainingData)
	# testLabels = getTrainingLabels(testFileName, labels)

	# accuracy = predictorAccuracy(predictor, testTweetFeatures, testLabels)
	# print "The accuracy of our SVM is: %f" % (accuracy)
	####### END ACCURACY TEST

	unlabeledTweets = csvToArray(unlabeledFileName, columnHeader)
	(unlabeledTweetFeatures, unlabeledFeatureNames) = getFeatures(unlabeledTweets, wordsInTrainingData)
	predictedLabels = predictLabels(predictor, unlabeledTweetFeatures, desiredNumTweets)

	# print "PREDICTEDLABELS ARE"
	# print predictedLabels

	# NOW OUTPUT THESE TO A NEW FILE!!
	outputToFile(unlabeledTweets, predictedLabels, outputFileName, "IsIndividual")

def outputToFile(tweets, labels, fileName, labelType):
	"""
		Outputs to file with first column as "Tweets", "Labels",
		and then those corresponding values. 

	"""
	with open(fileName, 'w+') as csvfile:
		writer = csv.writer(csvfile)
		writer.writerow(["Tweets", labelType])
		for i in range(0, len(tweets)): # len(tweets) should equal len(labels)
			writer.writerow([tweets[i], labels[i]]) 

	print "Done writing file"
