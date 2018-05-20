
def readTaggedFile(filePath):
    currentLine = []
    currentTags = []

    with open(filePath, "r") as ins:

        lines = []
        tags = []

        for line in ins:
            stripped = line.rstrip()
            if stripped == '':
                if len(currentLine) > 0:
                    lines.append(currentLine)
                    tags.append(currentTags)
                    currentTags = []
                    currentLine = []
                    continue

            segment, tag = stripped.split('\t')
            currentLine.append(segment)
            currentTags.append(tag)

    if len(currentLine) > 0:
        lines.append(currentLine)
        tags.append(currentTags)

    return lines, tags


def evaluate(taggedFile, goldFile, resFile, model, isSmoothing):

    goldFileLines, goldFileTaggs = readTaggedFile(goldFile)
    taggedFileLines, taggedFileTaggs = readTaggedFile(taggedFile)


    lineLengthAndCorrectCounter = []

    for lineIndex, lineTaggs in  enumerate(taggedFileTaggs):
        goldTaggs = goldFileTaggs[lineIndex]

        correctCounter = 0
        for tagIndex, tag in enumerate(lineTaggs):
            goldTag = goldTaggs[tagIndex]

            if goldTag == tag:
                correctCounter += 1

        lineLengthAndCorrectCounter.append([len(lineTaggs), correctCounter])

    totalNunOfWords = 0
    totalNunOfCorrectTaggedWords = 0
    correctSentenses = 0

    lineIndex =  1
    with open(resFile, 'w') as file:

        file.write("#————————————————————————\n")
        file.write("# Part-of-Speech Tagging Evaluation\n")
        file.write("#————————————————————————\n")
        file.write("#\n")
        file.write("# Model: {0}\n".format(model))
        file.write("# Smoothing: {0}\n".format(isSmoothing))
        file.write("# Test File: {0}\n".format(taggedFile))
        file.write("# Gold File: : {0}\n".format(goldFile))
        file.write("#\n")
        file.write("#————————————————————————\n")
        file.write("# sent-num word-accuracy sent-accuracy\n")
        file.write("#————————————————————————\n")




        for lineLength, correctCounter in lineLengthAndCorrectCounter:

            wordAccurcy = float(correctCounter) / lineLength

            totalNunOfWords += lineLength
            totalNunOfCorrectTaggedWords += correctCounter

            if correctCounter == lineLength:
                lineAccuracy =  1
                correctSentenses += 1
            else:
                lineAccuracy = 0

            file.write('{0}\t{1}\t{2}\n'.format(lineIndex, wordAccurcy, lineAccuracy))

            lineIndex += 1

        file.write("#————————————————————————\n")

        corpusWordAccuracy = float(totalNunOfCorrectTaggedWords) / totalNunOfWords
        corpusSentensesAccuracy = float(correctSentenses) / len(lineLengthAndCorrectCounter)


        file.write('macro-avg {0}\t{1}\n'.format(corpusWordAccuracy, corpusSentensesAccuracy))

        return corpusWordAccuracy, corpusSentensesAccuracy


def frequentMistakes(taggedFile, goldFile):
    goldFileLines, goldFileTaggs = readTaggedFile(goldFile)
    taggedFileLines, taggedFileTaggs = readTaggedFile(taggedFile)


    tagToMistakes = {}
    for lineIndex, lineTaggs in enumerate(taggedFileTaggs):
        goldTaggs = goldFileTaggs[lineIndex]

        for tagIndex, tag in enumerate(lineTaggs):
            goldTag = goldTaggs[tagIndex]

            if goldTag != tag:


                key = (goldTag, tag)
                counter = 0
                if key in tagToMistakes:
                    counter = tagToMistakes[key][2]

                tagToMistakes[key] = [goldTag, tag, counter + 1]


    mistakes = list(tagToMistakes.values())
    mistakes.sort(key=lambda tup: tup[2])  # sorts in place

    print(mistakes)


if __name__ == '__main__':

    taggedFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/res/hmm1/tagged11111S.txt'
    goldFile = '/home/gabib3b/mycode/git/pyspark/nlpexe2/testfiles/heb-pos.gold'
    frequentMistakes(taggedFile, goldFile)













