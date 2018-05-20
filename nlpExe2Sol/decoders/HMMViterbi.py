# encoding=utf8

from itertools import permutations
from itertools import product

from utils.Constants import SENTENSE_START, SENTENSE_END, UNKNOWN_WORD, LOW_FREQUENCY_WORD_THRESHOLD,\
     SMOOTHING_DELTA

from utils.filereader import  readSentenses
from math import log
def readGramLine(line):
    values = line.split('\t')

    tags = []
    for index, tag in enumerate(values):
        if index == 0:
            logProb = values[0]
        else:
            tags.append(tag)

    return tuple(tags), logProb

def reafLexicalProbabilitiesParamaters(filePath):
    with open(filePath, "r") as f:
        lines = f.readlines()

        gramIndex = 1
        gramToRows = {}
        lineIndex = 1
        while True:
            line = lines[lineIndex].rstrip()
            lineIndex = lineIndex + 1
            if line == '':
                break

            count = line.split('=')[1].rstrip()
            gramToRows[1] = int(count)
            gramIndex = gramIndex + 1

        # skip line
        lineIndex = lineIndex + 1

        gramsToProb = {}
        gram = 1
        while lineIndex < len(lines):
            line = lines[lineIndex].rstrip()

            if line == '':
                gram = gram + 1
                print('gram - > {0}'.format(gram))
                lineIndex = lineIndex + 2
                continue

            ngram, logProb = readGramLine(line)
            gramsToProb[ngram] = float(logProb)

            lineIndex = lineIndex + 1

        return gramsToProb

def reafEmissionsProbabilitiesParamaters(filePath):
    wordTagLogProb = {}
    tags = set()
    words = set()
    with open(filePath, "r") as ins:

        for line in ins:
            if line != '':
                segmentTagsProbs = line.rstrip()

                values = segmentTagsProbs.split('\t')

                word = values[0]
                index = 1

                words.add(word)
                while index < len(values):
                    tag = values[index]
                    logProb = values[index + 1]
                    tags.add(tag)

                    wordTagLogProb[(word, tag)] = float(logProb)

                    index  = index + 2

    return wordTagLogProb, tags, words

class HMMViterbuTagger:

    def __init__(self, hmmK, smoothing, eFile = None, qFile=None, trainFile = None,
                 testFile = None, taggedFile = None, smoothUnKnownWords = False):

        self.smoothing = smoothing
        self.hmmK = hmmK
        self.trainFile = trainFile
        self.eFile = eFile
        self.qFile = qFile
        self.testFile = testFile
        self.taggedFile = taggedFile
        self.tagWordCounter = {}
        self.nGramsToApperanceCounter = {}
        self.ngramTypeTotal = {}
        self.smoothUnKnownWords = smoothUnKnownWords
        self.tagsCache = {}
        if trainFile is not None:
            self.nGramsToApperanceCounter[0] = {}

            for index in range(0, hmmK + 2):
                self.nGramsToApperanceCounter[index] = {}
                self.ngramTypeTotal[index] = 0

            self.nGramsToApperanceCounter[0][tuple([])] = 0
        else:
            self.wordTagLogProb, self.tags, self.words = reafEmissionsProbabilitiesParamaters(eFile)
            self.gramsToLogProb = reafLexicalProbabilitiesParamaters(qFile)




    def getE(self, word, tag):

        wordTagKey = tuple([word, tag])
        if wordTagKey in self.wordTagLogProb:
            return self.wordTagLogProb[wordTagKey]

        else:
            return float('inf')

    def getQ(self, gram):
        mechane = tuple(gram[0: len(gram) -1])
        mone = tuple(gram)

        if mone in self.gramsToLogProb:
            return self.gramsToLogProb[mone] / mechane
        else:
            return float('inf')

    def transitionProb(self, given, target):
        ngram = given[:]
        ngram.append(target)

        key = tuple(ngram)

        if key in self.gramsToLogProb:
            logProb = self.gramsToLogProb[tuple(ngram)]
            return logProb

        return -float('inf')

    def emissionProb(self, word, tag):

        wordToUse = word
        if self.smoothUnKnownWords and  word not in self.words:
            wordToUse = UNKNOWN_WORD

        key = (wordToUse, tag)
        if key in self.wordTagLogProb:
            return self.wordTagLogProb[key]

        #if not self.smoothing:
        if  wordToUse not in self.words:
            if tag == 'NNP':
                return log(1)

        return -float('inf')


    def viterbi(self, sentense):
        bpV = {}
        index = []
        index.append(0)

        for x in  range(0, self.hmmK):
            index.append(SENTENSE_START)


        bpV[tuple(index)] = log(1)
        bpt = {}

        for k in range(1, len(sentense) + 1):
            wordIndex = k - 1
            word = self.wordFromSntense(sentense, wordIndex)

            indexPossiblesTags = self.indexPossibleTags(wordIndex, len(sentense))
            prevIndexesTags = self.indexesPrevTags(wordIndex, len(sentense))

            for currentTag in indexPossiblesTags:

                emissionLogProb = self.emissionProb(word, currentTag)

                for prevTags in prevIndexesTags:

                    transitionLogProb = self.transitionProb(prevTags, currentTag)

                    prevTagsVIndex = prevTags[:]
                    prevTagsVIndex.insert(0, k-1)

                    prevMaxV = bpV[tuple(prevTagsVIndex)]

                    val = prevMaxV + transitionLogProb+ emissionLogProb

                    currentVindexList = []
                    currentVindexList.append(k)
                    currentVindexList.extend(prevTags[1:])

                    currentVindexList.append(currentTag)

                    currentIndexKey = tuple(currentVindexList)

                    if currentIndexKey not in bpV or bpV[currentIndexKey] < val:
                        bpV[currentIndexKey] = val
                        bpt[currentIndexKey] = prevTags[0]


        matchedTags = []
        lastMax = None
        bestLastTags = None
        indexPossiblesTags = self.indexPossibleTags(len(sentense), len(sentense))
        prevIndexesTags = self.indexesPrevTags(len(sentense), len(sentense))

        for currentTag in indexPossiblesTags:
            for prevTags in prevIndexesTags:
                transitionLogProb = self.transitionProb(prevTags, currentTag)
                prevTagsVIndex = prevTags[:]
                prevTagsVIndex.insert(0, len(sentense))

                prevMaxV = bpV[tuple(prevTagsVIndex)]
                val = prevMaxV + transitionLogProb

                if lastMax is None or lastMax < val:
                    lastMax = val
                    bestLastTags = prevTags



        tagIndexInTags = len(bestLastTags) -1

        while tagIndexInTags >=0:
            tag = bestLastTags[tagIndexInTags]
            move = len(bestLastTags) - 1 - tagIndexInTags

            matchedTags.insert(0,[sentense[len(sentense) - move-1],
                                tag])

            tagIndexInTags -= 1






        #for index, tag in enumerate(bestLastTags):
        #    matchedTags.append([sentense[len(sentense) - index-1], bestLastTags[index]])

        startWordIndex = len(sentense) - self.hmmK - 1
        bastNext = bestLastTags
        for currentK in range(startWordIndex, -1, -1):
            currentVindexList = []
            currentVindexList.append(currentK + self.hmmK +1)
            currentVindexList.extend(bastNext)
            prevBestMatch = bpt[tuple(currentVindexList)]

            word = sentense[currentK]

            matchedTags.insert(0, [word, prevBestMatch])
            edited = bastNext[0: len(bastNext)-1]
            edited.insert(0, prevBestMatch)
            bastNext = edited
            #wordIndex -= 1

        return matchedTags




    def indexesPrevTags(self, currentIndex, sentenseLength):

        indexesTags = []
        keyIndex = []
        for index in range(currentIndex - self.hmmK, currentIndex):
            indexTags = self.indexPossibleTags(index, sentenseLength)
            indexesTags.append(indexTags)
            keyIndex.append(tuple(indexTags))

        cacheKey = tuple(keyIndex)
        if cacheKey in self.tagsCache:
            oldList = self.tagsCache[cacheKey]
            #newList = [x[:] for x in oldList]
            return oldList

        arrays = []

        for arrayIndex in range(0, len(indexesTags)):
            copyArrays = []
            for prevIndexTag in indexesTags[arrayIndex]:
                if len(arrays) == 0:
                    copyArrays.append([prevIndexTag])

                else:
                    for arr in arrays:
                        currentCopy = arr[:]
                        currentCopy.append(prevIndexTag)
                        copyArrays.append(currentCopy)

            arrays = copyArrays


        unique = set()

        for arr in arrays:
            unique.add(tuple(arr))

        l = []
        for arr in arrays:
            l.append(list(arr))

        self.tagsCache[cacheKey] = l
        return l

    def indexPossibleTags(self, index, sentenseLength):
        if index < 0:
            startTags = []
            startTags.append(SENTENSE_START)
            return startTags

        if index > sentenseLength -1:
            endTags = []
            endTags.append(SENTENSE_END)
            return endTags

        return self.tags

    def wordFromSntense(self, sentense, index):
        if index < 0:
            return SENTENSE_START

        return sentense[index]

    def addSentsensePro(self, sentense, wordToCounter):

        sentenseTags = []

        for index in range(0, self.hmmK):
            sentenseTags.append(SENTENSE_START)

        for tagSegment in  sentense:
            sentenseTags.append(tagSegment[0])
            word = tagSegment[1]

            tagSegmentToUser = tagSegment
            if self.smoothUnKnownWords and  wordToCounter[word] <= LOW_FREQUENCY_WORD_THRESHOLD:
                tagSegmentToUser = (tagSegment[0], UNKNOWN_WORD)


            if tagSegmentToUser not in  self.tagWordCounter:
                self.tagWordCounter[tagSegmentToUser] = 0

            self.tagWordCounter[tagSegmentToUser] += 1


        sentenseTags.append(SENTENSE_END)



        self.nGramsToApperanceCounter[0][tuple([])] += len(sentenseTags)

        for tagIndex, tag in enumerate(sentenseTags):

            backIndex = tagIndex
            while backIndex >= 0:

                ngramKey = tuple(sentenseTags[backIndex: tagIndex +1])

                if len(ngramKey) in self.nGramsToApperanceCounter:

                    if ngramKey not in self.nGramsToApperanceCounter[len(ngramKey)]:
                        self.nGramsToApperanceCounter[len(ngramKey)][ngramKey] = 0

                    self.nGramsToApperanceCounter[len(ngramKey)][ngramKey] += 1

                    self.ngramTypeTotal[len(ngramKey)] += 1
                    backIndex -= 1
                else:
                    break

    def train(self):
        allSentenses = []
        currentSentense = []
        wordToCounter = {}
        uniqueTags = set()
        for line in open(self.trainFile, 'r'):
            line = line.strip()

            if line:
                segment, tag = line.split('\t')

                uniqueTags.add(tag)
                if segment not in wordToCounter:
                    wordToCounter[segment] = 0

                wordToCounter[segment] += 1

                currentSentense.append((tag, segment))
            else:
                if len(currentSentense) > 0:
                    #self.addSentsensePro(currentSentense)
                    allSentenses.append(currentSentense)

                currentSentense = []



        for sentense in allSentenses:
            self.addSentsensePro(sentense, wordToCounter)

        uniqueTags.add(SENTENSE_START)
        uniqueTags.add(SENTENSE_END)

        if self.smoothing:
             self.addOneSmoothingEmission(uniqueTags, wordToCounter.keys())

        wordToTagAndProb = self.produceEmissionProb(len(wordToCounter.keys()))

        self.writeEmissionProbabilities(wordToTagAndProb)

        if self.smoothing:
             self.addOneSmoothingTransision(uniqueTags)


        self.writeLexicalParamaters()

    def addOneSmoothingTransision(self, tags):
        currentK = 1

        while currentK <= self.hmmK+1:
            tagsPermutations = product(tags, repeat =currentK)


            for permutation in tagsPermutations:

                prev = permutation[0:len(permutation)-1]

                # if prev in  self.nGramsToApperanceCounter[len(prev)]:
                #     prevCounter \
                #         = self.nGramsToApperanceCounter[len(prev)][prev] + SMOOTHING_DELTA
                # else:
                #     prevCounter = prevCounter


                if permutation in  self.nGramsToApperanceCounter[len(permutation)]:
                    currentCounter \
                        = self.nGramsToApperanceCounter[len(permutation)][permutation] + SMOOTHING_DELTA
                else:
                    currentCounter = SMOOTHING_DELTA

                self.nGramsToApperanceCounter[len(permutation)][permutation] = currentCounter


                # if permutation not in self.nGramsToApperanceCounter[len(permutation)]:
                #     self.nGramsToApperanceCounter[len(permutation)][permutation] = 0
                #
                # self.nGramsToApperanceCounter[len(permutation)][permutation] += SMOOTHING_DELTA

            currentK += 1

    def addOneSmoothingEmission(self, tags, words):
        for tag in tags:
            for word in words:
                tagWord = (tag, word)

                if tagWord not in self.tagWordCounter:
                    self.tagWordCounter[tagWord] = 0

                self.tagWordCounter[tagWord] += SMOOTHING_DELTA





















    def decode(self):
        sentenses = readSentenses(self.testFile)

        sentensesTags = []
        for index, sentense in enumerate(sentenses):
            print('tagging index {0} from {1} k {2}'.format(index, len(sentenses), self.hmmK))
            sentenseTagged = self.viterbi(sentense)
            sentensesTags.append(sentenseTagged)

        with open(self.taggedFile, 'w') as file:
            for sentenseTags in sentensesTags:

                for word, tag in sentenseTags:
                    file.write('{0}\t{1}\n'.format(word, tag))

                file.write('\n')

    def writeLexicalParamaters(self):
        with open(self.qFile, 'w') as file:
            file.write('\\data\\\n')
            for index in range(1, self.hmmK + 2):
                file.write('ngram {0} = {1}\n'.format(index, self.ngramTypeTotal[index]))

            file.write('\n')

            for index in range(1, self.hmmK + 2):
                file.write('\\{0}-gram\\\n'.format(index))

                for keys, counter in  self.nGramsToApperanceCounter[index].items():
                    keysList = list(keys)
                    keysStr = ''
                    prevKeys = []
                    for keyIndex, key in  enumerate(keysList):
                        keysStr += key
                        keysStr += '\t'

                        if keyIndex < len(keysList) -1:
                            prevKeys.append(key)

                    indexKey = tuple(prevKeys)

                    prevCounter = self.nGramsToApperanceCounter[index-1][indexKey]


                    prob = float(counter) / prevCounter

                    logProb = log(prob)

                    file.write ('{0}\t{1}\n'.format(logProb, keysStr))

                file.write('\n')

    def writeEmissionProbabilities(self, wordToTagAndProb):
        with open(self.eFile, 'w') as file:
            for word, tagsLogProg in wordToTagAndProb.items():

                file.write('{0}\t'.format(word))
                for tag, logProb in tagsLogProg:
                    file.write('{0}\t{1}\t'.format(tag, logProb))

                file.write('\n')

    def produceEmissionProb(self, vSize):

        wordToTagsProb = {}
        for tagWord, counter in self.tagWordCounter.items():
            tag, word = tagWord
            tagCounter = self.nGramsToApperanceCounter[1][tuple([tag])]

            if self.smoothing:
                prob = float(counter) /(tagCounter + (vSize * SMOOTHING_DELTA))
            else:
                prob = float(counter) / (tagCounter)

            logPorv = log(prob)

            if word not in wordToTagsProb:
                wordToTagsProb[word] = []

            wordToTagsProb[word].append([tag, logPorv])

        return wordToTagsProb

    def increaseCounters(self, tagsSequnce):

        self.nGramsToApperanceCounter[0][tuple([])] += 1

        for i in range(0, self.hmmK +1):
            k = i + 1

            currentNGram = self.nGramsToApperanceCounter[k]

            kTags = tuple(tagsSequnce[len(tagsSequnce) - k:]) #tuple(tagsSequnce[:k])
            if kTags not in currentNGram:
                currentNGram[kTags] = 0

            currentNGram[kTags] += 1


def test(hmmk, smooth,  smoothUnKnown):

    eFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/e1111S_k={0}_smooth={1}_smoothUnKnown={2}.txt'.format(hmmk, smooth, smoothUnKnown)
    qFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/q1k={0}_smooth={1}_smoothUnKnown={2}.txt'.format(hmmk, smooth, smoothUnKnown)

    trainFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.train'
    tagResFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/tagged_k={0}_smooth={1}_smoothUnKnown={2}.txt'.format(hmmk, smooth, smoothUnKnown)

    x = True
    # Train
    hmm1 = HMMViterbuTagger(hmmk, smooth, eFile, qFile, trainFile,
                            None, None,
                            smoothUnKnown)
    print('start train')
    hmm1.train()
    print('finish train -> start decode')

    testFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.test'

    hmm1 = HMMViterbuTagger(hmmk, smooth, eFile, qFile, None, testFile,
                            tagResFile, smoothUnKnown)
    hmm1.decode()

    print('finish decode')
    # tag
    goldFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.gold'
    evaluationFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/eval_k={0}_smooth={1}_smoothUnKnown={2}.txt'.format(hmmk, smooth, smoothUnKnown)
    from evaluation.Evaluator import evaluate,frequentMistakes

    evaluate(tagResFile, goldFile, evaluationFile,
             'basic', False)


def writeTagged(file, sentensesTags):
    with open(file, 'w') as file:
        for sentenseTags in sentensesTags:

            for word, tag in sentenseTags:
                file.write('{0}\t{1}\n'.format(word, tag))

            file.write('\n')

import tempfile


def splitFile(file):

    lines = []
    for line in open(file, 'r'):
        lines.append(line)

    sliceSize = int(float(len(lines))/ 10)

    counterFile = {}
    for index in range(1, 11):
        fd, path = tempfile.mkstemp()
        sliceLines = lines[0 : sliceSize * index]

        counterFile[index] = path
        with open(fd, 'w') as file:
            for line in sliceLines:
                file.write(line)


    return counterFile



def partialTrain():

    trainFileToSplit = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.train'


    files =splitFile(trainFileToSplit)

    for index, trainFile in files.items():

        print('working on index {0}'.format(index))
        eFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/e1111S.txt'
        qFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/q11111S.txt'


        tagResFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/tagged11111S.txt'

        x = True
        # Train
        hmm1 = HMMViterbuTagger(1, True, eFile, qFile, trainFile,
                                None, None,
                                False)
        hmm1.train()

        testFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.test'

        hmm1 = HMMViterbuTagger(1, True, eFile, qFile, None, testFile,
                                tagResFile, False)
        hmm1.decode()

        # tag
        goldFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.gold'
        evaluationFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/eval111S.txt'
        from evaluation.Evaluator import evaluate

        corpusWordAccuracy, corpusSentensesAccuracy = evaluate(tagResFile, goldFile, evaluationFile,
                 'basic', False)

        print('index -> {0} corpusWordAccuracy->{1} corpusSentensesAccuracy->{2}'.format(index,
                                                             corpusWordAccuracy, corpusSentensesAccuracy))




if __name__ == '__main__':

    from evaluation.Evaluator import evaluate, frequentMistakes

    res1 = frequentMistakes('/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/tagged_k=1_smooth=False_smoothUnKnown=True.txt',
                   '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.gold')


    for k in range(3, 6):

        for smooth in range(0, 2):
            for smoothWord in range(0, 2):

                print('k={0} smooth={1} smoothWord={2}'.format(k, smooth, smoothWord))
                test(k, smooth == 1, smoothWord == 1)



