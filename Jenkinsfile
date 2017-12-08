pipeline {
    agent any

	environment {
		RABBIT_LOGIN = credentials('RabbitUserDev')
		BUILD_DATE = sh(returnStdout: true, script: "date +%F-%H-%M-%S")
	}

	stages {
	    stage('Init') {
			steps {
				timeout(time: 10, unit: 'MINUTES') {
					// Load system-specific environment variables
					load "/etc/tradeix/build-environment.dev.groovy"

					// Set deployment scripts as executable
					sh script: 'chmod a+x ./Deployment/*.sh'
					sh 'Deployment/1-init.sh'
				}
			}
		}

		stage('Build') {
			steps {
				timeout(time: 25, unit: 'MINUTES') {
					sh 'Deployment/2-build.sh'
				}
			}
		}

		stage('Deploy') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh 'Deployment/3-deploy.sh'
                }
            }
        }
	}
}