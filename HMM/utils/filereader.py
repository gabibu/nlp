



def readLinesAndTags(filePath):
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


def readLinesAndTags(filePath):

    linesWordAndTag = []
    currentLine = []
    with open(filePath, "r") as ins:

        for line in ins:
            stripped = line.rstrip()
            if stripped == '':
                if len(currentLine) > 0:
                    linesWordAndTag.append(currentLine)

                    currentLine = []
                    continue

            segment, tag = stripped.split('\t')
            currentLine.append([segment, tag])


    if len(currentLine) > 0:
        linesWordAndTag.append(currentLine)

    return linesWordAndTag





def readTagSegmentCounter(filePath):

    tagTosegmentCounter = {}
    tagToCounter = {}
    totalTags = 0


    with open(filePath, "r") as lins:

        for line in lins:
            stripped = line.rstrip()
            if stripped == '':
                continue

            segment, tag = stripped.split('\t')

            totalTags += 1
            if tag not  in tagToCounter:
                tagToCounter[tag] = 0

            tagToCounter[tag] = tagToCounter[tag] + 1

            if tag not in tagTosegmentCounter:
                tagTosegmentCounter[tag] = {}

            if segment not in tagTosegmentCounter[tag]:
                tagTosegmentCounter[tag][segment] = 0

            tagTosegmentCounter[tag][segment] = tagTosegmentCounter[tag][segment] + 1




    return tagTosegmentCounter, tagToCounter, totalTags


def readFiles(files):
    lines = []
    tags = []
    for file in files:
        lines1, tags1 = read(file)
        lines = lines + lines1
        tags = tags + tags1

    return lines, tags


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



def reafEmissionsProbabilitiesParamaters(filePath):
    wordToTagProg = {}
    tags = set()
    with open(filePath, "r") as ins:

        for line in ins:
            if line != '':
                segmentTagsProbs = line.rstrip()

                values = segmentTagsProbs.split('\t')

                word = values[0]
                index = 1
                wordToTagProg[word] = {}

                while index < len(values):
                    tag = values[index]
                    logProb = values[index + 1]
                    tags.add(tag)
                    wordToTagProg[word][tag] = float(logProb)
                    index  = index + 2

    return wordToTagProg, tags

from entities.Ngrams import Ngram

def readGramLine(gram, line):
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

        #skip line
        lineIndex = lineIndex + 1

        gramsToProb = {}
        gram = 1
        while lineIndex < len(lines):
            line = lines[lineIndex].rstrip()

            if line == '':
                gram = gram + 1
                lineIndex = lineIndex + 2
                continue

            ngram, logProb = readGramLine(gram, line)
            gramsToProb [ngram] = float(logProb)

            lineIndex = lineIndex + 1

        return gramsToProb










