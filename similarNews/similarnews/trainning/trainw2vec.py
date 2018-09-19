
import gensim

def train(sentenses, savePath):

    model = gensim.models.Word2Vec(sentenses, min_count=1)
    model.wv.save_word2vec_format(savePath)

    return model