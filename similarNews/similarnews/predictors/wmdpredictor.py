
import gensim
from itertools import product
from collections import defaultdict
from scipy.spatial.distance import euclidean
import pulp


class WMDSimilarityPredictor:

    def __init__(self, modelPath):
        self._model = gensim.models.KeyedVectors.load_word2vec_format(modelPath, binary=modelPath.endswith('bin.gz'))
        self._model.init_sims(replace=True)

    def predict(self, tokenizedArticle1, tokenizedArticle2):

        wmdSimilarity = self.calcSim(tokenizedArticle1, tokenizedArticle2)
        return wmdSimilarity


    def calcSim(self, firstSentenseTokens, secondSentTokens):

        firstSentenseTokens = [token for token in firstSentenseTokens if token in self._model.vocab]
        secondSentTokens = [token for token in secondSentTokens if token in self._model.vocab]

        # secondSentTokens = filter(lambda x: x in self.model.vocab, secondSentTokens)

        # firstSentenseTokens = [token for token in first_sent_tokens if token in self.model]
        # second_sent_tokens = [token for token in second_sent_tokens if token in self.model]

        all_tokens = list(set(firstSentenseTokens + secondSentTokens))
        wordvecs = {token: self._model[token] for token in all_tokens}

        first_sent_buckets = self.tokenToWeight(firstSentenseTokens)
        second_sent_buckets = self.tokenToWeight(secondSentTokens)

        T = pulp.LpVariable.dicts('T', list(product(all_tokens, all_tokens)), lowBound=0)

        prob = pulp.LpProblem('WMD', sense=pulp.LpMinimize)

        prob += pulp.lpSum([T[token1, token2] * euclidean(wordvecs[token1], wordvecs[token2])
                            for token1, token2 in product(all_tokens, all_tokens)])

        for token2 in second_sent_buckets:
            prob += pulp.lpSum([T[token1, token2] for token1 in first_sent_buckets]) == second_sent_buckets[token2]

        for token1 in first_sent_buckets:
            prob += pulp.lpSum([T[token1, token2] for token2 in second_sent_buckets]) == first_sent_buckets[token1]



        prob.solve()

        return pulp.value(prob.objective)

        #return prob


    def tokenToWeight(self, tokens):
        cntdict = defaultdict(lambda: 0)
        for token in tokens:
            cntdict[token] += 1
        totalcnt = sum(cntdict.values())
        return {token: float(cnt) / totalcnt for token, cnt in cntdict.items()}



if __name__ == '__main__':
    x1 = WMDSimilarityPredictor('/home/gabib3b/Desktop/gabiData/testWMD/similarNews/models/en/GoogleNews-vectors-negative300.bin.gz')


    x1.predict([], [])



