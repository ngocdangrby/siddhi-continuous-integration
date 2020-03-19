pipeline {
  agent any
  stages {
    stage('Build Docker') {
      steps {
        sh '''cd siddhi-runner

#image name
NAME=\'siddhivt/siddhi-runner-test\'

#remove previous image if existed
#docker image rm -f $(docker images | grep $NAME |  awk \'{print $3}\')

#build new image from Dockerfile
docker build . -t $NAME
docker login -u ngocdangrby -p 658eacab-64e7-4024-b007-16d555b175c7
docker push $NAME
docker ps
'''
      }
    }

    stage('Testing') {
      parallel {
        stage('unit test') {
          steps {
            sh '''export M2_HOME=/usr/local/maven
export PATH=$PATH:$M2_HOME/bin

echo "==========START JunitTEST=========="
mvn --version
ls
cd siddhi-test-suite
mvn test -Dtest=siddhi.test.suite.TemperatureAlertAppUnitTests#testMoniteredFilter test -DfailIfNoTests=false'''
          }
        }

        stage('integration test') {
          steps {
            sh '''export M2_HOME=/usr/local/maven
export PATH=$PATH:$M2_HOME/bin

echo "==========START JunitTEST=========="
mvn --version
ls
cd siddhi-test-suite
mvn test -Dtest=siddhi.test.suite.TemperatureAlertAppIntegrationTests#testDBPersistence test -DfailIfNoTests=false'''
          }
        }

      }
    }

    stage('Dev') {
      steps {
        sh 'echo "Deploy in DEV Environment"'
      }
    }

  }
}