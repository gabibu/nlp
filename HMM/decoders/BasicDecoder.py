
def readSentenses(filePath):
    currentLine = []

    with open(filePath, "r") as ins:
        lines = []

        for line in ins:
            strippedWord = line.rstrip()
            if strippedWord == '':
                if len(currentLine) > 0:
                    lines.append(currentLine)
                    currentLine = []
                    continue

            currentLine.append(strippedWord)

    if len(currentLine) > 0:
        lines.append(currentLine)

    return lines

def readBasicParamaters(filePath):
    wordToTag = {}

    with open(filePath, "r") as ins:


        for line in ins:
            if line != '':
                wordTag = line.rstrip()
                word, tag = wordTag.rstrip().split('\t')

                wordToTag[word] = tag

    return wordToTag


def tag(paramatersFile, fileToTag, resFile):

    sentenses = readSentenses(fileToTag)
    wordToTag = readBasicParamaters(paramatersFile)

    sentensesTags = []
    for sentene in sentenses:
        sentenseWordTags = []
        for word in sentene:
            tag = 'NNP'
            if word in wordToTag:
                tag = wordToTag[word]

            sentenseWordTags.append([word, tag])

        sentensesTags.append(sentenseWordTags)


    with open(resFile, 'w') as file:
        for sentenseTags in sentensesTags:

            for word,tag in sentenseTags:

                file.write('{0}\t{1}\n'.format(word, tag))

            file.write('\n')

    print(sentensesTags)

def readSegmentMostFrequestTag(filePath):

    with open(filePath, "r") as ins:

        segmentTagsCounter = {}

        for line in ins:
            stripped = line.rstrip()
            if stripped == '':
                continue

            segment, tag = stripped.split('\t')

            if segment not in  segmentTagsCounter:
                segmentTagsCounter[segment] = {}

            tagToCounter = segmentTagsCounter[segment]


            if tag not in tagToCounter:
                tagToCounter[tag] = 0

            tagToCounter[tag] = tagToCounter[tag] + 1

    segmentToMostFreTag = {}
    for segment, tagsCounter in segmentTagsCounter.items():
        maxCounter = 0
        maxCounterTag = None

        for tag, counter in tagsCounter.items():

            if counter > maxCounter:
                maxCounter = counter
                maxCounterTag = tag

        segmentToMostFreTag[segment] = maxCounterTag


    return segmentToMostFreTag


def buidParamaters(trainFile, outputFile):
    segmentToMostFreTag = readSegmentMostFrequestTag(trainFile)

    with open(outputFile, 'w') as file:

        for word, tag in segmentToMostFreTag.items():
            file.write('{0}\t{1}\n'.format(word, tag))



if __name__ == '__main__':

    #Train
    trainFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.train'
    paramatersFile ='/home/gabib3b/mycode/git/pyspark/nlpexe2/res/basic/p1.txt'
    buidParamaters(trainFile, paramatersFile)

    #tag
    testFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.test'
    tagResFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/basic/r2.txt'
    tag(paramatersFile, testFile, tagResFile)

    goldFile ='/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.gold'
    evaluationFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/basic/eval2.txt'
    from evaluation.Evaluator import evaluate

    evaluate(tagResFile, goldFile, evaluationFile ,
             'basic', False)

    print('completd')

