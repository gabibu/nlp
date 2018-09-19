

from similarnews.trainning import hebtokenizer
from polyglot.text import Word

morp_clean = ['ב', 'ה', 'ו', 'כ', 'ל', 'מ', 'ש']


class HECleaner:

    def __init__(self, stopWords, tokenMinLength,  tokenMaxLength=100000, minArticleSize=3,
                        morphemes=True):

        self._stopWords = stopWords
        self._tokenMinLength = tokenMinLength
        self._morphemes = morphemes
        self._tokenMaxLength = tokenMaxLength
        self._minArticleSize = minArticleSize



    def _morf(self, originalWord):

        words = []

        w = Word(originalWord, language="he")
        morp = w.morphemes

        if len(morp) == 1:
            words.append(originalWord)
            return words
        else:
            notIn = []
            for w in morp:
                if w not in morp_clean:
                    notIn.append(w)

            if len(notIn) > 1:
                words.append(originalWord)
                return words

            hasInv = False
            for w2 in notIn:
                if len(w2) == 1:
                    hasInv = True

            if hasInv:
                words.append(originalWord)
                return words
            else:
                s1 = set(morp)

                if len(s1) == len(morp):
                    words.extend(morp)
                else:
                    words.append(originalWord)

                return words

    def _prepareArticle(self, articleText):
        parts = hebtokenizer.tokenize(articleText)
        sentense = []

        for part in parts:
            type, text = part

            if len(text) < self._tokenMinLength or len(text) > self._tokenMaxLength:
                continue
            if type == 'PUNCT':
                continue
            if text in self._stopWords:
                continue
            elif type == 'URL':
                sentense.append('Link')
            elif type == 'HEB':
                if self._morphemes:
                    wordsFound = self._morf(text)

                    for word in wordsFound:
                        if len(word) >= self._tokenMinLength or len(word) <= self._tokenMaxLength:
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

        if len(sentense) > self._minArticleSize:
            return sentense

        return None

    def clean(self, article):
        return self._prepareArticle(article)
