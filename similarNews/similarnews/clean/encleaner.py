

import re
import string
import gensim
from nltk.tokenize import word_tokenize
from gensim.parsing.preprocessing import remove_stopwords


class ENCleaner:

    def __init__(self, tokenMinLength=100000, minArticleSize=3):

        self._tokenMinLength = tokenMinLength
        self._minArticleSize = minArticleSize

    def __removePunctuation(self, words):

        regex = re.compile(
            '[%s]' % re.escape(
                string.punctuation))  # see documentation here: http://docs.python.org/2/library/string.html

        noPunctuation = []

        for token in words:

            new_token = regex.sub(u'', token)
            if not new_token == u'':
                noPunctuation.append(new_token)

        return noPunctuation

    def __removeStopWords(self, text):
        text = remove_stopwords(text)

        return text

    def __tokenize(self, text):
        return word_tokenize(text)


    def clean(self, article):
        text = article.lower()
        text = gensim.parsing.preprocessing.strip_multiple_whitespaces(text)
        text = gensim.parsing.preprocessing.strip_short(text, minsize=self._tokenMinLength)

        text = self.__removeStopWords(text)

        tokens = self.__tokenize(text)
        noPunctuation = self.__removePunctuation(tokens)

        words = [word for word in noPunctuation if len(word) >= self._tokenMinLength]

        return words