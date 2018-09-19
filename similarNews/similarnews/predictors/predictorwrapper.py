
class PredictorWrapper:

    def __init__(self, predictor, cleaner):
        self._predictor = predictor
        self._cleaner = cleaner

    def predict(self,   article1, article2):

        ar1 = self._cleaner.clean(article1)
        ar2 = self._cleaner.clean(article2)

        return self._predictor.predict(ar1, ar2)