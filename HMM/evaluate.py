



import  sys
from evaluation.Evaluator import evaluate


#paramaters:
#1 tagged file
#2 gold file
#3 model
#4 smooth (y.n)
#5 evaluation file

if __name__  == '__main__':


    tagResFile = sys.argv[1]
    goldFile = sys.argv[2]
    model = sys.argv[3]
    smooth = sys.argv[4] == 'y'
    evaluationFile = sys.argv[5]



    evaluate(tagResFile, goldFile, evaluationFile,
             model, smooth)