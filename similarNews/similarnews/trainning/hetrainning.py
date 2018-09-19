from gensim.corpora import WikiCorpus
from similarnews.trainning import hebtokenizer
from similarnews.trainning import stopwords as getStopWords
from similarnews.trainning import trainw2vec
import os
import logging
from polyglot.text import Word
from similarnews.predictors.tfidfpredictor import TFIDFPredictor
from similarnews.clean.hestopwords import getHeStopWords
x = ['ב', 'ה', 'ו', 'כ', 'ל', 'מ', 'ש']

def _morf(text):

    words = []

    w = Word(text, language="he")
    morp = w.morphemes
    # x
    if len(morp) == 1:
        words.append(text)
        return words
    else:
        notIn = []
        for w in morp:
            if w not in x:
                notIn.append(w)

        if len(notIn) > 1:
            words.append(text)
            return words

        hasInv = False
        for w2 in notIn:
            if len(w2) == 1:
                hasInv = True

        if hasInv:
            words.append(text)
            return words
        else:
            s1 = set(morp)

            if len(s1) == len(morp):
                words.extend(morp)
            else:
                words.append(text)

            return words


def _prepareArticle(articleText, tokenMinLength =-1, tokenMaxLength=100000, stopWords = [], minArticleSize = 3,
                   morphemes = True):
    parts = hebtokenizer.tokenize(articleText)
    sentense = []

    for part in parts:
        type, text = part

        if len(text) < tokenMinLength or len(text) > tokenMaxLength:
            continue
        if type == 'PUNCT':
            continue
        if text in stopWords:
            continue
        elif type == 'URL':
            sentense.append('Link')
        elif type == 'HEB':
            if morphemes:
                wordsFound = _morf(text)

                for word in wordsFound:
                    if len(word) >= tokenMinLength or len(word) <= tokenMaxLength:
                        sentense.append(word)
            else:
                sentense.append(text)

        elif type == 'ENG':
            sentense.append(text)
        elif type == 'JUNK':
            continue
        elif type == 'NUM':
            text = "#" * len(text)
            sentense.append(text)

    if len(sentense) > minArticleSize:
        return sentense

    return None


def _wikiCorpus(filePath, cleaner):

    wiki = WikiCorpus(filePath, lemmatize=False, dictionary={})
    i = 0
    corpuses = []

    for text in wiki.get_texts():

        article = cleaner.clean(text)
        i += 1
        if article:

            # output.write("{}\n".format(article.encode("utf-8")))
            corpuses.append(article)

            if (i % 500 == 0):
                logging.info("processed {0} articles".format(i))
        else:
            logging.info('ignoring article {0}'.format(i))

    return corpuses


def _prepareNewsData(folders, cleaner):
    newsFiles = []

    for folder in folders:
        for path, subdirs, files in os.walk(folder):
            for name in files:
                newsFiles.append(os.path.join(path, name))

    for fileIndex, file in enumerate(newsFiles):

        if fileIndex % 500 == 0:
            logging.info('newsFiles processing file {0} from {1}'.format(fileIndex, len(newsFiles)))

        articleText = open(file).read().strip()

        corpuses = []
        processedText = cleaner.clean(articleText)

        if processedText:
            # output.write("{}\n".format(processedText.encode("utf-8")))
            corpuses.append(processedText)

        return corpuses


def _articles(wikiFilePath, newsFoldersPath, cleaner):

    articles = []
    if wikiFilePath:
        wikiArticles = _wikiCorpus(wikiFilePath, cleaner)
        articles = articles + wikiArticles

    if newsFoldersPath:
        newsArticles = _prepareNewsData(newsFoldersPath, cleaner)
        articles = articles + newsArticles

    return articles


def trainw2v(wikiFilePath, newsFoldersPath, cleaner, savePath):
    logging.info('trainning start')
    articles = _articles(wikiFilePath, newsFoldersPath, cleaner)
    logging.info('trainw2vec.train on {0} articles'.format(len(articles)))

    trainw2vec.train(articles, savePath)

    logging.info('model saved in {0}'.format(savePath))

    return trainw2vec


def trainTFIdf(wikiFilePath, newsFoldersPath, cleaner, savePath):

    logging.info('trainning trainTFIdf start')
    articles = _articles(wikiFilePath, newsFoldersPath, cleaner)
    logging.info('trainTFIdf train on {0} articles'.format(len(articles)))

    predictor = TFIDFPredictor()

    predictor.train(articles, getHeStopWords(), savePath)
    logging.info('train completed mode is in {0}'.format(savePath))

    return predictor
