from decoders.HMMViterbi import  HMMViterbuTagger
from decoders.BasicDecoder import buidParamaters, tag

import  sys

#paramaters:
# 1 -> model
#2 test file
#3 paramaters file 1(smission file for hmm)
#4 paramater file 2 (transition file hmm)
#5 res file
#6 smooth (y/n)

if __name__  == '__main__':


    model = sys.argv[1]
    testFile = sys.argv[2]
    paramatersFile1 = sys.argv[3]

    tagResFile = sys.argv[5]
    smooth = sys.argv[6] == 'y'


    if 'baseline' in model:
        tag(paramatersFile1, testFile, tagResFile)
    else:

        qFile = sys.argv[4]

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

        hmm1 = HMMViterbuTagger(hmmk, smooth, paramatersFile1, qFile, None, testFile,
                                tagResFile, False)

        hmm1.decode()

