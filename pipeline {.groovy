pipeline {
     agent any
    
    environment {
        DOCKER_IMAGE = "sathvikbhupal1/sample:latest"
        DOCKERHUB_USERNAME = "sathvikbhupal1"
        DOCKERHUB_PASSWORD = "Ankisat0209@"
        EC2_INSTANCE = "ubuntu@54.216.228.55"
        SSH_CREDENTIALS = credentials('ec2-ssh-key')
   }

    
    stages {
        stage('Checkout') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/sathvik9709/application-assessmengt-repo']])          
               
            }
        }

        stage('mvn clean') {
            steps {
                sh 'mvn clean package -DskipTests -Dcheckstyle.skip'
                
           }
       }
       
       stage('Docker Build') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE} ."
                    sh "docker images"  // List images to verify
               }
           }
       }
        stage('Docker Push') {
            steps {
                script {
                    sh 'docker login -u $DOCKERHUB_USERNAME -p $DOCKERHUB_PASSWORD'
                    sh "docker push ${DOCKER_IMAGE}"
               }
           }
       }
       
        stage('Deploy') {
            steps {
                script {
                    sh """
                            ssh -o StrictHostKeyChecking=no ${EC2_INSTANCE} '
                            docker pull ${DOCKER_IMAGE} &&
                            docker stop petclinic || true &&
                            docker rm petclinic || true &&
                            docker run -d --name petclinic -p 80:80 ${DOCKER_IMAGE}
                            '
                        """
                 }
           }
       }
   }

}
   
   

