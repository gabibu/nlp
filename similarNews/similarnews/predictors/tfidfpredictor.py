

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import linear_kernel
import pickle


class TFIDFPredictor:

    def __init__(self):
        self.model = None

    #corpus - list of sentenses
    def train(self, corpus, stopWords, savePath):
        self.model = TfidfVectorizer(stop_words=stopWords)

        texts = []
        for article in corpus:
            txt = ' '.join(article)
            texts.append(txt)

        self.model.fit_transform(texts)

        with open(savePath, 'wb') as fin:
            pickle.dump(self.model, fin)

    def loadModel(self, path):
        self.model = pickle.load(open(path, "rb"))

    def predict(self,   article1, article2):
        invec11 = self.model.transform([article1])
        invec22 = self.model.transform([article2])
        res = linear_kernel(invec11, invec22).flatten()

        return res[0]



if __name__ == '__main__':

    x = ['A reliable ordered delivery protocol for interconnected local area networks',
                                   'Interconnection of broadband local area networks',
                                   'High-speed local area networks and their performance: a surve',
                                   'High-speed switch scheduling for local-area networks',
                                  'Algorithms for Distributed Query Processing in Broadcast Local Area Networks']

    p = TFIDFPredictor()
    p.train(x, None, 'test.pk')

    p.loadModel('test.pk')

    res = p.predict('High-speed local area', 'High-speed switc')

    data = TFIDFPredictor.predict(x)

    print(1)



