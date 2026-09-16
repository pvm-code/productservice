pipeline {

    agent any

    environment {

        IMAGE_NAME = 'productservice'

        IMAGE_TAG  = "${env.BUILD_NUMBER}"

    }

    stages {

        stage('Checkout') {

            steps {

                checkout scm

            }

        }

        stage('Build Docker Image') {

            steps {

                sh """

                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .

                """

            }

        }

        stage('Login to Amazon ECR') {

            steps {

                withCredentials([[

                    $class: 'AmazonWebServicesCredentialsBinding',

                    credentialsId: 'aws-ecr-creds'

                ]]) {

                    sh """

                        aws ecr get-login-password --region ${AWS_REGION} \

                        | docker login --username AWS --password-stdin ${ECR_REGISTRY}

                    """

                }

            }

        }

        stage('Tag Image') {

            steps {

                sh """

                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} \

                    ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}

                """

            }

        }

        stage('Push Image') {

            steps {

                sh """

                    docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}

                """

            }

        }

        stage('Deploy to Kubernetes') {

            steps {

                sshagent(credentials: ['k8s-server-ssh-key']) {

                    sh """

                        ssh -o StrictHostKeyChecking=no ec2-user@${K8S_SERVER} '

                            sudo kubectl set image deployment/product-service \

                            product-service=${ECR_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}

                            sudo kubectl rollout status deployment/product-service

                        '

                    """

                }

            }

        }

    }

    post {

        success {

            echo 'Product Service deployed successfully.'

        }

        failure {

            echo 'Product Service deployment failed.'

        }

        always {

            sh 'docker image prune -f'

        }

    }

}