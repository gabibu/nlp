from decoders.HMMViterbi import  HMMViterbuTagger
from decoders.BasicDecoder import buidParamaters

import  sys

#paramater
# 1 -> model
#2 train file path
#3 smooth (y/n)
#4 paramater file 1(emission file) or most frequest tag for word
#5paramaters File 2 transition

if __name__  == '__main__':

    model = sys.argv[1]

    trainFile = sys.argv[2]
    smooth = sys.argv[3] == 'y'
    paramatersFile1 = sys.argv[4]

    if 'baseline' in model:

        buidParamaters(trainFile, paramatersFile1)

    else:

        if 'bi' in model:
            hmmk = 2
        elif 'uni' in model:
            hmmk = 1
        elif 'tri' in model:
            k = 3
        elif 'four-gram' in model:
            k = 4
        elif 'five-gram' in model:
            k = 5

        paramatersFile2 = sys.argv[5]


        hmm1 = HMMViterbuTagger(hmmk, smooth, paramatersFile1, paramatersFile2, trainFile,
                                None, None, False)

        hmm1.train()



