

import sys
import os
from similarnews.trainning.entrainning import readAndTrainw2v, readAndTrainTFIdf
from similarnews.trainning.hetrainning import trainw2v, trainTFIdf
from similarnews.clean.encleaner import ENCleaner
from similarnews.clean.hecleaner import HECleaner
from similarnews.clean.hestopwords import getHeStopWords
if __name__ == '__main__':


    if len(sys.argv) < 4:
        raise Exception('missing arguments - check readme file')

    language = sys.argv[1]
    model = sys.argv[2]
    saveModelPath = sys.argv[3]




    if language == 'EN':

        ar1 = os.path.join(os.path.dirname(__file__), '..', 'data', 'en', 'allthenews', 'articles1.csv') #, 'articles1.csv'
        ar2 = os.path.join(os.path.dirname(__file__), '..', 'data', 'en', 'allthenews', 'articles2.csv')
        ar3 = os.path.join(os.path.dirname(__file__), '..', 'data', 'en', 'allthenews', 'articles3.csv')

        files = [ar1, ar2, ar3]


        cleaner = ENCleaner(tokenMinLength=2, minArticleSize=3)

        if model == 'W2V':
            readAndTrainw2v(files, saveModelPath, cleaner)
        elif model == 'TFIDF':
            readAndTrainTFIdf(files, cleaner, None, saveModelPath)
        else:
            raise Exception('unknown model to train {0}'.format(model))

    elif language == 'HE':
        heStopWords = getHeStopWords()
        cleaner = HECleaner(heStopWords, tokenMinLength=2, tokenMaxLength=30, minArticleSize=3, morphemes=True)

        wikiFilePath = os.path.join(os.path.dirname(__file__), '..', 'data', 'he', 'wiki',
                                    'hewiki-latest-pages-articles.xml.bz2')

        news7 = os.path.join(os.path.dirname(__file__), '..', 'data', 'he', 'a7NewsTXT')
        newsHaaretz = os.path.join(os.path.dirname(__file__), '..', 'data', 'he', 'haaretz_txt')
        newsThemarker = os.path.join(os.path.dirname(__file__), '..', 'data', 'he', 'themarkerUTF8TXT')
        newsFolders = [news7, newsHaaretz, newsThemarker]
        if model == 'W2V':


            trainw2v(wikiFilePath, newsFolders, cleaner, saveModelPath)

        elif model == 'TFIDF':
            trainTFIdf(wikiFilePath, newsFolders, cleaner, saveModelPath)

        else:
            raise Exception('unknown model to train {0}'.format(model))

    else:
        raise Exception('unknown language to train {0}'.format(language))



