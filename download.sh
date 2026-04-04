rm -f -r .git
rm -f -r org.openmarkov*
git clone -b development https://bitbucket.org/cisiad/org.openmarkov
rm -f -r org.openmarkov/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.annotationProcessing
rm -f -r org.openmarkov.annotationProcessing/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.core
rm -f -r org.openmarkov.core/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.full  
rm -f -r org.openmarkov.full/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.bnEvaluation
rm -f -r org.openmarkov.bnEvaluation/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.costEffectiveness
rm -f -r org.openmarkov.costEffectiveness/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.dbGenerator
rm -f -r org.openmarkov.dbGenerator/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.gui
rm -f -r org.openmarkov.gui/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.inference
rm -f -r org.openmarkov.inference/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.integrationTests
rm -f -r org.openmarkov.integrationTests/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io
rm -f -r org.openmarkov.io/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io.database.elvira
rm -f -r org.openmarkov.io.database.elvira/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io.database.excel
rm -f -r org.openmarkov.io.database.excel/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io.database.weka
rm -f -r org.openmarkov.io.database.weka/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.algorithm
rm -f -r org.openmarkov.learning.algorithm/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.core
rm -f -r org.openmarkov.learning.core/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.gui
rm -f -r org.openmarkov.learning.gui/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.metric
rm -f -r org.openmarkov.learning.metric/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.stochasticPropagationOutput
rm -f -r org.openmarkov.stochasticPropagationOutput/.git
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.sensitivityAnalysis
rm -f -r org.openmarkov.sensitivityAnalysis/.git
git init .
git add .