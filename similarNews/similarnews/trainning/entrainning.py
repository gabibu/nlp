

import pandas
import logging
from similarnews.trainning import trainw2vec
from similarnews.predictors.tfidfpredictor import TFIDFPredictor

def _readData(allNewsFiles, cleaner):

    articles = []
    for file in allNewsFiles:
        pan = pandas.read_csv(file)
        for article in pan['content']:
            clean = cleaner.clean(article)
            articles.append(clean)

    return articles


def readAndTrainw2v(newsFiles, savePath, cleaner):

    articles = _readData(newsFiles, cleaner)


    logging.info('trainw2vec.train on {0} articles'.format(len(articles)))

    trainw2vec.train(articles, savePath)

    logging.info('model saved in {0}'.format(savePath))

    return trainw2vec


def readAndTrainTFIdf(newsFiles, cleaner, stopWords, savePath):
    logging.info('trainTFIdf')
    articles = _readData(newsFiles, cleaner)
    logging.info('articles {0}'.format(len(articles)))


    predictor = TFIDFPredictor()

    predictor.train(articles, stopWords, savePath)
    logging.info('train completed mode is in {0}'.format(savePath))

    return predictor
