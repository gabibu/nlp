

import sys
import os
from similarnews.trainning.hetrainning import trainw2v, trainTFIdf
from similarnews.clean.encleaner import ENCleaner
from similarnews.clean.hecleaner import HECleaner
from similarnews.clean.hestopwords import getHeStopWords
from similarnews.predictors.tfidfpredictor import TFIDFPredictor
from similarnews.predictors.wmdpredictor import WMDSimilarityPredictor
from similarnews.predictors.predictorwrapper import PredictorWrapper
import logging



if __name__ == '__main__':


    if len(sys.argv) < 6:
        raise Exception('missing arguments - check readme file')

    language = sys.argv[1]
    model = sys.argv[2]
    modelPath = sys.argv[3]
    ar1Path = sys.argv[4]
    ar2Path = sys.argv[5]

    with open(ar1Path, 'r') as f:
        article1 = f.read()

    with open(ar2Path, 'r') as f:
        article2 = f.read()


    if language == 'EN':


        cleaner = ENCleaner(tokenMinLength=2, minArticleSize=3)


        if model == 'W2V':
            modelPredictor = WMDSimilarityPredictor(modelPath)
        elif model == 'TFIDF':

            modelPredictor = TFIDFPredictor()
            modelPredictor.loadModel(modelPath)

        else:
            raise Exception('unknown model to train {0}'.format(model))

        predictor = PredictorWrapper(modelPredictor, cleaner)

        res = predictor.predict(article1, article2)

        logging.info('sim {0}'.format(res))


    elif language == 'HE':
        heStopWords = getHeStopWords()
        cleaner = HECleaner(heStopWords, tokenMinLength=2, tokenMaxLength=30, minArticleSize=3, morphemes=True)


        if model == 'W2V':
            modelPredictor = WMDSimilarityPredictor(modelPath)
        elif model == 'TFIDF':
            modelPredictor = TFIDFPredictor()
            modelPredictor.loadModel(modelPath)

        else:
            raise Exception('unknown model to train {0}'.format(model))

        predictor = PredictorWrapper(modelPredictor, cleaner)
        res = predictor.predict(article1, article2)
        logging.info('sim {0}'.format(res))

    else:
        raise Exception('unknown language to train {0}'.format(language))



